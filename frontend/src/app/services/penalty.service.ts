import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Penalty {
  idPenalty: number;
  userId: number;
  complaintId?: number;
  penaltyType: 'WARNING' | 'ACCOUNT_RESTRICTION' | 'TEMPORARY_SUSPENSION' | 'FINE' | 'PERMANENT_BAN';
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  reason: string;
  description: string;
  appliedAt: string;
  expiresAt?: string;
  isActive: boolean;
  appliedByAdminId?: number;
  fineAmount?: number;
  ruleName: string;
}

@Injectable({
  providedIn: 'root'
})
export class PenaltyService {
  private apiUrl = environment.complaintsServiceUrl + '/report/penalties';

  constructor(private http: HttpClient) {}

  /**
   * Get all active penalties for a user
   */
  getActivePenalties(userId: number): Observable<Penalty[]> {
    return this.http.get<Penalty[]>(`${this.apiUrl}/user/${userId}`);
  }

  /**
   * Check if user has any active penalties
   */
  hasActivePenalties(userId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/check?userId=${userId}`);
  }

  /**
   * Evaluate penalty rules for a user (triggers rule check)
   */
  evaluateRules(userId: number): Observable<Penalty[]> {
    return this.http.get<Penalty[]>(`${this.apiUrl}/evaluate?userId=${userId}`);
  }

  /**
   * Apply manual penalty (admin only)
   */
  applyManualPenalty(request: {
    userId: number;
    complaintId?: number;
    type: string;
    severity: string;
    reason: string;
    description: string;
    adminId: number;
    daysToExpire?: number | null;
    fineAmount?: number | null;
  }): Observable<Penalty> {
    return this.http.post<Penalty>(`${this.apiUrl}/apply`, request);
  }

  /**
   * Deactivate a penalty (admin only)
   */
  deactivatePenalty(penaltyId: number): Observable<Penalty> {
    return this.http.post<Penalty>(`${this.apiUrl}/deactivate/${penaltyId}`, {});
  }
}
