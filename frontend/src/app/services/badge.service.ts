import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Badge, LeaderboardEntry, FreelancerStats } from '../models/badge.model';

@Injectable({ providedIn: 'root' })
export class BadgeService {
  private apiUrl = 'http://localhost:8085/api/badges';

  constructor(private http: HttpClient) {}

  // Admin: badge CRUD
  getAllBadges(): Observable<Badge[]> {
    return this.http.get<Badge[]>(this.apiUrl);
  }

  createBadge(badge: Badge): Observable<Badge> {
    return this.http.post<Badge>(this.apiUrl, badge);
  }

  updateBadge(id: number, badge: Badge): Observable<Badge> {
    return this.http.put<Badge>(`${this.apiUrl}/${id}`, badge);
  }

  deleteBadge(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Stats update
  updateFreelancerStats(stats: FreelancerStats): Observable<LeaderboardEntry> {
    return this.http.post<LeaderboardEntry>(`${this.apiUrl}/update-stats`, stats);
  }

  // Leaderboard
  getLeaderboard(): Observable<LeaderboardEntry[]> {
    return this.http.get<LeaderboardEntry[]>(`${this.apiUrl}/leaderboard`);
  }

  getFreelancerProfile(email: string): Observable<LeaderboardEntry> {
    return this.http.get<LeaderboardEntry>(`${this.apiUrl}/freelancer/${encodeURIComponent(email)}`);
  }
}
