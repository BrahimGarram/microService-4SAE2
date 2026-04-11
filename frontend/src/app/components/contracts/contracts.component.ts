import { Component, OnInit } from '@angular/core';
import { Contract, ContractStatus, PaymentType, ContractType } from '../../models/contract.model';
import { ContractService } from '../../services/contract.service';
import { ToastService } from '../../services/toast.service';
import { ContractPdfService, ContractPdfData } from '../../services/contract-pdf.service';


@Component({
  selector: 'app-contracts',
  templateUrl: './contracts.component.html',
  styleUrls: ['./contracts.component.css']
})
export class ContractsComponent implements OnInit {
  contracts: Contract[] = [];
  filteredContracts: Contract[] = [];
  loading = false;
  showModal = false;
  isEditMode = false;
  currentContract: Contract = this.getEmptyContract();
  searchTerm = '';
  filterStatus: ContractStatus | 'ALL' = 'ALL';
  contractStatuses = Object.values(ContractStatus);
  paymentTypes = Object.values(PaymentType);
  contractTypes = Object.values(ContractType);
  currencies = ['USD', 'EUR', 'GBP', 'MAD', 'CAD'];

  // Validation errors
  errors: { [key: string]: string } = {};
  
  // Signature data
  clientSignature: string = '';
  freelancerSignature: string = '';
  showSignatureModal = false;
  currentSignerRole: 'Client' | 'Freelancer' = 'Client';
  currentSignerName: string = '';
  contractForSigning: Contract | null = null;

  // Métier modals
  showDisputeModal = false;
  showResolveModal = false;
  showRenewModal = false;
  selectedContractForAction: Contract | null = null;
  disputeForm = { raisedBy: 'CLIENT', reason: '', description: '' };
  resolveText = '';
  renewForm: { newEndDate: string; newAmount: number | null; renewalNote: string } =
    { newEndDate: '', newAmount: null, renewalNote: '' };

  constructor(
    private contractService: ContractService, // Using real service with backend
    private toastService: ToastService,
    private contractPdfService: ContractPdfService
  ) {}

  ngOnInit(): void {
    this.loadContracts();
  }

