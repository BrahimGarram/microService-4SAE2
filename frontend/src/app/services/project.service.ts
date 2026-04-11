import { Injectable } from '@angular/core';
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
 * Project Service
 * 
 * Provides methods to interact with the Project Microservice backend.
 */
@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  
  private apiUrl = 'http://localhost:8082/api/projects';  // Backend runs on port 8082
  
  constructor(private http: HttpClient) {}
  
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
}
