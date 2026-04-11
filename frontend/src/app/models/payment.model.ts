export enum PaymentStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED'
}

export interface Payment {
  idPayment?: number;
  amount: number;
  paymentDate: Date | string;
  status: PaymentStatus;
  contractId?: number;
  userId?: number;
  paymentMethod?: string;
  transactionId?: string;
  description?: string;
  createdAt?: Date | string;
  updatedAt?: Date | string;
}
