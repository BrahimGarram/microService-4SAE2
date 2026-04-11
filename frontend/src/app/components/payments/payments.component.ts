import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Payment, PaymentStatus } from '../../models/payment.model';
import { PaymentService } from '../../services/payment.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-payments',
  templateUrl: './payments.component.html',
  styleUrls: ['./payments.component.css']
})
export class PaymentsComponent implements OnInit {
  payments: Payment[] = [];
  filteredPayments: Payment[] = [];
  loading = false;
  showModal = false;
  isEditMode = false;
  currentPayment: Payment = this.getEmptyPayment();
  searchTerm = '';
  filterStatus: PaymentStatus | 'ALL' = 'ALL';
  paymentStatuses = Object.values(PaymentStatus);

  totalPayments = 0;
  successfulPayments = 0;
  pendingPayments = 0;
  failedPayments = 0;
  currentMonthRevenue = 0;

  constructor(
    private paymentService: PaymentService,
    private toastService: ToastService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPayments();
  }

  loadPayments(): void {
    this.loading = true;
    this.paymentService.getAllPayments().subscribe({
      next: (data) => {
        this.payments = data;
        this.calculateStatistics();
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        this.toastService.error('Failed to load payments');
        console.error('Error loading payments:', error);
        this.loading = false;
      }
    });
  }

  calculateStatistics(): void {
    this.totalPayments = this.payments.length;
    this.successfulPayments = this.payments.filter(p => p.status === PaymentStatus.COMPLETED).length;
    this.pendingPayments = this.payments.filter(p => p.status === PaymentStatus.PENDING).length;
    this.failedPayments = this.payments.filter(p => p.status === PaymentStatus.FAILED).length;
    
    // Calculate current month revenue
    const currentDate = new Date();
    const currentMonth = currentDate.getMonth();
    const currentYear = currentDate.getFullYear();
    
    this.currentMonthRevenue = this.payments
      .filter(p => {
        const paymentDate = new Date(p.paymentDate);
        return p.status === PaymentStatus.COMPLETED &&
               paymentDate.getMonth() === currentMonth &&
               paymentDate.getFullYear() === currentYear;
      })
      .reduce((sum, p) => sum + (p.amount || 0), 0);
  }

  applyFilters(): void {
    this.filteredPayments = this.payments.filter(payment => {
      // Search filter
      const searchLower = this.searchTerm.toLowerCase().trim();
      const matchesSearch = !searchLower || 
        payment.idPayment?.toString().includes(searchLower) ||
        payment.amount?.toString().includes(searchLower) ||
        payment.transactionId?.toLowerCase().includes(searchLower) ||
        payment.paymentMethod?.toLowerCase().includes(searchLower) ||
        payment.description?.toLowerCase().includes(searchLower) ||
        payment.status?.toLowerCase().includes(searchLower);
      
      // Status filter
      const matchesStatus = this.filterStatus === 'ALL' || payment.status === this.filterStatus;
      
      return matchesSearch && matchesStatus;
    });
    
    console.log('Search term:', this.searchTerm);
    console.log('Filtered payments:', this.filteredPayments.length);
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  onStatusFilterChange(): void {
    this.applyFilters();
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.currentPayment = this.getEmptyPayment();
    this.showModal = true;
  }

  openEditModal(payment: Payment): void {
    this.isEditMode = true;
    this.currentPayment = { ...payment };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.currentPayment = this.getEmptyPayment();
  }

  savePayment(): void {
    if (!this.currentPayment.amount || this.currentPayment.amount <= 0) {
      this.toastService.error('Please enter a valid amount');
      return;
    }

    if (!this.currentPayment.paymentDate) {
      this.toastService.error('Please select a payment date');
      return;
    }

    if (this.isEditMode && this.currentPayment.idPayment) {
      this.paymentService.updatePayment(this.currentPayment.idPayment, this.currentPayment).subscribe({
        next: () => {
          this.toastService.success('Payment updated successfully');
          this.loadPayments();
          this.closeModal();
        },
        error: (error) => {
          const errorMessage = error?.error?.message || error?.message || 'Failed to update payment';
          this.toastService.error(errorMessage);
          console.error('Error updating payment:', error);
        }
      });
    } else {
      this.paymentService.createPayment(this.currentPayment).subscribe({
        next: () => {
          this.toastService.success('Payment created successfully');
          this.loadPayments();
          this.closeModal();
        },
        error: (error) => {
          const errorMessage = error?.error?.message || error?.message || 'Failed to create payment';
          this.toastService.error(errorMessage);
          console.error('Error creating payment:', error);
        }
      });
    }
  }

  deletePayment(id: number): void {
    if (confirm('Are you sure you want to delete this payment?')) {
      this.paymentService.deletePayment(id).subscribe({
        next: () => {
          this.toastService.success('Payment deleted successfully');
          this.loadPayments();
        },
        error: (error) => {
          this.toastService.error('Failed to delete payment');
          console.error('Error deleting payment:', error);
        }
      });
    }
  }

  getStatusClass(status: PaymentStatus): string {
    const statusClasses: { [key in PaymentStatus]: string } = {
      [PaymentStatus.PENDING]: 'status-pending',
      [PaymentStatus.COMPLETED]: 'status-active',
      [PaymentStatus.FAILED]: 'status-cancelled',
      [PaymentStatus.CANCELLED]: 'status-cancelled',
      [PaymentStatus.REFUNDED]: 'status-expired'
    };
    return statusClasses[status];
  }

  navigateToSubscription(): void {
    this.router.navigate(['/subscription']);
  }

  private getEmptyPayment(): Payment {
    return {
      amount: 0,
      paymentDate: '',
      status: PaymentStatus.PENDING,
      paymentMethod: '',
      description: ''
    };
  }
}
