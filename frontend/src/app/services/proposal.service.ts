import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type ProposalStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'NEGOTIATING';

export interface Proposal {
  id: number;
  projectId: number;
  freelancerId: number;
  amount: number;
  proposedDeadline?: string;
  message?: string;
  status: ProposalStatus;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateProposalRequest {
  projectId: number;
  freelancerId: number;
  amount: number;
  proposedDeadline?: string;
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class ProposalService {
  private apiUrl = `${environment.apiGatewayUrl}/api/proposals`;

  constructor(private http: HttpClient) {}

  create(request: CreateProposalRequest): Observable<Proposal> {
    return this.http.post<Proposal>(this.apiUrl, request);
  }

  getByProject(projectId: number): Observable<Proposal[]> {
    return this.http.get<Proposal[]>(`${this.apiUrl}/project/${projectId}`);
  }

  getById(id: number): Observable<Proposal> {
    return this.http.get<Proposal>(`${this.apiUrl}/${id}`);
  }

  getMyProposals(freelancerId: number): Observable<Proposal[]> {
    return this.http.get<Proposal[]>(`${this.apiUrl}/my`, { params: { freelancerId: String(freelancerId) } });
  }

  accept(proposalId: number, ownerId: number): Observable<Proposal> {
    return this.http.put<Proposal>(`${this.apiUrl}/${proposalId}/accept`, null, {
      params: { ownerId: String(ownerId) }
    });
  }

  reject(proposalId: number, ownerId: number): Observable<Proposal> {
    return this.http.put<Proposal>(`${this.apiUrl}/${proposalId}/reject`, null, {
      params: { ownerId: String(ownerId) }
    });
  }

  getStatusDisplay(status: ProposalStatus): string {
    const map: Record<ProposalStatus, string> = {
      PENDING: 'En attente',
      ACCEPTED: 'Acceptée',
      REJECTED: 'Refusée',
      NEGOTIATING: 'En négociation'
    };
    return map[status] || status;
  }
}
