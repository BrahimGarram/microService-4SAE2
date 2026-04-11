import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Payment, PaymentStatus } from '../models/payment.model';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
 // private apiUrl = 'http://localhost:9090/api/payments';
private apiUrl = 'http://localhost:8083/payments';

  constructor(private http: HttpClient) { }

  getAllPayments(): Observable<Payment[]> {
    return this.http.get<Payment[]>(this.apiUrl);
  }

  getPaymentById(id: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.apiUrl}/${id}`);
  }

  getPaymentsByStatus(status: PaymentStatus): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.apiUrl}/status/${status}`);
  }

  getPaymentsByContract(contractId: number): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.apiUrl}/contract/${contractId}`);
  }

  createPayment(payment: Payment): Observable<Payment> {
    const { idPayment, createdAt, updatedAt, ...paymentData } = payment;
    return this.http.post<Payment>(this.apiUrl, paymentData);
  }

  updatePayment(id: number, payment: Payment): Observable<Payment> {
    return this.http.put<Payment>(`${this.apiUrl}/${id}`, payment);
  }

  deletePayment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getStats(year: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/stats?year=${year}`);
  }
}
