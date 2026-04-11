import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Contract, ContractStatus, PaymentType, ContractType } from '../../models/contract.model';
import { ContractService } from '../../services/contract.service';
import { PaymentService } from '../../services/payment.service';
import { ContractPdfService, ContractPdfData } from '../../services/contract-pdf.service';
import { ToastService } from '../../services/toast.service';
import { UserService, User } from '../../services/user.service';

@Component({
  selector: 'app-my-contracts',
  templateUrl: './my-contracts.component.html',
  styleUrls: ['./my-contracts.component.css']
})
export class MyContractsComponent implements OnInit {
  contracts: Contract[] = [];
  filteredContracts: Contract[] = [];
  loading = false;
  currentUser: User | null = null;
  userRole: 'USER' | 'FREELANCER' | 'ADMIN' | null = null;
  searchTerm = '';
  filterStatus: ContractStatus | 'ALL' = 'ALL';
  contractStatuses = Object.values(ContractStatus);
  paymentTypes = Object.values(PaymentType);
  contractTypes = Object.values(ContractType);
  currencies = ['USD', 'EUR', 'GBP', 'MAD', 'CAD'];

  // Freelancer: create contract modal
  showCreateModal = false;
  newContract: Contract = this.emptyContract();
  errors: { [key: string]: string } = {};

  // Client: sign modal
  showSignModal = false;
  contractToSign: Contract | null = null;
  showSignatureModal = false;
  clientSignature = '';
  currentSignerName = '';

  // Client: pay modal
  showPayModal = false;
  contractToPay: Contract | null = null;

  // Métier modals
  showDisputeModal = false;
  showRenewModal = false;
  selectedContractForAction: Contract | null = null;
  disputeForm = { raisedBy: 'FREELANCER', reason: '', description: '' };
  renewForm: { newEndDate: string; newAmount: number | null; renewalNote: string } =
    { newEndDate: '', newAmount: null, renewalNote: '' };
  paymentMethod = 'Credit Card';
  paymentMethods = ['Credit Card', 'Bank Transfer', 'PayPal'];
  paymentProcessing = false;

  // Card fields
  cardNumber = '';
  cardHolder = '';
  cardExpiry = '';
  cardCvv = '';
  cardErrors: { [key: string]: string } = {};

  // Bank transfer fields
  bankSenderName = '';
  bankTransferDate = '';

  // Test cards panel
  showTestCards = false;

  stripeTestCards = [
    // Success cards
    { brand: 'Visa', number: '4242 4242 4242 4242', expiry: '12/34', cvv: '123', icon: 'fa-cc-visa',       description: 'Success', type: 'tc-success' },
    { brand: 'Visa (debit)', number: '4000 0566 5566 5556', expiry: '12/34', cvv: '123', icon: 'fa-cc-visa', description: 'Success', type: 'tc-success' },
    { brand: 'Mastercard', number: '5555 5555 5555 4444', expiry: '12/34', cvv: '123', icon: 'fa-cc-mastercard', description: 'Success', type: 'tc-success' },
    { brand: 'Mastercard (2-series)', number: '2223 0031 2200 3222', expiry: '12/34', cvv: '123', icon: 'fa-cc-mastercard', description: 'Success', type: 'tc-success' },
    { brand: 'Amex', number: '3782 822463 10005', expiry: '12/34', cvv: '1234', icon: 'fa-cc-amex', description: 'Success', type: 'tc-success' },
    { brand: 'Amex (2)', number: '3714 496353 98431', expiry: '12/34', cvv: '1234', icon: 'fa-cc-amex', description: 'Success', type: 'tc-success' },
    { brand: 'Discover', number: '6011 1111 1111 1117', expiry: '12/34', cvv: '123', icon: 'fa-cc-discover', description: 'Success', type: 'tc-success' },
    { brand: 'JCB', number: '3566 0020 2036 0505', expiry: '12/34', cvv: '123', icon: 'fa-credit-card', description: 'Success', type: 'tc-success' },
    // Decline cards
    { brand: 'Visa', number: '4000 0000 0000 0002', expiry: '12/34', cvv: '123', icon: 'fa-cc-visa', description: 'Declined', type: 'tc-decline' },
    { brand: 'Visa', number: '4000 0000 0000 9995', expiry: '12/34', cvv: '123', icon: 'fa-cc-visa', description: 'Insufficient funds', type: 'tc-decline' },
    { brand: 'Visa', number: '4000 0000 0000 0069', expiry: '12/34', cvv: '123', icon: 'fa-cc-visa', description: 'Expired card', type: 'tc-decline' },
    { brand: 'Visa', number: '4000 0000 0000 0127', expiry: '12/34', cvv: '123', icon: 'fa-cc-visa', description: 'Incorrect CVC', type: 'tc-decline' },
    { brand: 'Visa', number: '4000 0000 0000 0119', expiry: '12/34', cvv: '123', icon: 'fa-cc-visa', description: 'Processing error', type: 'tc-decline' },
    // 3DS cards
    { brand: 'Visa (3DS)', number: '4000 0027 6000 3184', expiry: '12/34', cvv: '123', icon: 'fa-cc-visa', description: '3DS Required', type: 'tc-3ds' },
    { brand: 'Visa (3DS)', number: '4000 0000 0000 3220', expiry: '12/34', cvv: '123', icon: 'fa-cc-visa', description: '3DS Always', type: 'tc-3ds' },
  ];

