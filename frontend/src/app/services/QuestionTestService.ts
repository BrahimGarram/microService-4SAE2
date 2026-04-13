import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface QuestionTest {
  id?: number;
  questionText: string;
  domain: string;
  questionType: string;
  difficultyLevel: string;
  duration: number;
  choices: string[];
  correctAnswer: string;
  isActive: boolean;
  language?: string;
  starterCode?: string;
  testCasesJson?: string;
  createdAt?: Date;
   attemptsCount?: number;
   successCount?: number;
   avgTimeSeconds?: number;
}

export interface AiQuestionGenerateRequest {
  prompt: string;
  numberOfQuestions?: number | null;
  domains?: string[] | null;
}

export interface AiGeneratedQuestionDto {
  questionText: string;
  domain: string;
  questionType: string;
  difficultyLevel: string;
  duration?: number;
  choices?: string[];
  correctAnswer?: string;
  isActive?: boolean;
  language?: string;
  starterCode?: string;
  testCasesJson?: string;
}

export interface AiQuestionGenerateResponse {
  success: boolean;
  message: string;
  needsNumberOfQuestions: boolean;
  questions: AiGeneratedQuestionDto[];
}

@Injectable({
  providedIn: 'root'
})
export class QuestionTestService {
  private baseUrl = `${environment.apiGatewayUrl}/service-test/questions`;

  constructor(private http: HttpClient) { }

  // CREATE - Créer une nouvelle question
  createQuestion(question: QuestionTest): Observable<QuestionTest> {
    return this.http.post<QuestionTest>(this.baseUrl, question);
  }

  // READ ALL - Récupérer toutes les questions
  getAllQuestions(): Observable<QuestionTest[]> {
    return this.http.get<QuestionTest[]>(this.baseUrl);
  }

  // READ BY ID - Récupérer une question par ID
  getQuestionById(id: number): Observable<QuestionTest> {
    return this.http.get<QuestionTest>(`${this.baseUrl}/${id}`);
  }

  // UPDATE - Mettre à jour une question
  updateQuestion(id: number, question: QuestionTest): Observable<QuestionTest> {
    return this.http.put<QuestionTest>(`${this.baseUrl}/${id}`, question);
  }

  // DELETE - Supprimer une question
  deleteQuestion(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  // GET BY DOMAINS - Récupérer les questions par domaines
  getQuestionsByDomains(): Observable<QuestionTest[]> {
    return this.http.get<QuestionTest[]>(`${this.baseUrl}/getbydomains`);
  }

  /** Appelle l'agent IA (microservice Python via Spring) pour générer des questions. */
  generateQuestionsWithAi(payload: AiQuestionGenerateRequest): Observable<AiQuestionGenerateResponse> {
    return this.http.post<AiQuestionGenerateResponse>(`${this.baseUrl}/ai/generate`, payload);
  }
}