import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Contract, ContractStatus } from '../models/contract.model';

@Injectable({
  providedIn: 'root'
})
export class ContractService {
  //private apiUrl = 'http://localhost:9090/api/contracts'; // Update this if your backend runs on a different port
private apiUrl = 'http://localhost:8083/contracts';

  constructor(private http: HttpClient) { }

  getAllContracts(): Observable<Contract[]> {
    return this.http.get<Contract[]>(this.apiUrl);
  }

  getContractById(id: number): Observable<Contract> {
    return this.http.get<Contract>(`${this.apiUrl}/${id}`);
  }

  getContractsByStatus(status: ContractStatus): Observable<Contract[]> {
    return this.http.get<Contract[]>(`${this.apiUrl}/status/${status}`);
  }

  createContract(contract: Contract): Observable<Contract> {
    // Remove idContract when creating a new contract
    const { idContract, createdAt, updatedAt, createdBy, ...contractData } = contract;
    return this.http.post<Contract>(this.apiUrl, contractData);
  }

  updateContract(id: number, contract: Contract): Observable<Contract> {
    return this.http.put<Contract>(`${this.apiUrl}/${id}`, contract);
  }

  deleteContract(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // ── Métier endpoints ──────────────────────────────────────────────────────

  activateContract(id: number): Observable<Contract> {
    return this.http.patch<Contract>(`${this.apiUrl}/${id}/activate`, {});
  }

  completeContract(id: number): Observable<Contract> {
    return this.http.patch<Contract>(`${this.apiUrl}/${id}/complete`, {});
  }

  cancelContract(id: number, reason?: string): Observable<Contract> {
    const params = reason ? `?reason=${encodeURIComponent(reason)}` : '';
    return this.http.patch<Contract>(`${this.apiUrl}/${id}/cancel${params}`, {});
  }

  renewContract(id: number, dto: { newEndDate: string; newAmount?: number; renewalNote?: string }): Observable<Contract> {
    return this.http.post<Contract>(`${this.apiUrl}/${id}/renew`, dto);
  }

  raiseDispute(id: number, dto: { reason: string; raisedBy: string; description: string }): Observable<Contract> {
    return this.http.post<Contract>(`${this.apiUrl}/${id}/dispute`, dto);
  }

  resolveDispute(id: number, resolution: string): Observable<Contract> {
    return this.http.patch<Contract>(`${this.apiUrl}/${id}/dispute/resolve?resolution=${encodeURIComponent(resolution)}`, {});
  }

  duplicateContract(id: number): Observable<Contract> {
    return this.http.post<Contract>(`${this.apiUrl}/${id}/duplicate`, {});
  }

  getContractSummary(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}/summary`);
  }

  getFreelancerAnalytics(email: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/analytics/freelancer/${email}`);
  }

  getClientAnalytics(email: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/analytics/client/${email}`);
  }

  getStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/stats`);
  }
}
