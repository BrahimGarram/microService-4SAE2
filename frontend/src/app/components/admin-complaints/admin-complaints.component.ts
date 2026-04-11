import { Component, OnInit } from '@angular/core';
import { ComplaintsService, Complaint, ClaimStatus } from '../../services/complaints.service';
import { ToastService } from '../../services/toast.service';
import { UserService } from '../../services/user.service';
import { PenaltyService, Penalty } from '../../services/penalty.service';

interface ComplaintWithUser extends Complaint {
  userName?: string;
  userEmail?: string;
  userProfilePicture?: string;
  userRole?: string;
}

@Component({
  selector: 'app-admin-complaints',
  templateUrl: './admin-complaints.component.html',
  styleUrls: ['./admin-complaints.component.css']
})
export class AdminComplaintsComponent implements OnInit {
  complaints: ComplaintWithUser[] = [];
  filteredComplaints: ComplaintWithUser[] = [];
  loading = false;
  error: string | null = null;

  // Filters
  searchTerm = '';
  selectedPriority: string = 'All';
  selectedStatus: string = 'All';

  // Modal state
  selectedClaim: ComplaintWithUser | null = null;
  showModal = false;
  actionInProgress = false;
  resolutionNote = '';
  
  // Penalty assignment
  showPenaltyModal = false;
  penaltyForm = {
    type: 'WARNING' as 'WARNING' | 'ACCOUNT_RESTRICTION' | 'TEMPORARY_SUSPENSION' | 'FINE' | 'PERMANENT_BAN',
    severity: 'LOW' as 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL',
    reason: '',
    description: '',
    daysToExpire: 7,
    fineAmount: 0
  };
  currentAdminId: number | null = null;
  userPenalties: Penalty[] = [];

  // Statistics
  stats = {
    total: 0,
    pending: 0,
    underReview: 0,
    resolved: 0,
    rejected: 0,
    closed: 0,
    urgent: 0,
    high: 0,
    avgResolutionTime: 0,
    resolutionRate: 0
  };

  constructor(
    private complaintsService: ComplaintsService,
    private toastService: ToastService,
    private userService: UserService,
    private penaltyService: PenaltyService
  ) {}

  ngOnInit(): void {
    this.loadAllComplaints();
    this.loadCurrentAdmin();
  }

