import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CollaborationPublicDto {
  id: number;
  type: string;
  priority: string;
  publishedAt: string;
  mediaUrl: string;
}

export interface CollaborationPublicGrouped {
  high: CollaborationPublicDto[];
  medium: CollaborationPublicDto[];
  low: CollaborationPublicDto[];
  all: CollaborationPublicDto[];
}

export interface CollaborationFullDto extends CollaborationPublicDto {
  userId: string;
  /** Nom utilisateur (JWT) enregistré à la soumission */
  userName?: string;
  status: string;
  active: boolean;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class CollaborationApiService {
  private readonly base = `${environment.apiGatewayUrl}/collaboration/api/collaborations`;

  constructor(private http: HttpClient) {}

  getPublic(): Observable<CollaborationPublicGrouped> {
    return this.http.get<CollaborationPublicGrouped>(`${this.base}/public`);
  }

  getMine(): Observable<CollaborationFullDto[]> {
    return this.http.get<CollaborationFullDto[]>(`${this.base}/mine`);
  }

  getPending(): Observable<CollaborationFullDto[]> {
    return this.http.get<CollaborationFullDto[]>(`${this.base}/pending`);
  }

  create(formData: FormData): Observable<CollaborationFullDto> {
    return this.http.post<CollaborationFullDto>(`${this.base}`, formData);
  }

  approve(id: number): Observable<CollaborationFullDto> {
    return this.http.patch<CollaborationFullDto>(`${this.base}/${id}/approve`, {});
  }

  reject(id: number): Observable<CollaborationFullDto> {
    return this.http.patch<CollaborationFullDto>(`${this.base}/${id}/reject`, {});
  }

  /** URL absolue pour <img> / <video> (média public une fois approuvé). */
  absoluteMediaUrl(relativePath: string): string {
    return `${environment.apiGatewayUrl}/collaboration${relativePath}`;
  }

  /** Aperçu admin (demandes PENDING). */
  absolutePreviewUrl(id: number): string {
    return `${environment.apiGatewayUrl}/collaboration/api/collaborations/preview/${id}`;
  }
}
