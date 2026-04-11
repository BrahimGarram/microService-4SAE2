import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FollowService {
  private apiUrl = `${environment.apiGatewayUrl}/api/follow`;

  constructor(private http: HttpClient) {}

  follow(userId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${userId}`, null);
  }

  unfollow(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${userId}`);
  }
}
