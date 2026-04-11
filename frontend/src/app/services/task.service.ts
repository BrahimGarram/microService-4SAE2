import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export enum TaskStatus {
  TO_DO = 'TO_DO',
  IN_PROGRESS = 'IN_PROGRESS',
  DONE = 'DONE'
}

export interface Task {
  id?: number;
  title: string;
  description?: string;
  status: TaskStatus;
  projectId: number;
  assignedTo?: number;
  assignedToName?: string;
  createdBy: number;
  createdByName?: string;
  dueDate?: string;
  completedAt?: string;
  priority: number; // 0 = Low, 1 = Medium, 2 = High
  orderIndex: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  projectId: number;
  assignedTo?: number;
  createdBy: number;
  dueDate?: string;
  priority?: number;
  orderIndex?: number;
}

export interface UpdateTaskRequest {
  title?: string;
  description?: string;
  status?: TaskStatus;
  assignedTo?: number;
  dueDate?: string;
  priority?: number;
  orderIndex?: number;
}

export interface TaskStatistics {
  total: number;
  toDo: number;
  inProgress: number;
  done: number;
  completionRate: number;
}

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private apiUrl = 'http://localhost:8082/api/tasks';

  constructor(private http: HttpClient) {}

  /**
   * Create a new task
   */
  createTask(request: CreateTaskRequest): Observable<Task> {
    return this.http.post<Task>(this.apiUrl, request);
  }

  /**
   * Get all tasks for a project
   */
  getTasksByProject(projectId: number): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/project/${projectId}`);
  }

  /**
   * Get tasks by project and status
   */
  getTasksByProjectAndStatus(projectId: number, status: TaskStatus): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/project/${projectId}/status/${status}`);
  }

  /**
   * Get task by ID
   */
  getTaskById(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/${id}`);
  }

  /**
   * Update a task
   */
  updateTask(id: number, request: UpdateTaskRequest): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Delete a task
   */
  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get task statistics for a project
   */
  getTaskStatistics(projectId: number): Observable<TaskStatistics> {
    return this.http.get<TaskStatistics>(`${this.apiUrl}/project/${projectId}/statistics`);
  }

  /**
   * Get status display name
   */
  getStatusDisplayName(status: TaskStatus): string {
    const statusMap: { [key in TaskStatus]: string } = {
      [TaskStatus.TO_DO]: 'To Do',
      [TaskStatus.IN_PROGRESS]: 'In Progress',
      [TaskStatus.DONE]: 'Done'
    };
    return statusMap[status] || status;
  }

  /**
   * Get priority display name
   */
  getPriorityDisplayName(priority: number): string {
    const priorityMap: { [key: number]: string } = {
      0: 'Low',
      1: 'Medium',
      2: 'High'
    };
    return priorityMap[priority] || 'Low';
  }

  /**
   * Get priority color class
   */
  getPriorityColorClass(priority: number): string {
    const colorMap: { [key: number]: string } = {
      0: 'priority-low',
      1: 'priority-medium',
      2: 'priority-high'
    };
    return colorMap[priority] || 'priority-low';
  }
}
