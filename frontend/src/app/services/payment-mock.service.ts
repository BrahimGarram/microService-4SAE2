import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';
import { Payment, PaymentStatus } from '../models/payment.model';

@Injectable({
  providedIn: 'root'
})
export class PaymentMockService {
  private payments: Payment[] = [
    {
      idPayment: 1,
      amount: 5000,
      paymentDate: '2024-02-15',
      status: PaymentStatus.COMPLETED,
      contractId: 1,
      userId: 1,
      paymentMethod: 'Bank Transfer',
      transactionId: 'TXN001',
      description: 'Initial payment for web development project',
      createdAt: '2024-02-15T10:00:00',
      updatedAt: '2024-02-15T10:00:00'
    },
    {
      idPayment: 2,
      amount: 3500,
      paymentDate: '2024-02-20',
      status: PaymentStatus.COMPLETED,
      contractId: 2,
      userId: 2,
      paymentMethod: 'Credit Card',
      transactionId: 'TXN002',
      description: 'Monthly retainer payment',
      createdAt: '2024-02-20T14:30:00',
      updatedAt: '2024-02-20T14:30:00'
    },
    {
      idPayment: 3,
      amount: 2000,
      paymentDate: '2024-02-22',
      status: PaymentStatus.PENDING,
      contractId: 3,
      userId: 3,
      paymentMethod: 'PayPal',
      transactionId: 'TXN003',
      description: 'Milestone 1 payment',
      createdAt: '2024-02-22T09:15:00',
      updatedAt: '2024-02-22T09:15:00'
    },
    {
      idPayment: 4,
      amount: 1500,
      paymentDate: '2024-02-18',
      status: PaymentStatus.FAILED,
      contractId: 4,
      userId: 4,
      paymentMethod: 'Credit Card',
      transactionId: 'TXN004',
      description: 'Payment failed - insufficient funds',
      createdAt: '2024-02-18T16:45:00',
      updatedAt: '2024-02-18T16:45:00'
    },
    {
      idPayment: 5,
      amount: 4200,
      paymentDate: '2024-02-25',
      status: PaymentStatus.COMPLETED,
      contractId: 5,
      userId: 5,
      paymentMethod: 'Bank Transfer',
      transactionId: 'TXN005',
      description: 'Final project payment',
      createdAt: '2024-02-25T11:20:00',
      updatedAt: '2024-02-25T11:20:00'
    }
  ];
  private nextId = 6;

  getAllPayments(): Observable<Payment[]> {
    return of([...this.payments]).pipe(delay(300));
  }

  getPaymentById(id: number): Observable<Payment> {
    const payment = this.payments.find(p => p.idPayment === id);
    return payment 
      ? of(payment).pipe(delay(300))
      : throwError(() => new Error('Payment not found'));
  }

  getPaymentsByStatus(status: PaymentStatus): Observable<Payment[]> {
    const filtered = this.payments.filter(p => p.status === status);
    return of(filtered).pipe(delay(300));
  }

  getPaymentsByContract(contractId: number): Observable<Payment[]> {
    const filtered = this.payments.filter(p => p.contractId === contractId);
    return of(filtered).pipe(delay(300));
  }

  createPayment(payment: Payment): Observable<Payment> {
    const newPayment = {
      ...payment,
      idPayment: this.nextId++,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    this.payments.push(newPayment);
    return of(newPayment).pipe(delay(300));
  }

  updatePayment(id: number, payment: Payment): Observable<Payment> {
    const index = this.payments.findIndex(p => p.idPayment === id);
    if (index === -1) {
      return throwError(() => new Error('Payment not found'));
    }
    const updated = {
      ...payment,
      idPayment: id,
      updatedAt: new Date().toISOString()
    };
    this.payments[index] = updated;
    return of(updated).pipe(delay(300));
  }

  deletePayment(id: number): Observable<void> {
    const index = this.payments.findIndex(p => p.idPayment === id);
    if (index === -1) {
      return throwError(() => new Error('Payment not found'));
    }
    this.payments.splice(index, 1);
    return of(void 0).pipe(delay(300));
  }
}
