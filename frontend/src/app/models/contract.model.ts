export enum ContractStatus {
  ACTIVE = 'ACTIVE',
  PENDING = 'PENDING',
  EXPIRED = 'EXPIRED',
  CANCELLED = 'CANCELLED',
  DISPUTED = 'DISPUTED',
  COMPLETED = 'COMPLETED',
  RENEWED = 'RENEWED'
}

export enum PaymentType {
  HOURLY = 'HOURLY',
  FIXED_PRICE = 'FIXED_PRICE',
  MILESTONE = 'MILESTONE',
  MONTHLY = 'MONTHLY',
  WEEKLY = 'WEEKLY'
}

export enum ContractType {
  FULL_TIME = 'FULL_TIME',
  PART_TIME = 'PART_TIME',
  PROJECT_BASED = 'PROJECT_BASED',
  RETAINER = 'RETAINER',
  CONSULTING = 'CONSULTING'
}

export interface Contract {
  idContract?: number;
  contractTitle: string;
  freelancerName: string;
  freelancerEmail: string;
  clientName: string;
  clientEmail: string;
  projectDescription?: string;
  startDate: Date | string;
  endDate: Date | string;
  status: ContractStatus;
  contractAmount: number;
  paymentType: PaymentType;
  currency?: string;
  contractType?: ContractType;
  deliverables?: string;
  termsAndConditions?: string;
  paymentTermsDays?: number;
  createdAt?: Date | string;
  updatedAt?: Date | string;
  createdBy?: string;
}
