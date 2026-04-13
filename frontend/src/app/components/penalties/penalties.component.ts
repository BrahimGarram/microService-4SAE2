import { Component, OnInit } from '@angular/core';
import { PenaltyService, Penalty } from '../../services/penalty.service';
import { UserService } from '../../services/user.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-penalties',
  templateUrl: './penalties.component.html',
  styleUrls: ['./penalties.component.css']
})
export class PenaltiesComponent implements OnInit {
  penalties: Penalty[] = [];
  loading = false;
  error: string | null = null;
  currentUserId: number | null = null;
  accountStatus: 'NORMAL' | 'WARNING' | 'RESTRICTED' | 'SUSPENDED' = 'NORMAL';
  highestSeverity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | null = null;
  restrictionEndDate: Date | null = null;
  countdownText = '';

  constructor(
    private penaltyService: PenaltyService,
    private userService: UserService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadUserAndPenalties();
    // Update countdown every second
    setInterval(() => {
      this.updateCountdown();
    }, 1000);
  }

  loadUserAndPenalties(): void {
    this.loading = true;
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUserId = user.id;
        this.loadPenalties();
      },
      error: (err) => {
        console.error('Error loading user:', err);
        this.error = 'Failed to load user information';
        this.loading = false;
        this.toastService.error('Failed to load user information');
      }
    });
  }

  loadPenalties(): void {
    if (!this.currentUserId) return;

    this.penaltyService.getActivePenalties(this.currentUserId).subscribe({
      next: (penalties) => {
        this.penalties = penalties;
        this.loading = false;
        console.log('Loaded penalties:', penalties);
        this.calculateAccountStatus();
        this.updateCountdown();
      },
      error: (err) => {
        console.error('Error loading penalties:', err);
        this.error = 'Failed to load penalties';
        this.loading = false;
        this.toastService.error('Failed to load penalties');
      }
    });
  }

  calculateAccountStatus(): void {
    if (this.penalties.length === 0) {
      this.accountStatus = 'NORMAL';
      this.highestSeverity = null;
      this.restrictionEndDate = null;
      return;
    }

    // Find highest severity
    const severityOrder = { 'LOW': 1, 'MEDIUM': 2, 'HIGH': 3, 'CRITICAL': 4 };
    let maxSeverity = 0;
    let latestEndDate: Date | null = null;

    this.penalties.forEach(penalty => {
      const severityValue = severityOrder[penalty.severity];
      if (severityValue > maxSeverity) {
        maxSeverity = severityValue;
        this.highestSeverity = penalty.severity;
      }

      if (penalty.expiresAt) {
        const endDate = new Date(penalty.expiresAt);
        if (!latestEndDate || endDate > latestEndDate) {
          latestEndDate = endDate;
        }
      }
    });

    this.restrictionEndDate = latestEndDate;

    // Set account status based on highest severity
    if (this.highestSeverity === 'CRITICAL' || this.penalties.some(p => p.penaltyType === 'PERMANENT_BAN')) {
      this.accountStatus = 'SUSPENDED';
    } else if (this.highestSeverity === 'HIGH' || this.penalties.some(p => p.penaltyType === 'TEMPORARY_SUSPENSION')) {
      this.accountStatus = 'SUSPENDED';
    } else if (this.highestSeverity === 'MEDIUM' || this.penalties.some(p => p.penaltyType === 'ACCOUNT_RESTRICTION')) {
      this.accountStatus = 'RESTRICTED';
    } else {
      this.accountStatus = 'WARNING';
    }
  }

  updateCountdown(): void {
    if (!this.restrictionEndDate) {
      this.countdownText = '';
      return;
    }

    const now = new Date();
    const end = new Date(this.restrictionEndDate);
    const diff = end.getTime() - now.getTime();

    if (diff <= 0) {
      this.countdownText = 'Restriction expired';
      return;
    }

    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((diff % (1000 * 60)) / 1000);

    if (days > 0) {
      this.countdownText = `${days}d ${hours}h ${minutes}m`;
    } else if (hours > 0) {
      this.countdownText = `${hours}h ${minutes}m ${seconds}s`;
    } else {
      this.countdownText = `${minutes}m ${seconds}s`;
    }
  }

  getStatusBarClass(): string {
    switch (this.accountStatus) {
      case 'WARNING': return 'status-bar-warning';
      case 'RESTRICTED': return 'status-bar-restricted';
      case 'SUSPENDED': return 'status-bar-suspended';
      default: return 'status-bar-normal';
    }
  }

  getStatusText(): string {
    switch (this.accountStatus) {
      case 'WARNING': return 'Account Warning';
      case 'RESTRICTED': return 'Account Restricted';
      case 'SUSPENDED': return 'Account Suspended';
      default: return 'Account Active';
    }
  }

  getStatusIcon(): string {
    switch (this.accountStatus) {
      case 'WARNING': return 'fa-exclamation-triangle';
      case 'RESTRICTED': return 'fa-ban';
      case 'SUSPENDED': return 'fa-pause-circle';
      default: return 'fa-check-circle';
    }
  }

  getPenaltyTypeClass(type: string): string {
    switch (type) {
      case 'WARNING':
        return 'penalty-warning';
      case 'ACCOUNT_RESTRICTION':
        return 'penalty-restriction';
      case 'TEMPORARY_SUSPENSION':
        return 'penalty-suspension';
      case 'FINE':
        return 'penalty-fine';
      case 'PERMANENT_BAN':
        return 'penalty-ban';
      default:
        return 'penalty-warning';
    }
  }

  getSeverityClass(severity: string): string {
    switch (severity) {
      case 'LOW':
        return 'severity-low';
      case 'MEDIUM':
        return 'severity-medium';
      case 'HIGH':
        return 'severity-high';
      case 'CRITICAL':
        return 'severity-critical';
      default:
        return 'severity-low';
    }
  }

  getPenaltyIcon(type: string): string {
    switch (type) {
      case 'WARNING':
        return 'fa-exclamation-triangle';
      case 'ACCOUNT_RESTRICTION':
        return 'fa-ban';
      case 'TEMPORARY_SUSPENSION':
        return 'fa-pause-circle';
      case 'FINE':
        return 'fa-dollar-sign';
      case 'PERMANENT_BAN':
        return 'fa-times-circle';
      default:
        return 'fa-exclamation-triangle';
    }
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
  }

  isExpired(penalty: Penalty): boolean {
    if (!penalty.expiresAt) return false;
    return new Date(penalty.expiresAt) < new Date();
  }

  getDaysUntilExpiry(penalty: Penalty): number | null {
    if (!penalty.expiresAt) return null;
    const now = new Date();
    const expiry = new Date(penalty.expiresAt);
    const diffTime = expiry.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays > 0 ? diffDays : 0;
  }

  getCountdownForPenalty(penalty: Penalty): string {
    if (!penalty.expiresAt) return '';

    const now = new Date();
    const end = new Date(penalty.expiresAt);
    const diff = end.getTime() - now.getTime();

    if (diff <= 0) return 'Expired';

    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((diff % (1000 * 60)) / 1000);

    if (days > 0) {
      return `${days}d ${hours}h ${minutes}m`;
    } else if (hours > 0) {
      return `${hours}h ${minutes}m ${seconds}s`;
    } else {
      return `${minutes}m ${seconds}s`;
    }
  }
}
