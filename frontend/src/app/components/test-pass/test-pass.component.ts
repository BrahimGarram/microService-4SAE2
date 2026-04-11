import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AffectationTestService, AffectationTest, AffectationQuestion, AFFECTATIONS_API_BASE, RunCodeResult } from '../../services/affectation-test.service';
import { UserService, User } from '../../services/user.service';
import { FaceVerificationService } from '../../services/face-verification.service';

@Component({
  selector: 'app-test-pass',
  templateUrl: './test-pass.component.html',
  styleUrls: ['./test-pass.component.css']
})
export class TestPassComponent implements OnInit, OnDestroy {
  @ViewChild('cameraVideo') cameraVideoRef!: ElementRef<HTMLVideoElement>;

  affectationId: number | null = null;
  affectation: AffectationTest | null = null;
  questions: AffectationQuestion[] = [];
  currentIndex = 0;
  userAnswers: Record<number, string> = {};
  /** Résultat du dernier Run pour la question CODING courante */
  runCodeResult: RunCodeResult | null = null;
  runCodeLoading = false;
  loading = true;
  errorMessage = '';
  submitted = false;
  /** True si le candidat a quitté l'onglet ou si le visage ne correspond pas à la photo. */
  disqualified = false;
  /** Raison : 'tab' = sortie d'onglet, 'face' = visage ne correspond pas, 'no_face' = 0 visage, 'multiple_faces' = 2+ visages. */
  disqualifyReason: 'tab' | 'face' | 'no_face' | 'multiple_faces' | null = null;
  score = 0;
  correctCount = 0;
  totalCount = 0;
  isValidated = false;
  /** Temps restant pour la question courante (secondes) */
  questionTimeLeft = 0;
  private startTime: number = 0;
  private questionTimerId: ReturnType<typeof setInterval> | null = null;
  /** Durée par défaut si la question n'en a pas (secondes) */
  private readonly defaultQuestionDuration = 60;
  private audioContext: AudioContext | null = null;
  /** Gestionnaire beforeunload pour envoyer l'abandon quand l'utilisateur quitte l'onglet ou le site. */
  private beforeunloadHandler = (): void => this.sendAbandonBeacon();
  /** Gestionnaire visibilitychange : détecte le passage à un autre onglet (triche). */
  private visibilityHandler = (): void => this.onVisibilityChange();
  /** Utilisateur connecté (pour récupérer email + nom du freelancer). */
  currentUser: User | null = null;
  /** Scores par domaine (par ex. { JAVA: 75, ANGULAR: 50 }). */
  domainScores: { [domain: string]: number } = {};
  /** Feedback IA (Gemini) sur le test. */
  aiFeedback: string | null = null;
  aiFeedbackLoading = false;
  aiFeedbackError = '';

