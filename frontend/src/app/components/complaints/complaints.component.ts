import { Component, OnInit } from '@angular/core';
import {
  ComplaintsService,
  Complaint,
  CreateComplaintRequest,
  UpdateComplaintRequest,
  ClaimPriority,
  ClaimStatus
} from '../../services/complaints.service';
import { UserService } from '../../services/user.service';
import { ToastService } from '../../services/toast.service';
import { PenaltyService, Penalty } from '../../services/penalty.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-complaints',
  templateUrl: './complaints.component.html',
  styleUrls: ['./complaints.component.css']
})
export class ComplaintsComponent implements OnInit {

  complaints: Complaint[] = [];
  loading = false;
  error: string | null = null;
  creating = false;
  currentUserId: number | null = null;
  currentUserEmail: string | null = null;
  activePenalties: Penalty[] = [];
  hasActivePenalties = false;
  accountStatus: 'NORMAL' | 'WARNING' | 'RESTRICTED' | 'SUSPENDED' = 'NORMAL';
  highestSeverity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | null = null;
  isAccountRestricted = false;
  restrictionEndDate: Date | null = null;
  countdownText = '';

  // Modal state
  selectedClaim: Complaint | null = null;
  isEditMode = false;
  updating = false;
  deleting = false;
  editClaim: {
    title: string;
    description: string;
    claimPriority: ClaimPriority;
    claimStatus: ClaimStatus;
  } = {
    title: '',
    description: '',
    claimPriority: 'Medium',
    claimStatus: 'Pending'
  };

  // Filters
  statusFilter: 'ALL' | ClaimStatus = 'ALL';
  priorityFilter: 'ALL' | ClaimPriority = 'ALL';

  // Predefined complaint types, grouped for nicer dropdown
  complaintTypes: { group: string; items: string[] }[] = [
    {
      group: 'Payments & deadlines',
      items: ['Payment issue', 'Late delivery']
    },
    {
      group: 'Work quality & communication',
      items: ['Poor quality work', 'Communication problem', 'Harassment or abuse']
    },
    {
      group: 'Account & platform',
      items: ['Account or profile issue', 'Bug or technical issue', 'Other']
    }
  ];

  // Create form model (template-driven)
  newClaim: {
    type: string;
    description: string;
    claimPriority: ClaimPriority;
    attachmentName: string;
    file: File | null;
  } = {
    type: 'Payment issue',
    description: '',
    claimPriority: 'Medium',
    attachmentName: '',
    file: null
  };

  constructor(
    private complaintsService: ComplaintsService,
    private userService: UserService,
    private toastService: ToastService,
    private penaltyService: PenaltyService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUserAndComplaints();
    // Update countdown every second
    setInterval(() => {
      this.updateCountdown();
    }, 1000);
  }