  loadCurrentAdmin(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentAdminId = user.id;
      },
      error: (err) => {
        console.error('Error loading admin user:', err);
      }
    });
  }

  loadAllComplaints(): void {
    this.loading = true;
    this.error = null;

    // Load all complaints for admin (from all users)
    this.complaintsService.getAllComplaintsForAdmin().subscribe({
      next: (complaints: Complaint[]) => {
        this.complaints = complaints || [];
        this.loadUserDetailsForComplaints();
      },
      error: (err: any) => {
        console.error('Error loading complaints:', err);
        this.error = 'Failed to load complaints. Please try again.';
        this.loading = false;
        this.toastService.error('Failed to load complaints');
      }
    });
  }

  loadUserDetailsForComplaints(): void {
    const userIds = [...new Set(this.complaints.map(c => c.userId))];
    let loadedUsers = 0;

    userIds.forEach(userId => {
      this.userService.getUserById(userId).subscribe({
        next: (user: any) => {
          // Update all complaints with this userId
          this.complaints.forEach(complaint => {
            if (complaint.userId === userId) {
              complaint.userName = user.username || 'Unknown User';
              complaint.userEmail = user.email || '';
              complaint.userProfilePicture = user.profilePicture || '';
              complaint.userRole = user.role || '';
            }
          });
          
          loadedUsers++;
          if (loadedUsers === userIds.length) {
            this.filteredComplaints = [...this.complaints];
            this.calculateStatistics();
            this.loading = false;
            console.log('Loaded complaints with user details:', this.complaints);
          }
        },
        error: (err: any) => {
          console.error(`Error loading user ${userId}:`, err);
          // Set default values for failed user loads
          this.complaints.forEach(complaint => {
            if (complaint.userId === userId && !complaint.userName) {
              complaint.userName = 'Unknown User';
              complaint.userEmail = '';
              complaint.userProfilePicture = '';
              complaint.userRole = '';
            }
          });
          
          loadedUsers++;
          if (loadedUsers === userIds.length) {
            this.filteredComplaints = [...this.complaints];
            this.calculateStatistics();
            this.loading = false;
          }
        }
      });
    });
  }

  applyFilters(): void {
    let filtered = [...this.complaints];

    // Apply search filter
    if (this.searchTerm.trim()) {
      const search = this.searchTerm.toLowerCase().trim();
      filtered = filtered.filter(c => 
        c.title.toLowerCase().includes(search) ||
        c.description.toLowerCase().includes(search) ||
        (c.resolutionNote && c.resolutionNote.toLowerCase().includes(search)) ||
        (c.userName && c.userName.toLowerCase().includes(search)) ||
        (c.userEmail && c.userEmail.toLowerCase().includes(search))
      );
    }

    // Apply priority filter
    if (this.selectedPriority !== 'All') {
      filtered = filtered.filter(c => c.claimPriority === this.selectedPriority);
    }

    // Apply status filter
    if (this.selectedStatus !== 'All') {
      filtered = filtered.filter(c => c.claimStatus === this.selectedStatus);
    }

    this.filteredComplaints = filtered;
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  onPriorityChange(): void {
    this.applyFilters();
  }

  onStatusChange(): void {
    this.applyFilters();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedPriority = 'All';
    this.selectedStatus = 'All';
    this.filteredComplaints = [...this.complaints];
  }

  calculateStatistics(): void {
    this.stats.total = this.complaints.length;
    this.stats.pending = this.complaints.filter(c => c.claimStatus === 'Pending').length;
    this.stats.underReview = this.complaints.filter(c => c.claimStatus === 'Under_Review').length;
    this.stats.resolved = this.complaints.filter(c => c.claimStatus === 'Resolved').length;
    this.stats.rejected = this.complaints.filter(c => c.claimStatus === 'Rejected').length;
    this.stats.closed = this.complaints.filter(c => c.claimStatus === 'Closed').length;
    this.stats.urgent = this.complaints.filter(c => c.claimPriority === 'Urgent').length;
    this.stats.high = this.complaints.filter(c => c.claimPriority === 'High').length;

    // Calculate resolution rate
    const resolvedOrClosed = this.stats.resolved + this.stats.closed;
    this.stats.resolutionRate = this.stats.total > 0 
      ? Math.round((resolvedOrClosed / this.stats.total) * 100) 
      : 0;

    // Calculate average resolution time (in days)
    const resolvedComplaints = this.complaints.filter(c => 
      c.resolvedAt && c.createdAt
    );
    
    if (resolvedComplaints.length > 0) {
      const totalDays = resolvedComplaints.reduce((sum, complaint) => {
        const created = new Date(complaint.createdAt!);
        const resolved = new Date(complaint.resolvedAt!);
        const diffTime = Math.abs(resolved.getTime() - created.getTime());
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return sum + diffDays;
      }, 0);
      
      this.stats.avgResolutionTime = Math.round(totalDays / resolvedComplaints.length);
    } else {
      this.stats.avgResolutionTime = 0;
    }
  }

  viewDetails(complaint: Complaint): void {
    this.selectedClaim = complaint;
    this.resolutionNote = complaint.resolutionNote || '';
    this.showModal = true;

    // Load user penalties
    this.loadUserPenalties(complaint.userId);

    // Automatically change status to Under_Review if it's Pending
    if (complaint.claimStatus === 'Pending') {
      this.changeStatusSilently('Under_Review');
    }
  }

  loadUserPenalties(userId: number): void {
    this.penaltyService.getActivePenalties(userId).subscribe({
      next: (penalties) => {
        this.userPenalties = penalties;
      },
      error: (err) => {
        console.error('Error loading user penalties:', err);
        this.userPenalties = [];
      }
    });
  }

  closeModal(): void {
    this.showModal = false;
    this.selectedClaim = null;
    this.resolutionNote = '';
    this.userPenalties = [];
  }

  openPenaltyModal(): void {
    if (!this.selectedClaim) return;
    this.showPenaltyModal = true;
    this.resetPenaltyForm();
  }

  closePenaltyModal(): void {
    this.showPenaltyModal = false;
    this.resetPenaltyForm();
  }

  resetPenaltyForm(): void {
    this.penaltyForm = {
      type: 'WARNING',
      severity: 'LOW',
      reason: '',
      description: '',
      daysToExpire: 7,
      fineAmount: 0
    };
  }

  assignPenalty(): void {
    if (!this.selectedClaim || !this.currentAdminId) {
      this.toastService.error('Unable to assign penalty');
      return;
    }

    if (!this.penaltyForm.reason.trim() || !this.penaltyForm.description.trim()) {
      this.toastService.error('Please fill in reason and description');
      return;
    }

    this.actionInProgress = true;

    const penaltyRequest = {
      userId: this.selectedClaim.userId,
      complaintId: this.selectedClaim.idReclamation,
      type: this.penaltyForm.type,
      severity: this.penaltyForm.severity,
      reason: this.penaltyForm.reason.trim(),
      description: this.penaltyForm.description.trim(),
      adminId: this.currentAdminId,
      daysToExpire: this.penaltyForm.type === 'WARNING' || this.penaltyForm.type === 'PERMANENT_BAN' 
        ? null 
        : this.penaltyForm.daysToExpire,
      fineAmount: this.penaltyForm.type === 'FINE' ? this.penaltyForm.fineAmount : null
    };

    this.penaltyService.applyManualPenalty(penaltyRequest).subscribe({
      next: (penalty) => {
        this.toastService.success('Penalty assigned successfully');
        this.actionInProgress = false;
        this.closePenaltyModal();
        this.loadUserPenalties(this.selectedClaim!.userId);
      },
      error: (err) => {
        console.error('Error assigning penalty:', err);
        this.toastService.error('Failed to assign penalty');
        this.actionInProgress = false;
      }
    });
  }

  onPenaltyTypeChange(): void {
    // Auto-adjust severity based on type
    switch (this.penaltyForm.type) {
      case 'WARNING':
        this.penaltyForm.severity = 'LOW';
        break;
      case 'ACCOUNT_RESTRICTION':
        this.penaltyForm.severity = 'MEDIUM';
        this.penaltyForm.daysToExpire = 7;
        break;
      case 'TEMPORARY_SUSPENSION':
        this.penaltyForm.severity = 'HIGH';
        this.penaltyForm.daysToExpire = 14;
        break;
      case 'FINE':
        this.penaltyForm.severity = 'MEDIUM';
        this.penaltyForm.fineAmount = 50;
        break;
      case 'PERMANENT_BAN':
        this.penaltyForm.severity = 'CRITICAL';
        break;
    }
  }

  getPenaltyTypeClass(type: string): string {
    switch (type) {
      case 'WARNING': return 'penalty-warning';
      case 'ACCOUNT_RESTRICTION': return 'penalty-restriction';
      case 'TEMPORARY_SUSPENSION': return 'penalty-suspension';
      case 'FINE': return 'penalty-fine';
      case 'PERMANENT_BAN': return 'penalty-ban';
      default: return 'penalty-warning';
    }
  }

  getSeverityClass(severity: string): string {
    switch (severity) {
      case 'LOW': return 'severity-low';
      case 'MEDIUM': return 'severity-medium';
      case 'HIGH': return 'severity-high';
      case 'CRITICAL': return 'severity-critical';
      default: return 'severity-low';
    }
  }

  formatPenaltyDate(dateString?: string): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
  }

  removePenalty(penalty: Penalty): void {
    if (!confirm(`Are you sure you want to remove this penalty?\n\nType: ${penalty.penaltyType}\nReason: ${penalty.reason}\n\nThis action cannot be undone.`)) {
      return;
    }

    this.actionInProgress = true;

    this.penaltyService.deactivatePenalty(penalty.idPenalty).subscribe({
      next: () => {
        this.toastService.success('Penalty removed successfully');
        this.actionInProgress = false;
        // Reload penalties
        if (this.selectedClaim) {
          this.loadUserPenalties(this.selectedClaim.userId);
        }
      },
      error: (err) => {
        console.error('Error removing penalty:', err);
        this.toastService.error('Failed to remove penalty');
        this.actionInProgress = false;
      }
    });
  }

  changeStatusSilently(newStatus: ClaimStatus): void {
    if (!this.selectedClaim) return;

    // Instant UI update
    const previousStatus = this.selectedClaim.claimStatus;
    this.selectedClaim.claimStatus = newStatus;
    this.updateComplaintInList(this.selectedClaim);

    const updateRequest = {
      idReclamation: this.selectedClaim.idReclamation,
      title: this.selectedClaim.title,
      description: this.selectedClaim.description,
      claimPriority: this.selectedClaim.claimPriority,
      claimStatus: newStatus,
      resolutionNote: this.selectedClaim.resolutionNote
    };

    this.complaintsService.updateClaimByAdmin(updateRequest).subscribe({
      next: (updatedClaim: Complaint) => {
        this.selectedClaim = { ...updatedClaim, ...this.selectedClaim };
        this.updateComplaintInList(this.selectedClaim);
        this.calculateStatistics();
        this.applyFilters();
      },
      error: (err: any) => {
        console.error('Error updating complaint:', err);
        // Revert on error
        if (this.selectedClaim) {
          this.selectedClaim.claimStatus = previousStatus;
          this.updateComplaintInList(this.selectedClaim);
        }
        this.toastService.error('Failed to update complaint status');
      }
    });
  }

  changeStatus(newStatus: ClaimStatus): void {
    if (!this.selectedClaim) return;

    this.actionInProgress = true;
    
    // Instant UI update
    const previousStatus = this.selectedClaim.claimStatus;
    const previousNote = this.selectedClaim.resolutionNote;
    this.selectedClaim.claimStatus = newStatus;
    if (this.resolutionNote.trim()) {
      this.selectedClaim.resolutionNote = this.resolutionNote.trim();
    }
    this.updateComplaintInList(this.selectedClaim);
    
    const updateRequest = {
      idReclamation: this.selectedClaim.idReclamation,
      title: this.selectedClaim.title,
      description: this.selectedClaim.description,
      claimPriority: this.selectedClaim.claimPriority,
      claimStatus: newStatus,
      resolutionNote: this.resolutionNote.trim() || this.selectedClaim.resolutionNote
    };

    this.complaintsService.updateClaimByAdmin(updateRequest).subscribe({
      next: (updatedClaim: Complaint) => {
        this.toastService.success(`Complaint status changed to ${newStatus}`);
        this.selectedClaim = { ...updatedClaim, ...this.selectedClaim };
        this.updateComplaintInList(this.selectedClaim);
        this.actionInProgress = false;
        this.calculateStatistics();
        this.applyFilters();
      },
      error: (err: any) => {
        console.error('Error updating complaint:', err);
        this.toastService.error('Failed to update complaint status');
        // Revert on error
        if (this.selectedClaim) {
          this.selectedClaim.claimStatus = previousStatus;
          this.selectedClaim.resolutionNote = previousNote;
          this.updateComplaintInList(this.selectedClaim);
        }
        this.actionInProgress = false;
      }
    });
  }

  saveResolutionNote(): void {
    if (!this.selectedClaim) return;
    if (!this.resolutionNote.trim()) {
      this.toastService.error('Please enter a resolution note');
      return;
    }

    this.actionInProgress = true;
    
    // Instant UI update
    const previousNote = this.selectedClaim.resolutionNote;
    this.selectedClaim.resolutionNote = this.resolutionNote.trim();
    this.updateComplaintInList(this.selectedClaim);
    
    const updateRequest = {
      idReclamation: this.selectedClaim.idReclamation,
      title: this.selectedClaim.title,
      description: this.selectedClaim.description,
      claimPriority: this.selectedClaim.claimPriority,
      claimStatus: this.selectedClaim.claimStatus,
      resolutionNote: this.resolutionNote.trim()
    };

    this.complaintsService.updateClaimByAdmin(updateRequest).subscribe({
      next: (updatedClaim: Complaint) => {
        this.toastService.success('Resolution note saved successfully');
        this.selectedClaim = { ...updatedClaim, ...this.selectedClaim };
        this.updateComplaintInList(this.selectedClaim);
        this.actionInProgress = false;
      },
      error: (err: any) => {
        console.error('Error saving resolution note:', err);
        this.toastService.error('Failed to save resolution note');
        // Revert on error
        if (this.selectedClaim) {
          this.selectedClaim.resolutionNote = previousNote;
          this.updateComplaintInList(this.selectedClaim);
        }
        this.actionInProgress = false;
      }
    });
  }

  updateComplaintInList(updatedComplaint: ComplaintWithUser): void {
    const index = this.complaints.findIndex(c => c.idReclamation === updatedComplaint.idReclamation);
    if (index !== -1) {
      this.complaints[index] = { ...this.complaints[index], ...updatedComplaint };
    }
    
    const filteredIndex = this.filteredComplaints.findIndex(c => c.idReclamation === updatedComplaint.idReclamation);
    if (filteredIndex !== -1) {
      this.filteredComplaints[filteredIndex] = { ...this.filteredComplaints[filteredIndex], ...updatedComplaint };
    }
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';
    const d = new Date(dateString);
    return isNaN(d.getTime()) ? 'N/A' : d.toLocaleDateString();
  }

  formatDateTime(dateString?: string): string {
    if (!dateString) return 'N/A';
    const d = new Date(dateString);
    if (isNaN(d.getTime())) return 'N/A';
    return d.toLocaleString();
  }

  formatFileSize(bytes?: number): string {
    if (!bytes) return 'N/A';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
  }

  getFullFileUrl(fileUrl?: string): string {
    if (!fileUrl) return '';
    if (fileUrl.startsWith('http://') || fileUrl.startsWith('https://')) {
      return fileUrl;
    }
    return 'http://localhost:8089' + fileUrl;
  }

  getUserInitials(userName?: string): string {
    if (!userName) return 'U';
    const names = userName.split(' ');
    if (names.length >= 2) {
      return (names[0][0] + names[1][0]).toUpperCase();
    }
    return userName.substring(0, 2).toUpperCase();
  }

  getStatusBadgeClass(status: Complaint['claimStatus']): string {
    switch (status) {
      case 'Pending':
        return 'status-badge status-pending';
      case 'Under_Review':
        return 'status-badge status-in-review';
      case 'Resolved':
        return 'status-badge status-resolved';
      case 'Rejected':
        return 'status-badge status-rejected';
      case 'Closed':
        return 'status-badge status-closed';
      default:
        return 'status-badge status-pending';
    }
  }

  getPriorityBadgeClass(priority: Complaint['claimPriority']): string {
    switch (priority) {
      case 'Urgent':
        return 'priority-badge priority-urgent';
      case 'High':
        return 'priority-badge priority-high';
      case 'Medium':
        return 'priority-badge priority-medium';
      case 'Low':
        return 'priority-badge priority-low';
      default:
        return 'priority-badge priority-medium';
    }
  }

  getAvailableActions(): { status: ClaimStatus; label: string; icon: string; color: string }[] {
    if (!this.selectedClaim) return [];

    const currentStatus = this.selectedClaim.claimStatus;
    const actions: { status: ClaimStatus; label: string; icon: string; color: string }[] = [];

    if (currentStatus === 'Pending') {
      actions.push(
        { status: 'Under_Review', label: 'Start Review', icon: 'fa-eye', color: 'blue' },
        { status: 'Rejected', label: 'Reject', icon: 'fa-times-circle', color: 'red' }
      );
    } else if (currentStatus === 'Under_Review') {
      actions.push(
        { status: 'Resolved', label: 'Resolve', icon: 'fa-check-circle', color: 'green' },
        { status: 'Rejected', label: 'Reject', icon: 'fa-times-circle', color: 'red' }
      );
    } else if (currentStatus === 'Resolved') {
      actions.push(
        { status: 'Closed', label: 'Close', icon: 'fa-lock', color: 'gray' }
      );
    }

    return actions;
  }
}