  /** Caméra : pending | allowed | denied | error. Le test ne se charge qu'après autorisation. */
  cameraStatus: 'pending' | 'allowed' | 'denied' | 'error' = 'pending';
  mediaStream: MediaStream | null = null;
  private faceCheckIntervalId: ReturnType<typeof setInterval> | null = null;
  private scoreLogIntervalId: ReturnType<typeof setInterval> | null = null;
  /** Vérification faciale (visage + correspondance photo) toutes les 2 secondes. */
  private readonly FACE_CHECK_INTERVAL_MS = 2_000;
  /** Délai avant de démarrer le test (timer + vérification faciale), en ms. */
  private readonly DELAY_BEFORE_TEST_MS = 2_000;
  /** Délai avant la première vérification faciale (laisse le temps à la caméra d'afficher des images). */
  private readonly DELAY_BEFORE_FIRST_FACE_CHECK_MS = 3_000;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private affectationService: AffectationTestService,
    private userService: UserService,
    private faceVerification: FaceVerificationService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.queryParamMap.get('id');
    if (!id) {
      this.router.navigate(['/test']);
      return;
    }
    this.affectationId = +id;
    this.startTime = Date.now();
    this.requestCamera();
  }

  ngOnDestroy(): void {
    this.stopFaceCheckInterval();
    this.stopScoreLogInterval();
    this.releaseCamera();
    this.exitFullscreenMode();
    this.clearQuestionTimer();
    this.removeBeforeunloadListener();
    this.removeVisibilityListener();
  }

  /** Demande l'accès à la caméra. En cas de succès, charge l'utilisateur et l'affectation puis lance le test. */
  private requestCamera(): void {
    if (typeof navigator === 'undefined' || !navigator.mediaDevices?.getUserMedia) {
      this.cameraStatus = 'error';
      this.loading = false;
      this.errorMessage = 'Your browser or device does not support camera access. The test requires the camera to be enabled.';
      return;
    }
    // Résolution minimale pour que CompreFace détecte le visage dans la capture
    navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'user', width: { ideal: 640 }, height: { ideal: 480 } },
      audio: false
    })
      .then((stream) => {
        this.mediaStream = stream;
        this.cameraStatus = 'allowed';
        this.loadCurrentUser();
        this.loadAffectation();
      })
      .catch((err) => {
        console.error('Camera error:', err);
        this.cameraStatus = err?.name === 'NotAllowedError' ? 'denied' : 'error';
        this.loading = false;
        this.errorMessage = this.cameraStatus === 'denied'
          ? 'Camera access is required to take the test. Please allow the camera and reload the page.'
          : 'Unable to access the camera. Check permissions and try again.';
      });
  }

  private releaseCamera(): void {
    if (this.mediaStream) {
      this.mediaStream.getTracks().forEach((t) => t.stop());
      this.mediaStream = null;
    }
  }

  private attachStreamToVideo(): void {
    const video = this.cameraVideoRef?.nativeElement;
    if (video && this.mediaStream) {
      video.srcObject = this.mediaStream;
    }
  }

  private startFaceCheckInterval(): void {
    this.stopFaceCheckInterval();
    this.stopScoreLogInterval();
    this.faceCheckIntervalId = setInterval(() => this.runFaceCheck(), this.FACE_CHECK_INTERVAL_MS);
    this.scoreLogIntervalId = setInterval(() => this.logFaceScore(), 1_000);
  }

  private stopFaceCheckInterval(): void {
    if (this.faceCheckIntervalId != null) {
      clearInterval(this.faceCheckIntervalId);
      this.faceCheckIntervalId = null;
    }
    this.stopScoreLogInterval();
  }

  private stopScoreLogInterval(): void {
    if (this.scoreLogIntervalId != null) {
      clearInterval(this.scoreLogIntervalId);
      this.scoreLogIntervalId = null;
    }
  }

  /** Log le score de détection du visage chaque seconde (console). */
  private logFaceScore(): void {
    if (this.submitted || this.disqualified || !this.mediaStream) return;
    const video = this.cameraVideoRef?.nativeElement;
    if (!video || video.readyState < 2) return;
    this.faceVerification.getFaceScore(video).then((score) => {
      const s = score != null ? score.toFixed(3) : '—';
      console.log(`[FaceVerification] score visage (1s): ${s}`);
    });
  }

  private async runFaceCheck(): Promise<void> {
    if (this.submitted || this.disqualified || !this.affectationId || !this.mediaStream) return;
    const video = this.cameraVideoRef?.nativeElement;
    if (!video || video.readyState < 2) return;
    const profilePhoto = (this.currentUser as any)?.profilePhoto ?? (this.currentUser as any)?.profilePicture;
    const result = await this.faceVerification.compareWithProfile(video, profilePhoto);
    if (result === 'no_match' || result === 'no_face' || result === 'multiple_faces') {
      this.disqualifyReason = result === 'no_face' ? 'no_face' : result === 'multiple_faces' ? 'multiple_faces' : 'face';
      this.disqualified = true;
      this.submitted = true;
      this.exitFullscreenMode();
      this.clearQuestionTimer();
      this.stopFaceCheckInterval();
      this.removeBeforeunloadListener();
      this.removeVisibilityListener();
      this.releaseCamera();
      this.affectationService.reportFraud(this.affectationId).subscribe({ next: () => {}, error: () => {} });
    }
  }

  /** Charge l'utilisateur connecté (freelancer) pour obtenir son email et son nom à envoyer dans l'email de résultat. */
  private loadCurrentUser(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
      },
      error: () => {
        this.currentUser = null;
      }
    });
  }

  private enterFullscreenMode(): void {
    if (typeof document === 'undefined') return;
    document.body.classList.add('test-fullscreen');
    try {
      document.documentElement.requestFullscreen?.();
    } catch {
      // ignore
    }
  }

  private exitFullscreenMode(): void {
    if (typeof document === 'undefined') return;
    document.body.classList.remove('test-fullscreen');
    try {
      if (document.fullscreenElement) {
        document.exitFullscreen?.();
      }
    } catch {
      // ignore
    }
  }

  /** Détecte la sortie de l'onglet (Alt+Tab) → fraude ; au retour sur l'onglet, réessaie d'enregistrer la fraude en base. */
  private onVisibilityChange(): void {
    if (typeof document === 'undefined') return;
    if (document.visibilityState === 'hidden') {
      if (this.submitted || this.disqualified || !this.affectationId || this.questions.length === 0) return;
      this.disqualifyReason = 'tab';
      this.disqualified = true;
      this.submitted = true;
      this.exitFullscreenMode();
      this.clearQuestionTimer();
      this.removeBeforeunloadListener();
      this.sendFraudBeacon();
      this.affectationService.reportFraud(this.affectationId).subscribe({ next: () => {}, error: () => {} });
    } else if (document.visibilityState === 'visible' && this.disqualified && this.affectationId) {
      this.affectationService.reportFraud(this.affectationId).subscribe({ next: () => {}, error: () => {} });
    }
  }

  /** Envoie la fraude via fetch keepalive pour que la requête aboutisse même quand l'onglet est caché. */
  private sendFraudBeacon(): void {
    if (this.affectationId == null) return;
    const token = typeof localStorage !== 'undefined' ? localStorage.getItem('kc-access-token') : null;
    const url = `${AFFECTATIONS_API_BASE}/${this.affectationId}/fraud`;
    const headers: Record<string, string> = {};
    if (token) headers['Authorization'] = `Bearer ${token}`;
    try {
      fetch(url, { method: 'PATCH', headers, keepalive: true }).catch(() => {});
    } catch {
      // ignore
    }
  }

  private addVisibilityListener(): void {
    if (typeof document === 'undefined') return;
    document.addEventListener('visibilitychange', this.visibilityHandler);
  }

  private removeVisibilityListener(): void {
    if (typeof document === 'undefined') return;
    document.removeEventListener('visibilitychange', this.visibilityHandler);
  }

  /** Envoie un abandon via fetch keepalive (quand l'utilisateur ferme l'onglet ou quitte le site). */
  private sendAbandonBeacon(): void {
    if (this.submitted || this.affectationId == null || this.questions.length === 0) return;
    const token = typeof localStorage !== 'undefined' ? localStorage.getItem('kc-access-token') : null;
    const url = `${AFFECTATIONS_API_BASE}/${this.affectationId}/abandon`;
    const headers: Record<string, string> = {};
    if (token) headers['Authorization'] = `Bearer ${token}`;
    try {
      fetch(url, { method: 'PATCH', headers, keepalive: true }).catch(() => {});
    } catch {
      // ignore
    }
  }

  private addBeforeunloadListener(): void {
    if (typeof window === 'undefined') return;
    window.addEventListener('beforeunload', this.beforeunloadHandler);
  }

  private removeBeforeunloadListener(): void {
    if (typeof window === 'undefined') return;
    window.removeEventListener('beforeunload', this.beforeunloadHandler);
  }

  /** Retourne la durée de la question en secondes (API: duration ou durationInSeconds). */
  private getQuestionDurationSeconds(index: number): number {
    const q = this.questions[index];
    if (!q) return this.defaultQuestionDuration;
    const d = (q as { duration?: number; durationInSeconds?: number }).duration
      ?? (q as { duration?: number; durationInSeconds?: number }).durationInSeconds;
    return d != null && d > 0 ? d : this.defaultQuestionDuration;
  }

  private clearQuestionTimer(): void {
    if (this.questionTimerId != null) {
      clearInterval(this.questionTimerId);
      this.questionTimerId = null;
    }
  }

  private getAudioContext(): AudioContext | null {
    if (typeof window === 'undefined') return null;
    if (!this.audioContext) {
      this.audioContext = new (window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext)();
    }
    return this.audioContext;
  }

  /** Bip court (dernières secondes). */
  private playTickSound(): void {
    const ctx = this.getAudioContext();
    if (!ctx) return;
    if (ctx.state === 'suspended') {
      ctx.resume().catch(() => {});
    }
    try {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.frequency.value = 600;
      osc.type = 'sine';
      gain.gain.setValueAtTime(0.15, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.08);
      osc.start(ctx.currentTime);
      osc.stop(ctx.currentTime + 0.08);
    } catch {
      // ignore
    }
  }

  /** Son « temps écoulé ». */
  private playTimeUpSound(): void {
    const ctx = this.getAudioContext();
    if (!ctx) return;
    if (ctx.state === 'suspended') {
      ctx.resume().catch(() => {});
    }
    try {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.frequency.value = 400;
      osc.type = 'sine';
      gain.gain.setValueAtTime(0.2, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.3);
      osc.start(ctx.currentTime);
      osc.stop(ctx.currentTime + 0.3);
    } catch {
      // ignore
    }
  }

  /** Démarre le compte à rebours pour la question courante ; à 0, marque comme écoulé et passe à la suivante ou soumet. */
  private startQuestionTimer(): void {
    this.clearQuestionTimer();
    this.questionTimeLeft = this.getQuestionDurationSeconds(this.currentIndex);
    this.questionTimerId = setInterval(() => {
      this.questionTimeLeft--;
      if (this.questionTimeLeft > 0 && this.questionTimeLeft <= 10) {
        this.playTickSound();
      }
      if (this.questionTimeLeft <= 0) {
        this.playTimeUpSound();
        this.clearQuestionTimer();
        if (this.currentIndex < this.questions.length - 1) {
          this.currentIndex++;
          this.ensureCodingAnswer();
          this.startQuestionTimer();
        } else {
          this.submitTest();
        }
      }
    }, 1000);
  }

  private loadAffectation(): void {
    if (!this.affectationId) return;
    this.affectationService.getAffectationById(this.affectationId).subscribe({
      next: (a) => {
        this.affectation = a;
        const raw = a.questions && a.questions.length ? a.questions : [];
        this.questions = raw.map((q: AffectationQuestion) => this.normalizeQuestion(q));
        this.totalCount = this.questions.length;
        if (this.questions.length === 0) {
          this.loading = false;
          this.errorMessage = 'No questions in this test.';
        } else {
          setTimeout(() => {
            this.loading = false;
            this.enterFullscreenMode();
            this.addBeforeunloadListener();
            this.addVisibilityListener();
            this.ensureCodingAnswer();
            this.startQuestionTimer();
            // Attacher le flux vidéo après le prochain rendu pour que #cameraVideo soit dans le DOM
            setTimeout(() => this.attachStreamToVideo(), 0);
            // Démarrer la vérification faciale après un délai pour que la caméra affiche des images (évite frames noirs)
            setTimeout(() => this.startFaceCheckInterval(), this.DELAY_BEFORE_FIRST_FACE_CHECK_MS);
          }, this.DELAY_BEFORE_TEST_MS);
        }
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Unable to load the test.';
      }
    });
  }

  /** Normalise une question (camelCase ou snake_case) pour que le template ait toujours questionType, questionText, starterCode, etc. */
  private normalizeQuestion(q: AffectationQuestion): AffectationQuestion {
    const raw = q as AffectationQuestion & { question_text?: string; question_type?: string; starter_code?: string };
    return {
      ...q,
      id: q.id,
      questionText: q.questionText ?? raw.question_text,
      questionType: (q.questionType ?? raw.question_type ?? '').toUpperCase(),
      choices: q.choices ?? [],
      correctAnswer: q.correctAnswer,
      duration: q.duration,
      durationInSeconds: q.durationInSeconds,
      language: q.language,
      starterCode: q.starterCode ?? raw.starter_code ?? '',
    };
  }

  get currentQuestion(): AffectationQuestion | null {
    return this.questions[this.currentIndex] ?? null;
  }

  /** Indique si la question courante est de type CODING (robuste : questionType ou question_type, toute casse). */
  isCurrentQuestionCoding(): boolean {
    const q = this.currentQuestion;
    return q != null && this.isCodingQuestion(q);
  }

  /** Indique si une question est de type CODING (pour le template et la révision). */
  isCodingQuestion(q: AffectationQuestion | null | undefined): boolean {
    if (!q) return false;
    const raw = q as AffectationQuestion & { question_type?: string };
    const type = String(q.questionType ?? raw.question_type ?? '').trim().toUpperCase();
    return type === 'CODING';
  }

  /** Affichage du temps restant (M:SS). */
  get questionTimeFormatted(): string {
    const m = Math.floor(this.questionTimeLeft / 60);
    const s = this.questionTimeLeft % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  }

  /** Pourcentage de progression (pour la barre). */
  get progressPercent(): number {
    if (this.questions.length === 0) return 0;
    return ((this.currentIndex + 1) / this.questions.length) * 100;
  }

  /** Lettre de choix A, B, C, D... */
  choiceLetter(index: number): string {
    return String.fromCharCode(65 + index);
  }

  setAnswer(value: string): void {
    this.userAnswers[this.currentIndex] = value;
    if (this.isCurrentQuestionCoding()) {
      this.runCodeResult = null;
    }
  }

  /** Langage pour la question CODING courante */
  getCodingLanguage(): string {
    const q = this.currentQuestion as AffectationQuestion & { language?: string };
    return (q?.language || 'java').toUpperCase();
  }

  /** Code de départ pour la question CODING (une seule fois) */
  getStarterCode(): string {
    const q = this.currentQuestion as AffectationQuestion & { starterCode?: string };
    return q?.starterCode || '';
  }

  /** Initialise userAnswers avec le starterCode pour les questions CODING au premier affichage. N'utilise pas currentQuestion pour éviter récursion. */
  ensureCodingAnswer(): void {
    const q = this.questions[this.currentIndex] ?? null;
    if (!q || !this.isCodingQuestion(q)) return;
    const cur = this.userAnswers[this.currentIndex];
    if (cur === undefined || cur === null) {
      const raw = q as AffectationQuestion & { starter_code?: string };
      this.userAnswers[this.currentIndex] = q.starterCode ?? raw.starter_code ?? '';
    }
  }

  runCode(): void {
    const q = this.currentQuestion;
    if (!q || !this.isCodingQuestion(q) || !q.id) return;
    const code = this.userAnswers[this.currentIndex] ?? this.getStarterCode();
    this.runCodeLoading = true;
    this.runCodeResult = null;
    this.affectationService.runCode(q.id, code).subscribe({
      next: (res) => {
        this.runCodeLoading = false;
        this.runCodeResult = res;
      },
      error: () => {
        this.runCodeLoading = false;
        this.runCodeResult = {
          success: false,
          score: 0,
          passedCount: 0,
          totalCount: 0,
          message: 'Error during execution.'
        };
      }
    });
  }

  getSelectedAnswer(): string {
    return this.userAnswers[this.currentIndex] || '';
  }

  next(): void {
    if (this.currentIndex < this.questions.length - 1) {
      this.currentIndex++;
      this.ensureCodingAnswer();
      this.startQuestionTimer();
    }
  }

  /**
   * Normalise une réponse pour la comparaison : trim, string, minuscules,
   * et synonymes vrai/faux (true/false).
   */
  private normalizeAnswer(value: string | number | undefined | null): string {
    if (value == null) return '';
    const s = String(value).trim().toLowerCase().replace(/\s+/g, ' ');
    if (s === 'true') return 'vrai';
    if (s === 'false') return 'faux';
    return s;
  }

  /** Méthode publique pour le template : indique si la réponse à la question i est correcte (QCM/VRAI_FAUX). Pour CODING, on ne sait pas côté client. */
  isUserAnswerCorrect(index: number): boolean {
    const q = this.questions[index];
    if (!q) return false;
    if (this.isCodingQuestion(q)) return false; // évalué côté serveur
    const userAnswer = this.userAnswers[index] ?? '';
    return this.isAnswerCorrect(userAnswer, q.correctAnswer);
  }

  /** Retourne true si la réponse de l'utilisateur correspond à la réponse correcte. */
  private isAnswerCorrect(userAnswer: string, correctAnswer: string | number | undefined | null): boolean {
    const u = this.normalizeAnswer(userAnswer);
    const c = this.normalizeAnswer(correctAnswer);
    if (u === '') return false;
    return u === c;
  }

  /** Calcule le score en pourcentage (bonnes réponses / total). */
  calculateScore(): number {
    let correct = 0;
    this.questions.forEach((q, i) => {
      const userAnswer = this.userAnswers[i] ?? '';
      if (this.isAnswerCorrect(userAnswer, q.correctAnswer)) {
        correct++;
      }
    });
    const total = this.questions.length;
    return total > 0 ? Math.round((correct / total) * 100) : 0;
  }

  submitTest(): void {
    if (!this.affectationId || !this.affectation) return;
    this.stopFaceCheckInterval();
    this.releaseCamera();
    this.exitFullscreenMode();
    this.clearQuestionTimer();
    this.removeBeforeunloadListener();
    const timeSpent = Math.round((Date.now() - this.startTime) / 1000);
    const answers = this.questions.map((q, i) => ({
      questionId: q.id!,
      answer: this.userAnswers[i] ?? ''
    }));
    const freelancerEmail = this.currentUser?.email;
    const freelancerName = this.currentUser?.username;
    this.affectationService.submitTest(this.affectationId, timeSpent, answers, freelancerEmail, freelancerName).subscribe({
      next: (a) => {
        this.submitted = true;
        this.score = a.score ?? 0;
        this.correctCount = a.correctAnswersCount ?? 0;
        this.totalCount = a.totalQuestions ?? this.questions.length;
        this.isValidated = (a.isValidated === true) || (this.score >= 60);
        this.domainScores = {};
        if (a.domainScoresJson) {
          try {
            this.domainScores = JSON.parse(a.domainScoresJson) || {};
          } catch {
            this.domainScores = {};
          }
        }
        // Le feedback IA ne se génère pas automatiquement : l'utilisateur clique sur « Obtenir un feedback IA »
      },
      error: (err) => {
        const msg = err?.error?.error;
        this.errorMessage = typeof msg === 'string' && msg ? msg : 'Error submitting results.';
      }
    });
  }

  goToIntro(): void {
    this.router.navigate(['/test']);
  }

  domainKeys(): string[] {
    return Object.keys(this.domainScores || {});
  }

  /** Découpe le rapport IA en sections (titres ###) pour un affichage structuré. */
  get aiFeedbackSections(): { title?: string; content: string }[] {
    const raw = this.aiFeedback?.trim();
    if (!raw) return [];
    const normalized = raw.replace(/^\s*###\s*/, '\n### ');
    const parts = normalized.split(/\n\s*###\s*/).map(p => p.trim()).filter(Boolean);
    const sections: { title?: string; content: string }[] = [];
    for (const block of parts) {
      const lineEnd = block.indexOf('\n');
      const firstLine = lineEnd >= 0 ? block.slice(0, lineEnd).trim() : block;
      const rest = lineEnd >= 0 ? block.slice(lineEnd + 1).trim() : '';
      const looksLikeTitle = /^[\d\)]|Points|Recommandations|à améliorer|forts/i.test(firstLine);
      if (looksLikeTitle && (rest || firstLine.length < 80)) {
        sections.push({ title: firstLine, content: rest || firstLine });
      } else {
        sections.push({ content: block });
      }
    }
    return sections;
  }

  /** Appelle le backend pour obtenir le rapport de feedback IA (une seule fois). */
  fetchAiFeedback(): void {
    if (!this.affectationId) return;
    this.aiFeedbackLoading = true;
    this.aiFeedbackError = '';
    this.affectationService.getAiFeedback(this.affectationId).subscribe({
      next: (res) => {
        this.aiFeedbackLoading = false;
        this.aiFeedback = res.feedback;
      },
      error: (err) => {
        this.aiFeedbackLoading = false;
        const msg = err?.error?.error;
        this.aiFeedbackError = typeof msg === 'string' && msg ? msg : 'Impossible de récupérer le feedback IA pour le moment.';
      }
    });
  }
}
