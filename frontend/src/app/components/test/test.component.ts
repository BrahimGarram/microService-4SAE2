import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import {
  QuestionTestService,
  QuestionTest,
  AiQuestionGenerateRequest,
  AiGeneratedQuestionDto,
  AiQuestionGenerateResponse
} from '../../services/QuestionTestService';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';

const DOMAINS = ['JAVA', 'SPRING_BOOT', 'ANGULAR', 'RESEAU', 'SQL', 'DEVOPS', 'DOCKER', 'IA'] as const;
const QUESTION_TYPES = ['QCM_SIMPLE', 'CODING', 'VRAI_FAUX'] as const;
const DIFFICULTY_LEVELS = ['EASY', 'MEDIUM', 'HARD', 'EXPERT'] as const;

function minChoicesQcmValidator(group: AbstractControl): ValidationErrors | null {
  const form = group as FormGroup;
  const questionType = form.get('questionType')?.value;
  const choices = form.get('choices') as FormArray;
  if (questionType !== 'QCM_SIMPLE' || !choices) return null;
  const filledCount = choices.controls.filter(
    c => c.value != null && String(c.value).trim().length > 0
  ).length;
  return filledCount >= 2 ? null : { minChoicesQcm: { required: 2, actual: filledCount } };
}

@Component({
  selector: 'app-test',
  templateUrl: './test.component.html',
  styleUrl: './test.component.css'
})
export class TestComponent implements OnInit {
  // Liste des questions depuis la base de données
  questions: QuestionTest[] = [];
  filteredQuestions: QuestionTest[] = [];
  
  // Filtres
  searchTerm: string = '';
  selectedDomain: string = '';
  selectedDifficulty: string = '';
  selectedType: string = '';
  selectedStatus: string = '';
  
  // Statistiques
  totalQuestions: number = 0;
  activeQuestions: number = 0;
  expertQuestions: number = 0;
  domainCount: number = 0;
  
  // Pagination
  currentPage: number = 1;
  totalPages: number = 1;
  itemsPerPage: number = 10;

  // Vue : cartes (moderne) ou tableau
  viewMode: 'cards' | 'table' = 'cards';

  // Loading state
  isLoading: boolean = false;
  errorMessage: string = '';

  // Detail modal (voir les détails)
  showDetailModal = false;
  detailQuestion: QuestionTest | null = null;
  detailLoading = false;

  // Edit modal
  showEditModal = false;
  editingQuestionId: number | null = null;
  editForm!: FormGroup;
  editErrorMessage = '';
  editSuccessMessage = '';
  showEditValidationErrors = false;
  isSavingEdit = false;
  /** Erreurs de validation backend (édition) */
  editBackendErrors: { field: string; message: string }[] = [];

  readonly domains = DOMAINS;
  readonly questionTypes = QUESTION_TYPES;
  readonly difficultyLevels = DIFFICULTY_LEVELS;

  // Agent IA : génération de questions
  aiPrompt = '';
  aiSelectedDomains: string[] = [];
  aiNumberOfQuestions: number | null = null;
  aiLoading = false;
  aiMessage = '';
  aiNeedsNumber = false;
  aiProposals: (AiGeneratedQuestionDto & { selected?: boolean })[] = [];
  aiInserting = false;

  // Détails d'une question générée par l'IA (avant insertion)
  showAiDetailModal = false;
  aiDetailQuestion: AiGeneratedQuestionDto | null = null;

  constructor(
    private questionService: QuestionTestService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.buildEditForm();
  }

