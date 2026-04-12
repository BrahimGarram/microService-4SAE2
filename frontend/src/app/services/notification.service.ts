import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Notification {
  id: number;
  targetUserId: number;
  senderUserId?: number;
  type: string;
  message: string;
  relatedId?: number;
  read: boolean;
  createdAt: string;
}

export interface CreateNotificationRequest {
  targetUserId: number;
  type: string;
  message: string;
  relatedId?: number;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = `${environment.apiGatewayUrl}/api/notifications`;

  constructor(private http: HttpClient) {}

  getMyNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(this.apiUrl);
  }

  getUnreadCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/unread-count`);
  }

  markAsRead(id: number): Observable<Notification> {
    return this.http.put<Notification>(`${this.apiUrl}/${id}/read`, null);
  }

  create(request: CreateNotificationRequest): Observable<Notification> {
    return this.http.post<Notification>(this.apiUrl, request);
  }
}
