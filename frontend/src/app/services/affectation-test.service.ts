import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
const API_BASE = `${environment.apiGatewayUrl}/service-test/api/affectations`;

/** URL de base des affectations (pour fetch keepalive depuis outside Angular, ex. beforeunload). */
export const AFFECTATIONS_API_BASE = API_BASE;

export type Status = 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED' | 'FRAUD';

export interface AffectationQuestion {
  id?: number;
  questionText?: string;
  /** Type: QCM_SIMPLE | VRAI_FAUX | CODING (API peut renvoyer questionType ou question_type) */
  questionType?: string;
  choices?: string[];
  correctAnswer?: string;
  duration?: number;
  durationInSeconds?: number;
  /** CODING: langage (java, python, javascript) */
  language?: string;
  /** CODING: code de départ */
  starterCode?: string;
  /** Champs possibles en snake_case si l'API/proxy les renvoie ainsi */
  question_type?: string;
  question_text?: string;
  starter_code?: string;
}

export interface AffectationTest {
  id?: number;
  freelancerId?: number;
  freelancerFirstName?: string;
  freelancerLastName?: string;
  assignedAt?: string;
  testDate?: string;
  score?: number;
  isValidated?: boolean;
  correctAnswersCount?: number;
  totalQuestions?: number;
  timeSpent?: number;
  status?: Status;
  domainScoresJson?: string;
  questions?: AffectationQuestion[];
}

@Injectable({
  providedIn: 'root'
})
export class AffectationTestService {

  constructor(private http: HttpClient) {}

  /** Liste toutes les affectations d'un freelancer */
  getAffectationsByFreelancerId(freelancerId: number): Observable<AffectationTest[]> {
    return this.http.get<AffectationTest[]>(`${API_BASE}/by-freelancer/${freelancerId}`);
  }

  /** Récupère une affectation par son id */
  getAffectationById(id: number): Observable<AffectationTest> {
    return this.http.get<AffectationTest>(`${API_BASE}/by-id/${id}`);
  }

  /** Démarre un test pour le freelancer (backend crée l'affectation + assigne les questions) */
  startTest(freelancerId: number): Observable<AffectationTest> {
    return this.http.post<AffectationTest>(`${API_BASE}/start`, { freelancerId });
  }

  /** Crée une affectation (si besoin manuel) */
  createAffectation(affectation: AffectationTest): Observable<AffectationTest> {
    return this.http.post<AffectationTest>(API_BASE, affectation);
  }

  /** Met à jour une affectation (score, isValidated, etc.) */
  updateAffectation(id: number, affectation: AffectationTest): Observable<AffectationTest> {
    return this.http.put<AffectationTest>(`${API_BASE}/${id}`, affectation);
  }

  deleteAffectation(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/${id}`);
  }

  /** Marque l'affectation comme abandonnée (freelancer a quitté sans soumettre). */
  abandonTest(id: number): Observable<AffectationTest> {
    return this.http.patch<AffectationTest>(`${API_BASE}/${id}/abandon`, {});
  }

  /** Signale une fraude (ex. visage ne correspond pas à la photo de profil). */
  reportFraud(id: number): Observable<AffectationTest> {
    return this.http.patch<AffectationTest>(`${API_BASE}/${id}/fraud`, {});
  }

  /** Exécute le code pour une question CODING (bouton Exécuter). */
  runCode(questionId: number, code: string): Observable<RunCodeResult> {
    return this.http.post<RunCodeResult>(`${environment.apiGatewayUrl}/service-test/api/code/run`, {
      questionId,
      code
    });
  }

  /** Soumission du test avec réponses ; le backend calcule le score (QCM + CODING).
   *  Optionnellement, on envoie l'email et le nom du freelancer pour le mail de récapitulatif. */
  submitTest(
    affectationId: number,
    timeSpent: number,
    answers: { questionId: number; answer: string }[],
    freelancerEmail?: string,
    freelancerName?: string
  ): Observable<AffectationTest> {
    return this.http.post<AffectationTest>(`${API_BASE}/${affectationId}/submit`, {
      timeSpent,
      answers,
      freelancerEmail,
      freelancerName
    });
  }

  /** Demande un feedback IA (Gemini) pour une affectation, avec message optionnel du freelancer. */
  getAiFeedback(affectationId: number, message?: string): Observable<{ feedback: string }> {
    return this.http.post<{ feedback: string }>(
      `${API_BASE}/${affectationId}/ai/feedback`,
      message ? { message } : {}
    );
  }

  /** Liste toutes les affectations (admin) */
  getAllAffectations(): Observable<AffectationTest[]> {
    return this.http.get<AffectationTest[]>(API_BASE);
  }

  /** Stats : nombre avec score >= 50% */
  getStatsScoreSuccess(): Observable<number> {
    return this.http.get<number>(`${API_BASE}/stats/score/success`);
  }
  /** Stats : nombre avec score < 50% */
  getStatsScoreFail(): Observable<number> {
    return this.http.get<number>(`${API_BASE}/stats/score/fail`);
  }
  /** Stats : nombre validés (isValidated = true) */
  getStatsValidatedTrue(): Observable<number> {
    return this.http.get<number>(`${API_BASE}/stats/validated/true`);
  }
  /** Stats : nombre non validés (isValidated = false) */
  getStatsValidatedFalse(): Observable<number> {
    return this.http.get<number>(`${API_BASE}/stats/validated/false`);
  }
  /** Stats : freelancers validés dès la 1ère tentative */
  getStatsValidatedFirstTime(): Observable<number> {
    return this.http.get<number>(`${API_BASE}/stats/validated-first-time`);
  }
  /** Stats : nombre par statut (PENDING, ACTIVE, INACTIVE, DELETED) */
  getStatsByStatus(status: Status): Observable<number> {
    return this.http.get<number>(`${API_BASE}/stats/status/${status}`);
  }
}

/** Résultat d'exécution de code (jamais la sortie attendue). */
export interface RunCodeResult {
  success: boolean;
  score: number;
  passedCount: number;
  totalCount: number;
  stdout?: string;
  stderr?: string;
  message?: string;
  diagnostics?: { line: number; message: string; severity: string }[];
}