  loadUserAndComplaints(): void {
    this.loading = true;
    this.error = null;

    // First get the current user to know their numeric ID
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUserId = user.id;
        this.currentUserEmail = user.email || null;  // Handle undefined
        console.log('Current user loaded:', user.id, user.email);
        this.loadComplaints();
        this.checkPenalties();
      },
      error: (err) => {
        console.error('Error loading current user:', err);
        this.error = 'Failed to load user information. Please try again.';
        this.loading = false;
        this.toastService.error('Failed to load user information');
      }
    });
  }

  loadComplaints(): void {
    if (!this.currentUserId) {
      this.loading = false;
      this.error = 'No user session found.';
      return;
    }

    this.loading = true;
    this.error = null;

    this.complaintsService.getComplaintsForUser(this.currentUserId).subscribe({
      next: (complaints: Complaint[]) => {
        this.complaints = complaints || [];
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error loading complaints:', err);
        this.error = 'Failed to load your claims. Please try again.';
        this.loading = false;
        this.toastService.error('Failed to load your claims');
      }
    });
  }

  get filteredComplaints(): Complaint[] {
    return (this.complaints || []).filter(c => {
      const statusOk = this.statusFilter === 'ALL' || c.claimStatus === this.statusFilter;
      const priorityOk = this.priorityFilter === 'ALL' || c.claimPriority === this.priorityFilter;
      return statusOk && priorityOk;
    });
  }

  submitClaim(): void {
    if (this.isAccountRestricted) {
      this.toastService.error('Your account is restricted. You cannot submit new claims.');
      return;
    }
    
    if (!this.currentUserId) {
      this.toastService.error('You must be logged in to submit a claim');
      return;
    }
    if (!this.newClaim.type.trim() || !this.newClaim.description.trim()) {
      this.toastService.error('Please fill in complaint type and description');
      return;
    }

    this.creating = true;

    // Check if file is an image and upload to ImgBB
    if (this.newClaim.file && this.isImageFile(this.newClaim.file)) {
      // Capture file metadata BEFORE uploading
      const fileMetadata = {
        fileName: this.newClaim.file.name,
        fileType: this.newClaim.file.type,
        fileSize: this.newClaim.file.size
      };
      
      console.log('Uploading image to ImgBB:', fileMetadata.fileName, fileMetadata.fileType, fileMetadata.fileSize);
      this.toastService.info('Uploading image to ImgBB...');
      
      this.complaintsService.uploadImageToImgBB(this.newClaim.file).subscribe({
        next: (response: any) => {
          console.log('ImgBB response:', response);
          
          if (response.success && response.data && response.data.url) {
            this.toastService.success('Image uploaded to ImgBB successfully');
            console.log('Image URL:', response.data.url);
            console.log('Display URL:', response.data.display_url);
            
            // Create claim with ImgBB URL and file metadata
            this.createClaimWithImageUrl(response.data.url, fileMetadata);
          } else {
            console.error('ImgBB upload failed - invalid response:', response);
            this.toastService.warning('ImgBB upload failed, using local storage');
            this.createClaimWithLocalFile();
          }
        },
        error: (err: any) => {
          console.error('Error uploading to ImgBB:', err);
          console.error('Error details:', {
            status: err.status,
            statusText: err.statusText,
            message: err.message,
            error: err.error
          });
          
          this.toastService.warning('ImgBB upload failed, using local storage');
          // Fallback to local file upload
          this.createClaimWithLocalFile();
        }
      });
    } else {
      // Non-image file or no file - use local upload
      console.log('Using local file upload');
      this.createClaimWithLocalFile();
    }
  }

  private isImageFile(file: File): boolean {
    return file.type.startsWith('image/');
  }

  private createClaimWithImageUrl(imageUrl: string, fileMetadata: { fileName: string; fileType: string; fileSize: number }): void {
    if (!this.currentUserId) return;

    this.complaintsService.createClaimWithFile(this.currentUserId, {
      title: this.newClaim.type.trim(),
      description: this.newClaim.description.trim(),
      claimPriority: this.newClaim.claimPriority,
      imageUrl: imageUrl, // Send ImgBB URL
      fileName: fileMetadata.fileName,
      fileType: fileMetadata.fileType,
      fileSize: fileMetadata.fileSize,
      userEmail: this.currentUserEmail || undefined  // Pass user email
    }).subscribe({
      next: () => {
        this.toastService.success('Claim submitted successfully with ImgBB image');
        this.newClaim = {
          type: 'Payment issue',
          description: '',
          claimPriority: 'Medium',
          attachmentName: '',
          file: null
        };
        this.creating = false;
        this.loadComplaints();
      },
      error: (err: any) => {
        console.error('Error creating claim:', err);
        this.toastService.error('Failed to submit claim');
        this.creating = false;
      }
    });
  }

  private createClaimWithLocalFile(): void {
    if (!this.currentUserId) return;

    this.complaintsService.createClaimWithFile(this.currentUserId, {
      title: this.newClaim.type.trim(),
      description: this.newClaim.description.trim(),
      claimPriority: this.newClaim.claimPriority,
      file: this.newClaim.file,
      userEmail: this.currentUserEmail || undefined  // Pass user email
    }).subscribe({
      next: () => {
        this.toastService.success('Claim submitted successfully');
        this.newClaim = {
          type: 'Payment issue',
          description: '',
          claimPriority: 'Medium',
          attachmentName: '',
          file: null
        };
        this.creating = false;
        this.loadComplaints();
      },
      error: (err: any) => {
        console.error('Error creating claim:', err);
        this.toastService.error('Failed to submit claim');
        this.creating = false;
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      this.newClaim.file = file;
      if (!this.newClaim.attachmentName) {
        this.newClaim.attachmentName = file.name;
      }
    } else {
      this.newClaim.file = null;
    }
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';
    const d = new Date(dateString);
    return isNaN(d.getTime()) ? 'N/A' : d.toLocaleDateString();
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

  openClaimDetails(claim: Complaint): void {
    this.selectedClaim = claim;
    this.isEditMode = false;
  }

  closeClaimDetails(): void {
    this.selectedClaim = null;
    this.isEditMode = false;
    this.editClaim = {
      title: '',
      description: '',
      claimPriority: 'Medium',
      claimStatus: 'Pending'
    };
  }

  toggleEditMode(): void {
    if (this.selectedClaim) {
      this.isEditMode = true;
      this.editClaim = {
        title: this.selectedClaim.title,
        description: this.selectedClaim.description,
        claimPriority: this.selectedClaim.claimPriority,
        claimStatus: this.selectedClaim.claimStatus
      };
    }
  }

  cancelEdit(): void {
    this.isEditMode = false;
    this.editClaim = {
      title: '',
      description: '',
      claimPriority: 'Medium',
      claimStatus: 'Pending'
    };
  }

  updateClaim(): void {
    if (!this.selectedClaim || !this.currentUserId) {
      this.toastService.error('Unable to update claim');
      return;
    }

    if (!this.editClaim.title.trim() || !this.editClaim.description.trim()) {
      this.toastService.error('Title and description are required');
      return;
    }

    this.updating = true;
    
    // Users can only update title, description, and priority
    // Status changes are handled by admin actions only
    const updateRequest: UpdateComplaintRequest = {
      idReclamation: this.selectedClaim.idReclamation,
      title: this.editClaim.title.trim(),
      description: this.editClaim.description.trim(),
      claimPriority: this.editClaim.claimPriority,
      claimStatus: this.selectedClaim.claimStatus // Keep original status, don't allow user to change it
    };

    console.log('Updating claim with data:', updateRequest);
    console.log('User ID:', this.currentUserId);

    this.complaintsService.updateClaim(
      this.selectedClaim.idReclamation,
      this.currentUserId,
      updateRequest
    ).subscribe({
      next: (updatedClaim: Complaint) => {
        this.toastService.success('Claim updated successfully');
        this.updating = false;
        this.isEditMode = false;
        this.selectedClaim = updatedClaim;
        this.loadComplaints();
      },
      error: (err: any) => {
        console.error('Error updating claim:', err);
        console.error('Error status:', err.status);
        console.error('Error message:', err.message);
        console.error('Error body:', err.error);
        this.toastService.error('Failed to update claim');
        this.updating = false;
      }
    });
  }

  confirmDelete(): void {
    if (!this.selectedClaim) return;

    const confirmed = confirm(`Are you sure you want to delete the claim "${this.selectedClaim.title}"? This action cannot be undone.`);
    if (!confirmed) return;

    this.deleteClaim();
  }

  deleteClaim(): void {
    if (!this.selectedClaim || !this.currentUserId) {
      this.toastService.error('Unable to delete claim');
      return;
    }

    this.deleting = true;
    this.complaintsService.deleteClaim(this.selectedClaim.idReclamation, this.currentUserId).subscribe({
      next: () => {
        this.toastService.success('Claim deleted successfully');
        this.deleting = false;
        this.closeClaimDetails();
        this.loadComplaints();
      },
      error: (err: any) => {
        console.error('Error deleting claim:', err);
        this.toastService.error('Failed to delete claim');
        this.deleting = false;
      }
    });
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
    // If it's already a full URL (starts with http), return as is
    if (fileUrl.startsWith('http://') || fileUrl.startsWith('https://')) {
      return fileUrl;
    }
    // Otherwise, prepend the backend base URL
    return 'http://localhost:8089' + fileUrl;
  }

  canEditClaim(): boolean {
    if (!this.selectedClaim) return false;
    // Only allow editing if status is Pending
    return this.selectedClaim.claimStatus === 'Pending';
  }

  checkPenalties(): void {
    if (!this.currentUserId) return;

    this.penaltyService.getActivePenalties(this.currentUserId).subscribe({
      next: (penalties) => {
        this.activePenalties = penalties;
        this.hasActivePenalties = penalties.length > 0;
        
        if (this.hasActivePenalties) {
          console.log('User has active penalties:', penalties);
          this.calculateAccountStatus();
          this.updateCountdown();
        } else {
          this.accountStatus = 'NORMAL';
          this.highestSeverity = null;
          this.isAccountRestricted = false;
          this.restrictionEndDate = null;
        }
      },
      error: (err) => {
        console.error('Error checking penalties:', err);
      }
    });
  }

  calculateAccountStatus(): void {
    if (this.activePenalties.length === 0) {
      this.accountStatus = 'NORMAL';
      this.highestSeverity = null;
      this.isAccountRestricted = false;
      this.restrictionEndDate = null;
      return;
    }

    // Find highest severity
    const severityOrder = { 'LOW': 1, 'MEDIUM': 2, 'HIGH': 3, 'CRITICAL': 4 };
    let maxSeverity = 0;
    let latestEndDate: Date | null = null;

    this.activePenalties.forEach(penalty => {
      const severityValue = severityOrder[penalty.severity];
      if (severityValue > maxSeverity) {
        maxSeverity = severityValue;
        this.highestSeverity = penalty.severity;
      }

      // Check for restrictions that disable forms
      if (penalty.penaltyType === 'ACCOUNT_RESTRICTION' || 
          penalty.penaltyType === 'TEMPORARY_SUSPENSION' ||
          penalty.penaltyType === 'PERMANENT_BAN') {
        this.isAccountRestricted = true;
        
        if (penalty.expiresAt) {
          const endDate = new Date(penalty.expiresAt);
          if (!latestEndDate || endDate > latestEndDate) {
            latestEndDate = endDate;
          }
        }
      }
    });

    this.restrictionEndDate = latestEndDate;

    // Set account status based on highest severity
    if (this.highestSeverity === 'CRITICAL' || this.activePenalties.some(p => p.penaltyType === 'PERMANENT_BAN')) {
      this.accountStatus = 'SUSPENDED';
    } else if (this.highestSeverity === 'HIGH' || this.activePenalties.some(p => p.penaltyType === 'TEMPORARY_SUSPENSION')) {
      this.accountStatus = 'SUSPENDED';
    } else if (this.highestSeverity === 'MEDIUM' || this.activePenalties.some(p => p.penaltyType === 'ACCOUNT_RESTRICTION')) {
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

  viewPenalties(): void {
    this.router.navigate(['/penalties']);
  }

  getPenaltyAlertClass(): string {
    if (this.activePenalties.length === 0) return '';
    
    const hasCritical = this.activePenalties.some(p => p.severity === 'CRITICAL');
    const hasHigh = this.activePenalties.some(p => p.severity === 'HIGH');
    
    if (hasCritical) return 'penalty-alert-critical';
    if (hasHigh) return 'penalty-alert-high';
    return 'penalty-alert-medium';
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
}