  constructor(
    private contractService: ContractService,
    private paymentService: PaymentService,
    private contractPdfService: ContractPdfService,
    private toastService: ToastService,
    private userService: UserService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.loadUser();
  }

  loadUser(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.userRole = user.role;
        this.loadContracts();
      },
      error: () => {
        // fallback: load all contracts
        this.loadContracts();
      }
    });
  }

  loadContracts(): void {
    this.loading = true;
    this.contractService.getAllContracts().subscribe({
      next: (data) => {
        this.contracts = data;
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.toastService.error('Failed to load contracts');
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.filteredContracts = this.contracts.filter(c => {
      const matchSearch = !this.searchTerm ||
        c.contractTitle?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        c.freelancerName?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        c.clientName?.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchStatus = this.filterStatus === 'ALL' || c.status === this.filterStatus;
      return matchSearch && matchStatus;
    });
  }

  // ─── Freelancer: create contract ───────────────────────────────────────────

  openCreateModal(): void {
    this.newContract = this.emptyContract();
    if (this.currentUser) {
      this.newContract.freelancerName = this.currentUser.username;
      this.newContract.freelancerEmail = this.currentUser.email || '';
    }
    this.errors = {};
    this.showCreateModal = true;
  }

  private validateContract(): boolean {
    this.errors = {};
    const c = this.newContract;
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const today = new Date(); today.setHours(0, 0, 0, 0);

    if (!c.contractTitle?.trim())
      this.errors['contractTitle'] = 'Contract title is required.';
    else if (c.contractTitle.trim().length < 3)
      this.errors['contractTitle'] = 'Title must be at least 3 characters.';

    if (!c.clientName?.trim())
      this.errors['clientName'] = 'Client name is required.';

    if (!c.clientEmail?.trim())
      this.errors['clientEmail'] = 'Client email is required.';
    else if (!emailRegex.test(c.clientEmail))
      this.errors['clientEmail'] = 'Enter a valid email address.';

    if (!c.startDate)
      this.errors['startDate'] = 'Start date is required.';
    else if (new Date(c.startDate) < today)
      this.errors['startDate'] = 'Start date must be today or in the future.';

    if (!c.endDate)
      this.errors['endDate'] = 'End date is required.';
    else if (c.startDate && new Date(c.endDate) <= new Date(c.startDate))
      this.errors['endDate'] = 'End date must be after start date.';

    if (c.contractAmount == null || c.contractAmount <= 0)
      this.errors['contractAmount'] = 'Amount must be greater than 0.';
    else if (c.contractAmount > 10_000_000)
      this.errors['contractAmount'] = 'Amount cannot exceed 10,000,000.';

    if (c.paymentTermsDays != null && c.paymentTermsDays < 0)
      this.errors['paymentTermsDays'] = 'Payment terms cannot be negative.';

    return Object.keys(this.errors).length === 0;
  }

  submitContract(): void {
    if (!this.validateContract()) return;

    this.contractService.createContract(this.newContract).subscribe({
      next: () => {
        this.toastService.success('Contract submitted successfully');
        this.showCreateModal = false;
        this.errors = {};
        this.loadContracts();
      },
      error: () => this.toastService.error('Failed to create contract')
    });
  }

  // ─── Client: sign contract ─────────────────────────────────────────────────

  openSignModal(contract: Contract): void {
    this.contractToSign = contract;
    this.clientSignature = '';
    this.currentSignerName = this.currentUser?.username || contract.clientName;
    this.showSignModal = true;
  }

  openSignaturePad(): void {
    this.showSignatureModal = true;
  }

  onSignatureSaved(sig: string): void {
    this.clientSignature = sig;
    this.showSignatureModal = false;
    this.toastService.success('Signature captured');
  }

  onSignatureModalClosed(): void {
    this.showSignatureModal = false;
  }

  confirmSign(): void {
    if (!this.clientSignature) { this.toastService.error('Please provide your signature first'); return; }
    if (!this.contractToSign) return;

    // Generate signed PDF
    const pdfData: ContractPdfData = {
      contractNumber: this.contractToSign.idContract?.toString() || 'DRAFT',
      title: this.contractToSign.contractTitle,
      clientName: this.contractToSign.clientName,
      clientEmail: this.contractToSign.clientEmail,
      freelancerName: this.contractToSign.freelancerName,
      freelancerEmail: this.contractToSign.freelancerEmail,
      startDate: this.formatDate(this.contractToSign.startDate),
      endDate: this.formatDate(this.contractToSign.endDate),
      description: this.contractToSign.projectDescription || '',
      totalAmount: this.contractToSign.contractAmount,
      currency: this.contractToSign.currency || 'USD',
      paymentTerms: `Payment due within ${this.contractToSign.paymentTermsDays || 30} days`,
      deliverables: this.contractToSign.deliverables?.split('\n').filter(d => d.trim()) || [],
      terms: this.contractToSign.termsAndConditions || '',
      clientSignature: this.clientSignature,
      clientSignatureDate: new Date().toLocaleDateString()
    };
    this.contractPdfService.generateContractPdf(pdfData);
    this.toastService.success('Contract signed and PDF downloaded!');
    this.showSignModal = false;
    this.contractToSign = null;
  }

  // ─── Client: pay contract ──────────────────────────────────────────────────

  openPayModal(contract: Contract): void {
    this.contractToPay = contract;
    this.paymentMethod = 'Credit Card';
    this.cardNumber = ''; this.cardHolder = ''; this.cardExpiry = ''; this.cardCvv = '';
    this.bankSenderName = ''; this.bankTransferDate = '';
    this.cardErrors = {};
    this.paymentProcessing = false;
    this.showTestCards = false;
    this.showPayModal = true;
  }

  applyTestCard(tc: any): void {
    this.cardNumber = tc.number;
    this.cardExpiry = tc.expiry;
    this.cardCvv = tc.cvv;
    this.cardHolder = this.currentUser?.username || 'Test User';
    this.showTestCards = false;
    this.cardErrors = {};
  }

  formatCardNumber(event: any): void {
    let val = event.target.value.replace(/\D/g, '');
    const isAmex = val.startsWith('34') || val.startsWith('37');
    val = val.substring(0, isAmex ? 15 : 16);
    if (isAmex) {
      const a = val.substring(0, 4);
      const b = val.substring(4, 10);
      const c = val.substring(10, 15);
      this.cardNumber = [a, b, c].filter(s => s.length > 0).join(' ');
    } else {
      this.cardNumber = val.replace(/(.{4})/g, '$1 ').trim();
    }
  }

  formatExpiry(event: any): void {
    let val = event.target.value.replace(/\D/g, '').substring(0, 4);
    if (val.length >= 2) val = val.substring(0, 2) + '/' + val.substring(2);
    this.cardExpiry = val;
  }

  copyToClipboard(text: string): void {
    navigator.clipboard.writeText(text).then(() => this.toastService.success('Copied to clipboard!'));
  }

  isAmexCard(): boolean {
    const raw = this.cardNumber.replace(/\s/g, '');
    return raw.startsWith('34') || raw.startsWith('37');
  }

  private validatePayment(): boolean {
    this.cardErrors = {};
    if (this.paymentMethod === 'Credit Card') {
      const rawNum = this.cardNumber.replace(/\s/g, '');
      const isAmex = rawNum.startsWith('34') || rawNum.startsWith('37');
      const validLength = isAmex ? rawNum.length === 15 : rawNum.length === 16;

      if (!rawNum || !validLength)
        this.cardErrors['cardNumber'] = isAmex
          ? 'Amex cards require 15 digits.'
          : 'Enter a valid 16-digit card number.';

      if (!this.cardHolder?.trim())
        this.cardErrors['cardHolder'] = 'Card holder name is required.';

      if (!this.cardExpiry || !/^\d{2}\/\d{2}$/.test(this.cardExpiry))
        this.cardErrors['cardExpiry'] = 'Enter expiry as MM/YY.';
      else {
        const [mm, yy] = this.cardExpiry.split('/').map(Number);
        const now = new Date();
        const expDate = new Date(2000 + yy, mm - 1);
        if (mm < 1 || mm > 12) this.cardErrors['cardExpiry'] = 'Invalid month.';
        else if (expDate < now) this.cardErrors['cardExpiry'] = 'Card is expired.';
      }

      const cvvMinLen = isAmex ? 4 : 3;
      if (!this.cardCvv || this.cardCvv.length < cvvMinLen)
        this.cardErrors['cardCvv'] = isAmex
          ? 'Amex CVV must be 4 digits.'
          : 'Enter a valid 3-digit CVV.';
    } else {
      if (!this.bankSenderName?.trim())
        this.cardErrors['bankSenderName'] = 'Your name is required.';
      if (!this.bankTransferDate)
        this.cardErrors['bankTransferDate'] = 'Transfer date is required.';
    }
    return Object.keys(this.cardErrors).length === 0;
  }

  confirmPayment(): void {
    if (!this.contractToPay) return;
    if (!this.contractToPay.idContract) {
      this.toastService.error('Cannot process payment: contract ID is missing.');
      return;
    }
    if (!this.validatePayment()) return;

    this.paymentProcessing = true;

    if (this.paymentMethod === 'Bank Transfer') {
      // Simulate bank transfer confirmation
      setTimeout(() => {
        this.toastService.success('Bank transfer registered! Please complete the transfer using the details provided.');
        this.paymentProcessing = false;
        this.showPayModal = false;
        this.contractToPay = null;
      }, 1500);
      return;
    }

    const request = {
      contractId: this.contractToPay.idContract,
      currency: (this.contractToPay.currency || 'USD').toLowerCase(),
      description: `Payment for: ${this.contractToPay.contractTitle}`,
      customerEmail: this.currentUser?.email || ''
    };

    this.http.post<any>('http://localhost:8083/payments/stripe/create-intent', request).subscribe({
      next: (res) => {
        this.paymentProcessing = false;
        this.toastService.success(`Payment successful! Reference: ${res.paymentIntentId}`);
        this.showPayModal = false;
        this.contractToPay = null;
        this.loadContracts();
      },
      error: (err) => {
        this.paymentProcessing = false;
        // Extract the real error message from the backend
        let msg = 'Payment failed. Please try again.';
        if (err?.error && typeof err.error === 'string') {
          msg = err.error;
        } else if (err?.error?.message) {
          msg = err.error.message;
        } else if (err?.message) {
          msg = err.message;
        }
        console.error('Stripe error:', msg);
        this.toastService.error(msg);
      }
    });
  }

  // ─── Helpers ───────────────────────────────────────────────────────────────

  // ── Métier actions (Freelancer) ───────────────────────────────────────────

  completeContract(contract: Contract): void {
    if (!confirm('Mark this contract as COMPLETED?')) return;
    this.contractService.completeContract(contract.idContract!).subscribe({
      next: () => { this.toastService.success('Contract marked as completed'); this.loadContracts(); },
      error: (e) => this.toastService.error(e?.error || 'Failed to complete contract')
    });
  }

  openDisputeModal(contract: Contract): void {
    this.selectedContractForAction = contract;
    this.disputeForm = { raisedBy: 'FREELANCER', reason: '', description: '' };
    this.showDisputeModal = true;
  }

  submitDispute(): void {
    if (!this.disputeForm.reason.trim() || !this.disputeForm.description.trim()) {
      this.toastService.error('Please fill in all dispute fields'); return;
    }
    this.contractService.raiseDispute(this.selectedContractForAction!.idContract!, this.disputeForm).subscribe({
      next: () => { this.toastService.success('Dispute raised successfully'); this.showDisputeModal = false; this.loadContracts(); },
      error: (e) => this.toastService.error(e?.error || 'Failed to raise dispute')
    });
  }

  openRenewModal(contract: Contract): void {
    this.selectedContractForAction = contract;
    this.renewForm = { newEndDate: '', newAmount: null, renewalNote: '' };
    this.showRenewModal = true;
  }

  submitRenew(): void {
    if (!this.renewForm.newEndDate) { this.toastService.error('New end date is required'); return; }
    const dto: any = { newEndDate: this.renewForm.newEndDate, renewalNote: this.renewForm.renewalNote };
    if (this.renewForm.newAmount) dto.newAmount = this.renewForm.newAmount;
    this.contractService.renewContract(this.selectedContractForAction!.idContract!, dto).subscribe({
      next: () => { this.toastService.success('Contract renewed!'); this.showRenewModal = false; this.loadContracts(); },
      error: (e) => this.toastService.error(e?.error || 'Failed to renew contract')
    });
  }

  getStatusClass(status: ContractStatus): string {
    const map: Record<ContractStatus, string> = {
      [ContractStatus.ACTIVE]: 'status-active',
      [ContractStatus.PENDING]: 'status-pending',
      [ContractStatus.EXPIRED]: 'status-expired',
      [ContractStatus.CANCELLED]: 'status-cancelled',
      [ContractStatus.DISPUTED]: 'status-disputed',
      [ContractStatus.COMPLETED]: 'status-completed',
      [ContractStatus.RENEWED]: 'status-renewed'
    };
    return map[status] || '';
  }

  private formatDate(date: Date | string): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
  }

  private emptyContract(): Contract {
    return {
      contractTitle: '', freelancerName: '', freelancerEmail: '',
      clientName: '', clientEmail: '', projectDescription: '',
      startDate: '', endDate: '', status: ContractStatus.PENDING,
      contractAmount: 0, paymentType: PaymentType.FIXED_PRICE,
      currency: 'USD', contractType: ContractType.PROJECT_BASED,
      deliverables: '', termsAndConditions: '', paymentTermsDays: 30
    };
  }
}