  private buildEditForm(): void {
    this.editForm = this.fb.group(
      {
        questionText: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]],
        domain: ['', Validators.required],
        questionType: ['', Validators.required],
        difficultyLevel: ['', Validators.required],
        duration: [60, [Validators.min(1)]],
        choices: this.fb.array([]),
        correctAnswer: ['', [Validators.required, Validators.minLength(1)]],
        isActive: [true]
      },
      { validators: minChoicesQcmValidator }
    );
    this.editForm.get('questionType')?.valueChanges.subscribe(type => {
      const choicesArray = this.editForm.get('choices') as FormArray;
      if (type === 'QCM_SIMPLE' && choicesArray.length === 0) {
        this.addEditChoice();
        this.addEditChoice();
      }
      if (type !== 'QCM_SIMPLE') {
        while (choicesArray.length) {
          choicesArray.removeAt(0);
        }
      }
    });
  }

  get editChoices(): FormArray {
    return this.editForm.get('choices') as FormArray;
  }

  addEditChoice(): void {
    this.editChoices.push(this.fb.control('', Validators.required));
  }

  removeEditChoice(index: number): void {
    if (this.editChoices.length > 1) {
      this.editChoices.removeAt(index);
    }
  }

  get showEditChoices(): boolean {
    return this.editForm.get('questionType')?.value === 'QCM_SIMPLE';
  }

  get editChoicesMinError(): boolean {
    return !!this.editForm.errors?.['minChoicesQcm'];
  }

  ngOnInit() {
    this.loadQuestions();
  }

  // === Agent IA : génération de questions ===

  toggleAiDomain(domain: string): void {
    if (this.aiSelectedDomains.includes(domain)) {
      this.aiSelectedDomains = this.aiSelectedDomains.filter(d => d !== domain);
    } else {
      this.aiSelectedDomains = [...this.aiSelectedDomains, domain];
    }
  }

  askAiGenerate(): void {
    this.aiMessage = '';
    this.aiNeedsNumber = false;
    if (!this.aiPrompt || !this.aiPrompt.trim()) {
      this.aiMessage = 'Veuillez saisir un prompt pour l’agent (par ex. "Génère des questions Angular niveau intermédiaire").';
      return;
    }
    const payload: AiQuestionGenerateRequest = {
      prompt: this.aiPrompt.trim(),
      numberOfQuestions: this.aiNumberOfQuestions ?? null,
      domains: this.aiSelectedDomains.length ? this.aiSelectedDomains : null
    };
    this.aiLoading = true;
    this.aiProposals = [];
    this.questionService.generateQuestionsWithAi(payload).subscribe({
      next: (res: AiQuestionGenerateResponse) => {
        this.aiLoading = false;
        this.aiMessage = res.message;
        this.aiNeedsNumber = res.needsNumberOfQuestions;
        this.aiProposals = (res.questions || []).map(q => ({ ...q, selected: true }));
      },
      error: () => {
        this.aiLoading = false;
        this.aiMessage = 'Erreur lors de l’appel à l’agent IA (génération de questions).';
        this.aiNeedsNumber = false;
        this.aiProposals = [];
      }
    });
  }

  insertSelectedAiQuestions(): void {
    const selected = this.aiProposals.filter(p => p.selected);
    this.aiMessage = '';
    if (!selected.length) {
      this.aiMessage = 'Sélectionnez au moins une question proposée avant de l’insérer.';
      return;
    }
    this.aiInserting = true;
    const creations = selected.map(p => {
      const payload: QuestionTest = {
        questionText: p.questionText,
        domain: p.domain,
        questionType: p.questionType,
        difficultyLevel: p.difficultyLevel,
        duration: p.duration ?? 60,
        choices: p.choices || [],
        correctAnswer: p.correctAnswer || '',
        isActive: p.isActive !== false,
        language: p.language,
        starterCode: p.starterCode,
        testCasesJson: p.testCasesJson
      };
      return this.questionService.createQuestion(payload);
    });
    forkJoin(creations).subscribe({
      next: () => {
        this.aiInserting = false;
        this.aiMessage = `✅ ${selected.length} question(s) générée(s) ont été insérées avec succès dans la base.`;
        this.loadQuestions();
        // On retire les questions insérées de la liste pour éviter les doublons
        this.aiProposals = this.aiProposals.filter(p => !p.selected);
        // On réinitialise le formulaire de l’agent pour un nouveau cycle
        this.aiPrompt = '';
        this.aiSelectedDomains = [];
        this.aiNumberOfQuestions = null;
      },
      error: () => {
        this.aiInserting = false;
        this.aiMessage = 'Certaines questions n’ont pas pu être insérées. Vérifiez la liste et réessayez.';
      }
    });
  }

  openAiDetail(q: AiGeneratedQuestionDto & { selected?: boolean }): void {
    this.aiDetailQuestion = q;
    this.showAiDetailModal = true;
  }

  closeAiDetailModal(): void {
    this.showAiDetailModal = false;
    this.aiDetailQuestion = null;
  }

  // Charger toutes les questions depuis la base de données
  loadQuestions() {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.questionService.getAllQuestions().subscribe({
      next: (data) => {
        this.questions = data;
        this.filteredQuestions = [...this.questions];
        this.calculateStats();
        this.calculatePagination();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des questions:', error);
        this.errorMessage = 'Erreur lors du chargement des questions. Veuillez réessayer.';
        this.isLoading = false;
      }
    });
  }

  // Calculer les statistiques
  calculateStats() {
    this.totalQuestions = this.questions.length;
    this.activeQuestions = this.questions.filter(q => q.isActive).length;
    this.expertQuestions = this.questions.filter(q => q.difficultyLevel === 'EXPERT').length;
    this.domainCount = new Set(this.questions.map(q => q.domain)).size;
  }

  // Calculer la pagination
  calculatePagination() {
    this.totalPages = Math.ceil(this.filteredQuestions.length / this.itemsPerPage);
  }
 navigateToAddQuestion() {
    this.router.navigate(['/addquestion']);
  }
  // Filtrer les questions
  filterQuestions() {
    this.filteredQuestions = this.questions.filter(question => {
      const matchesSearch = question.questionText.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesDomain = !this.selectedDomain || question.domain === this.selectedDomain;
      const matchesDifficulty = !this.selectedDifficulty || question.difficultyLevel === this.selectedDifficulty;
      const matchesType = !this.selectedType || question.questionType === this.selectedType;
      
      // Filtre pour isActive (boolean)
      let matchesStatus = true;
      if (this.selectedStatus === 'true') {
        matchesStatus = question.isActive === true;
      } else if (this.selectedStatus === 'false') {
        matchesStatus = question.isActive === false;
      }
      
      return matchesSearch && matchesDomain && matchesDifficulty && matchesType && matchesStatus;
    });
    this.calculatePagination();
    this.currentPage = 1;
  }

  // Réinitialiser les filtres
  resetFilters() {
    this.searchTerm = '';
    this.selectedDomain = '';
    this.selectedDifficulty = '';
    this.selectedType = '';
    this.selectedStatus = '';
    this.filteredQuestions = [...this.questions];
    this.calculatePagination();
    this.currentPage = 1;
  }

  // Trier par date
  toggleSort() {
    this.filteredQuestions.sort((a, b) => {
      const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
      const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
      return dateB - dateA;
    });
  }

  // Classes CSS dynamiques
  getDomainClass(domain: string): string {
    return domain.toLowerCase();
  }

  getDifficultyClass(difficulty: string): string {
    return difficulty.toLowerCase();
  }

  getTypeClass(type: string): string {
    return type.toLowerCase().replace('_', '-');
  }

  // Formater le type de question pour l'affichage
  formatQuestionType(type: string): string {
    switch(type) {
      case 'QCM_SIMPLE':
        return 'QCM';
      case 'CODING':
        return 'Coding';
      case 'VRAI_FAUX':
        return 'True/False';
      default:
        return type;
    }
  }

  /** Taux de réussite (%) pour une question, ou null si pas encore de stats. */
  getSuccessRate(q: QuestionTest | null | undefined): number | null {
    if (!q || q.attemptsCount == null || q.attemptsCount === 0 || q.successCount == null) {
      return null;
    }
    return Math.round((q.successCount / q.attemptsCount) * 100);
  }

  /** Étiquette de qualité : Trop facile (≥90%), Trop difficile (≤20%), ou null. */
  getQualityLabel(q: QuestionTest | null | undefined): 'TROP_FACILE' | 'TROP_DIFFICILE' | null {
    const rate = this.getSuccessRate(q);
    if (rate == null) return null;
    if (rate >= 90) return 'TROP_FACILE';
    if (rate <= 20) return 'TROP_DIFFICILE';
    return null;
  }

  getQualityClass(q: QuestionTest | null | undefined): string {
    const label = this.getQualityLabel(q);
    if (label === 'TROP_FACILE') return 'quality-easy';
    if (label === 'TROP_DIFFICILE') return 'quality-hard';
    return '';
  }

  // Voir les détails de la question (ouvre le pop-up)
  viewQuestion(question: QuestionTest): void {
    if (question.id == null) return;
    this.detailQuestion = null;
    this.showDetailModal = true;
    this.detailLoading = true;
    this.questionService.getQuestionById(question.id).subscribe({
      next: (q) => {
        this.detailQuestion = q;
        this.detailLoading = false;
      },
      error: () => {
        this.detailLoading = false;
        this.detailQuestion = null;
      }
    });
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.detailQuestion = null;
  }

  /** Ferme le détail et ouvre l'édition pour la question affichée */
  editFromDetail(): void {
    const q = this.detailQuestion;
    this.closeDetailModal();
    if (q) this.editQuestion(q);
  }

  // Ouvrir le modal d'édition et charger la question
  editQuestion(question: QuestionTest): void {
    if (question.id == null) return;
    this.editingQuestionId = question.id;
    this.editErrorMessage = '';
    this.editSuccessMessage = '';
    this.showEditValidationErrors = false;
    this.editBackendErrors = [];
    this.questionService.getQuestionById(question.id).subscribe({
      next: (q) => {
        this.patchEditForm(q);
        this.showEditModal = true;
      },
      error: (err) => {
        console.error('Erreur chargement question', err);
        this.editErrorMessage = 'Impossible de charger la question.';
      }
    });
  }

  private patchEditForm(q: QuestionTest): void {
    this.editForm.patchValue({
      questionText: q.questionText ?? '',
      domain: q.domain ?? '',
      questionType: q.questionType ?? '',
      difficultyLevel: q.difficultyLevel ?? 'MEDIUM',
      duration: q.duration ?? 60,
      correctAnswer: q.correctAnswer ?? '',
      isActive: q.isActive !== false
    });
    const choicesArray = this.editForm.get('choices') as FormArray;
    choicesArray.clear();
    if (q.questionType === 'QCM_SIMPLE' && Array.isArray(q.choices) && q.choices.length > 0) {
      q.choices.forEach(c => choicesArray.push(this.fb.control(c ?? '', Validators.required)));
    } else if (q.questionType === 'QCM_SIMPLE') {
      this.addEditChoice();
      this.addEditChoice();
    }
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.editingQuestionId = null;
    this.editErrorMessage = '';
    this.editSuccessMessage = '';
    this.showEditValidationErrors = false;
    this.editBackendErrors = [];
  }

  saveEdit(): void {
    this.showEditValidationErrors = false;
    this.editErrorMessage = '';
    this.editBackendErrors = [];
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      this.showEditValidationErrors = true;
      return;
    }
    if (this.editingQuestionId == null) return;
    this.isSavingEdit = true;
    const raw = this.editForm.getRawValue();
    const payload: QuestionTest = {
      id: this.editingQuestionId,
      questionText: raw.questionText?.trim() ?? '',
      domain: raw.domain,
      questionType: raw.questionType,
      difficultyLevel: raw.difficultyLevel || 'MEDIUM',
      duration: raw.duration ? Number(raw.duration) : 60,
      choices: Array.isArray(raw.choices) ? raw.choices.filter((c: string) => c && String(c).trim()) : [],
      correctAnswer: (raw.correctAnswer && String(raw.correctAnswer).trim()) || '',
      isActive: raw.isActive !== false
    };
    this.questionService.updateQuestion(this.editingQuestionId, payload).subscribe({
      next: () => {
        this.isSavingEdit = false;
        this.editBackendErrors = [];
        this.editSuccessMessage = 'Question enregistrée.';
        this.loadQuestions();
        setTimeout(() => this.closeEditModal(), 800);
      },
      error: (err) => {
        this.isSavingEdit = false;
        this.editBackendErrors = Array.isArray(err?.error?.errors)
          ? err.error.errors.map((e: { field?: string; message?: string }) => ({
              field: e.field ?? '',
              message: e.message ?? 'Invalid value'
            }))
          : [];
        if (this.editBackendErrors.length > 0) {
          this.editErrorMessage = 'Please fix the errors shown below.';
        } else {
          this.editErrorMessage = err?.error?.message ?? 'Error saving question.';
        }
      }
    });
  }

  // Supprimer une question
  deleteQuestion(id: number) {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette question ?')) {
      this.questionService.deleteQuestion(id).subscribe({
        next: () => {
          console.log('Question supprimée avec succès');
          // Recharger la liste après suppression
          this.loadQuestions();
        },
        error: (error) => {
          console.error('Erreur lors de la suppression:', error);
          alert('Erreur lors de la suppression de la question');
        }
      });
    }
  }

  // Pagination
  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  previousPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  // Obtenir les questions paginées
  getPaginatedQuestions(): QuestionTest[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return this.filteredQuestions.slice(startIndex, endIndex);
  }

  setViewMode(mode: 'cards' | 'table'): void {
    this.viewMode = mode;
  }

  /** Aperçu court du texte (pour les cartes) */
  getQuestionPreview(text: string, maxLen: number = 90): string {
    if (!text || !text.trim()) return '—';
    const t = text.trim();
    return t.length <= maxLen ? t : t.slice(0, maxLen) + '…';
  }
}