  loadContracts(): void {
    this.loading = true;
    this.contractService.getAllContracts().subscribe({
      next: (data) => {
        this.contracts = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        this.toastService.error('Failed to load contracts');
        console.error('Error loading contracts:', error);
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.filteredContracts = this.contracts.filter(contract => {
      const matchesSearch = !this.searchTerm || 
        contract.idContract?.toString().includes(this.searchTerm) ||
        contract.contractTitle?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        contract.freelancerName?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        contract.clientName?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        contract.status.toLowerCase().includes(this.searchTerm.toLowerCase());
      
      const matchesStatus = this.filterStatus === 'ALL' || contract.status === this.filterStatus;
      
      return matchesSearch && matchesStatus;
    });
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  onStatusFilterChange(): void {
    this.applyFilters();
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.currentContract = this.getEmptyContract();
    this.errors = {};
    this.showModal = true;
  }

  openEditModal(contract: Contract): void {
    this.isEditMode = true;
    this.currentContract = { ...contract };
    this.errors = {};
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.errors = {};
    this.currentContract = this.getEmptyContract();
  }

  private validateContract(): boolean {
    this.errors = {};
    const c = this.currentContract;
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const today = new Date(); today.setHours(0, 0, 0, 0);

    if (!c.contractTitle?.trim())
      this.errors['contractTitle'] = 'Contract title is required.';
    else if (c.contractTitle.trim().length < 3)
      this.errors['contractTitle'] = 'Title must be at least 3 characters.';

    if (!c.freelancerName?.trim())
      this.errors['freelancerName'] = 'Freelancer name is required.';

    if (!c.freelancerEmail?.trim())
      this.errors['freelancerEmail'] = 'Freelancer email is required.';
    else if (!emailRegex.test(c.freelancerEmail))
      this.errors['freelancerEmail'] = 'Enter a valid email address.';

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

  saveContract(): void {
    if (!this.validateContract()) return;

    const contractData = {
      ...this.currentContract,
      startDate: this.formatDateForBackend(this.currentContract.startDate),
      endDate: this.formatDateForBackend(this.currentContract.endDate)
    };

    if (this.isEditMode && this.currentContract.idContract) {
      this.contractService.updateContract(this.currentContract.idContract, contractData).subscribe({
        next: () => {
          this.toastService.success('Contract updated successfully');
          this.loadContracts();
          this.closeModal();
        },
        error: (error) => {
          const msg = error?.error?.message || error?.message || 'Failed to update contract';
          this.toastService.error(msg);
        }
      });
    } else {
      this.contractService.createContract(contractData).subscribe({
        next: (createdContract) => {
          this.toastService.success('Contract created successfully');
          this.generateContractPdf(createdContract || contractData);
          this.loadContracts();
          this.closeModal();
        },
        error: (error) => {
          const msg = error?.error?.message || error?.message || 'Failed to create contract';
          this.toastService.error(msg);
        }
      });
    }
  }

  generateContractPdf(contract: Contract): void {
    const pdfData: ContractPdfData = {
      contractNumber: contract.idContract?.toString() || 'DRAFT',
      title: contract.contractTitle || 'Untitled Contract',
      clientName: contract.clientName || '',
      clientEmail: contract.clientEmail || '',
      freelancerName: contract.freelancerName || '',
      freelancerEmail: contract.freelancerEmail || '',
      startDate: this.formatDateForDisplay(contract.startDate),
      endDate: contract.endDate ? this.formatDateForDisplay(contract.endDate) : undefined,
      description: contract.projectDescription || 'No description provided',
      totalAmount: contract.contractAmount || 0,
      currency: contract.currency || 'USD',
      paymentTerms: `Payment due within ${contract.paymentTermsDays || 30} days. Payment type: ${contract.paymentType}`,
      deliverables: contract.deliverables ? contract.deliverables.split('\n').filter(d => d.trim()) : [],
      terms: contract.termsAndConditions || 'Standard terms and conditions apply.',
      clientSignature: this.clientSignature || undefined,
      freelancerSignature: this.freelancerSignature || undefined,
      clientSignatureDate: this.clientSignature ? new Date().toLocaleDateString() : undefined,
      freelancerSignatureDate: this.freelancerSignature ? new Date().toLocaleDateString() : undefined
    };

    this.contractPdfService.generateContractPdf(pdfData);
    this.toastService.success('Contract PDF generated successfully');
  }

  openSignatureModal(contract: Contract, role: 'Client' | 'Freelancer'): void {
    this.contractForSigning = contract;
    this.currentSignerRole = role;
    this.currentSignerName = role === 'Client' ? contract.clientName || '' : contract.freelancerName || '';
    this.showSignatureModal = true;
  }

  onSignatureSaved(signatureDataUrl: string): void {
    if (this.currentSignerRole === 'Client') {
      this.clientSignature = signatureDataUrl;
      this.toastService.success('Client signature saved');
    } else {
      this.freelancerSignature = signatureDataUrl;
      this.toastService.success('Freelancer signature saved');
    }
    this.showSignatureModal = false;
    
    // If both signatures are present, generate PDF automatically
    if (this.clientSignature && this.freelancerSignature && this.contractForSigning) {
      setTimeout(() => {
        if (confirm('Both signatures collected. Generate signed PDF now?')) {
          this.generateContractPdf(this.contractForSigning!);
          // Reset signatures after PDF generation
          this.clientSignature = '';
          this.freelancerSignature = '';
          this.contractForSigning = null;
        }
      }, 500);
    }
  }

  onSignatureModalClosed(): void {
    this.showSignatureModal = false;
  }

  private formatDateForDisplay(date: Date | string): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
  }

  private formatDateForBackend(date: Date | string): string {
    if (!date) return '';
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  deleteContract(id: number): void {
    if (confirm('Are you sure you want to delete this contract?')) {
      this.contractService.deleteContract(id).subscribe({
        next: () => {
          this.toastService.success('Contract deleted successfully');
          this.loadContracts();
        },
        error: (error) => {
          this.toastService.error('Failed to delete contract');
          console.error('Error deleting contract:', error);
        }
      });
    }
  }

  // ── Métier actions ────────────────────────────────────────────────────────

  activateContract(contract: Contract): void {
    this.contractService.activateContract(contract.idContract!).subscribe({
      next: () => { this.toastService.success('Contract activated'); this.loadContracts(); },
      error: (e) => this.toastService.error(e?.error || e?.message || 'Failed to activate')
    });
  }

  completeContract(contract: Contract): void {
    if (!confirm('Mark this contract as COMPLETED?')) return;
    this.contractService.completeContract(contract.idContract!).subscribe({
      next: () => { this.toastService.success('Contract completed'); this.loadContracts(); },
      error: (e) => this.toastService.error(e?.error || e?.message || 'Failed to complete')
    });
  }

  duplicateContract(contract: Contract): void {
    this.contractService.duplicateContract(contract.idContract!).subscribe({
      next: () => { this.toastService.success('Contract duplicated as PENDING'); this.loadContracts(); },
      error: (e) => this.toastService.error(e?.error || e?.message || 'Failed to duplicate')
    });
  }

  openDisputeModal(contract: Contract): void {
    this.selectedContractForAction = contract;
    this.disputeForm = { raisedBy: 'CLIENT', reason: '', description: '' };
    this.showDisputeModal = true;
  }

  submitDispute(): void {
    if (!this.disputeForm.reason.trim() || !this.disputeForm.description.trim()) {
      this.toastService.error('Please fill in all dispute fields'); return;
    }
    this.contractService.raiseDispute(this.selectedContractForAction!.idContract!, this.disputeForm).subscribe({
      next: () => { this.toastService.success('Dispute raised'); this.showDisputeModal = false; this.loadContracts(); },
      error: (e) => this.toastService.error(e?.error || 'Failed to raise dispute')
    });
  }

  openResolveModal(contract: Contract): void {
    this.selectedContractForAction = contract;
    this.resolveText = '';
    this.showResolveModal = true;
  }

  submitResolve(): void {
    if (!this.resolveText.trim()) { this.toastService.error('Please enter a resolution'); return; }
    this.contractService.resolveDispute(this.selectedContractForAction!.idContract!, this.resolveText).subscribe({
      next: () => { this.toastService.success('Dispute resolved'); this.showResolveModal = false; this.loadContracts(); },
      error: (e) => this.toastService.error(e?.error || 'Failed to resolve')
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
      next: () => { this.toastService.success('Contract renewed successfully'); this.showRenewModal = false; this.loadContracts(); },
      error: (e) => this.toastService.error(e?.error || 'Failed to renew')
    });
  }

  getStatusClass(status: ContractStatus): string {
    const statusClasses: { [key in ContractStatus]: string } = {
      [ContractStatus.ACTIVE]: 'status-active',
      [ContractStatus.PENDING]: 'status-pending',
      [ContractStatus.EXPIRED]: 'status-expired',
      [ContractStatus.CANCELLED]: 'status-cancelled',
      [ContractStatus.DISPUTED]: 'status-disputed',
      [ContractStatus.COMPLETED]: 'status-completed',
      [ContractStatus.RENEWED]: 'status-renewed'
    };
    return statusClasses[status];
  }

  private getEmptyContract(): Contract {
    return {
      contractTitle: '',
      freelancerName: '',
      freelancerEmail: '',
      clientName: '',
      clientEmail: '',
      projectDescription: '',
      startDate: '',
      endDate: '',
      status: ContractStatus.PENDING,
      contractAmount: 0,
      paymentType: PaymentType.FIXED_PRICE,
      currency: 'USD',
      contractType: ContractType.PROJECT_BASED,
      deliverables: '',
      termsAndConditions: '',
      paymentTermsDays: 30
    };
  }
}
