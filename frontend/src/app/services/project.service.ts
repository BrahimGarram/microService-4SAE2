import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Project Interface
 * Matches the ProjectDTO from backend
 */
export interface Project {
  id?: number;
  title: string;
  description?: string;
  status: 'DRAFT' | 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  ownerId: number;
  budget?: number;
  deadline?: string;
  category?: string;
  imageUrl?: string;
  tags?: string;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Create Project Request Interface
 */
export interface CreateProjectRequest {
  title: string;
  description?: string;
  status?: 'DRAFT' | 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  ownerId: number;
  budget?: number;
  deadline?: string;
  category?: string;
  imageUrl?: string;
  tags?: string;
}

/**
 * Update Project Request Interface
 */
export interface UpdateProjectRequest {
  title?: string;
  description?: string;
  status?: 'DRAFT' | 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  budget?: number;
  deadline?: string;
  category?: string;
  imageUrl?: string;
  tags?: string;
}

/**
 * AI-generated project draft (from natural language).
 */
export interface ProjectDraftDTO {
  title: string;
  description: string;
  category: string;
  tags: string;
  suggestedBudget?: number | null;
}

/**
 * Project statistics for backoffice dashboard.
 */
export interface ProjectStats {
  total: number;
  byStatus: { [key: string]: number };
  byCategory: { [key: string]: number };
  totalBudget: number;
  averageBudget: number;
  completionRate: number;
}

/** Token costs for project/proposal actions (from backend config). */
export interface TokenCosts {
  tokensPerProject: number;
  tokensPerProposal: number;
}

/**
 * Project Service
 * 
 * Provides methods to interact with the Project Microservice backend.
 */
@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  
  private apiUrl = `${environment.apiGatewayUrl}/project`;
  
  constructor(private http: HttpClient) {}

  /**
   * Get token costs for project creation and proposal submission.
   * GET /api/projects/config/token-costs
   */
  getTokenCosts(): Observable<TokenCosts> {
    return this.http.get<TokenCosts>(`${this.apiUrl}/config/token-costs`);
  }
  
  /**
   * Create a new project
   * POST /api/projects
   */
  createProject(request: CreateProjectRequest): Observable<Project> {
    return this.http.post<Project>(this.apiUrl, request);
  }
  
  /**
   * Get project by ID
   * GET /api/projects/{id}
   */
  getProjectById(id: number): Observable<Project> {
    return this.http.get<Project>(`${this.apiUrl}/${id}`);
  }
  
  /**
   * Get all projects
   * GET /api/projects
   */
  getAllProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(this.apiUrl);
  }

  /**
   * Get project statistics for backoffice
   * GET /api/projects/stats
   */
  getStatistics(): Observable<ProjectStats> {
    return this.http.get<ProjectStats>(`${this.apiUrl}/stats`);
  }
  
  /**
   * Get projects by owner ID
   * GET /api/projects/owner/{ownerId}
   */
  getProjectsByOwner(ownerId: number): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/owner/${ownerId}`);
  }
  
  /**
   * Get projects by status
   * GET /api/projects/status/{status}
   */
  getProjectsByStatus(status: string): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/status/${status}`);
  }
  
  /**
   * Get projects by category
   * GET /api/projects/category/{category}
   */
  getProjectsByCategory(category: string): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/category/${category}`);
  }
  
  /**
   * Update project
   * PUT /api/projects/{id}
   */
  updateProject(id: number, request: UpdateProjectRequest): Observable<Project> {
    return this.http.put<Project>(`${this.apiUrl}/${id}`, request);
  }
  
  /**
   * Delete project
   * DELETE /api/projects/{id}
   */
  deleteProject(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Generate project draft from natural language (Ollama).
   * POST /api/projects/ai/draft-from-text
   */
  getDraftFromText(userMessage: string): Observable<ProjectDraftDTO> {
    return this.http.post<ProjectDraftDTO>(`${this.apiUrl}/ai/draft-from-text`, { userMessage });
  }
}
