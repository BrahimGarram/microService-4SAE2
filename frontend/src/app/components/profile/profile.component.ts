import { Component, OnInit } from '@angular/core';
import { UserService, User, UpdateUserRequest, Skill, PortfolioItem, Language, SocialLink } from '../../services/user.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  
  user: User | null = null;
  updateRequest: UpdateUserRequest = {};
  loading = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  isEditMode = false;
  uploadingPicture = false;
  selectedFile: File | null = null;
  previewImage: string | null = null;
  
  // Form arrays for managing lists
  newSkill: Skill = { name: '', level: '', yearsOfExperience: undefined };
  newPortfolioItem: PortfolioItem = { title: '', description: '', url: '', technologies: '' };
  newLanguage: Language = { name: '', code: '', proficiency: '' };
  newSocialLink: SocialLink = { platform: '', url: '', username: '' };
  
  // Availability options
  availabilityOptions = [
    { value: 'ONLINE', label: 'Online' },
    { value: 'BUSY', label: 'Busy' },
    { value: 'OFFLINE', label: 'Offline' }
  ];
  
  // Common timezones
  timezones = [
    'UTC',
    'America/New_York',
    'America/Chicago',
    'America/Denver',
    'America/Los_Angeles',
    'Europe/London',
    'Europe/Paris',
    'Europe/Berlin',
    'Asia/Dubai',
    'Asia/Tokyo',
    'Asia/Shanghai',
    'Australia/Sydney'
  ];
  
  constructor(
    private userService: UserService,
    private toastService: ToastService
  ) {}
  
  ngOnInit() {
    this.isEditMode = false;
    this.errorMessage = null;
    this.successMessage = null;
    this.loadUser();
  }
  
  /**
   * Load current user profile
   */
  loadUser() {
    this.loading = true;
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
        this.initializeUpdateRequest(user);
        this.loading = false;
        this.errorMessage = null;
      },
      error: (err) => {
        console.error('Error loading user:', err);
        this.errorMessage = 'Failed to load user profile. Please try again.';
        this.toastService.error(this.errorMessage);
        this.loading = false;
      }
    });
  }
  
  /**
   * Initialize update request with current user data
   */
  initializeUpdateRequest(user: User) {
    this.updateRequest = {
      username: user.username,
      email: user.email,
      bio: user.bio || '',
      city: user.city || '',
      timezone: user.timezone || '',
      hourlyRate: user.hourlyRate,
      availability: user.availability || 'OFFLINE',
      skills: user.skills ? [...user.skills] : [],
      portfolioItems: user.portfolioItems ? [...user.portfolioItems] : [],
      languages: user.languages ? [...user.languages] : [],
      socialLinks: user.socialLinks ? [...user.socialLinks] : []
    };
  }
  
  /**
   * Toggle edit mode
   */
  toggleEditMode() {
    if (this.isEditMode) {
      this.cancelEdit();
    } else {
      this.isEditMode = true;
      if (this.user) {
        this.initializeUpdateRequest(this.user);
      }
    }
  }
  
  /**
   * Cancel edit mode
   */
  cancelEdit() {
    this.isEditMode = false;
    this.errorMessage = null;
    this.successMessage = null;
    this.selectedFile = null;
    this.previewImage = null;
    if (this.user) {
      this.initializeUpdateRequest(this.user);
    }
  }
  
  /**
   * Handle profile picture file selection
   */
  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      // Validate file type
      if (!file.type.startsWith('image/')) {
        this.toastService.error('Please select an image file');
        return;
      }
      
      // Validate file size (5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.toastService.error('Image size must be less than 5MB');
        return;
      }
      
      this.selectedFile = file;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.previewImage = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }
  
  /**
   * Upload profile picture
   */
  uploadProfilePicture() {
    if (!this.user || !this.selectedFile) return;
    
    this.uploadingPicture = true;
    this.userService.uploadProfilePicture(this.user.id, this.selectedFile).subscribe({
      next: (updatedUser) => {
        this.user = updatedUser;
        this.toastService.success('Profile picture uploaded successfully!');
        this.selectedFile = null;
        this.previewImage = null;
        this.uploadingPicture = false;
      },
      error: (err) => {
        console.error('Error uploading profile picture:', err);
        this.toastService.error('Failed to upload profile picture. Please try again.');
        this.uploadingPicture = false;
      }
    });
  }
  
  /**
   * Add skill
   */
  addSkill() {
    if (!this.newSkill.name) {
      this.toastService.error('Please enter a skill name');
      return;
    }
    
    if (!this.updateRequest.skills) {
      this.updateRequest.skills = [];
    }
    
    this.updateRequest.skills.push({ ...this.newSkill });
    this.newSkill = { name: '', level: '', yearsOfExperience: undefined };
  }
  
  /**
   * Remove skill
   */
  removeSkill(index: number) {
    if (this.updateRequest.skills) {
      this.updateRequest.skills.splice(index, 1);
    }
  }
  
  /**
   * Add portfolio item
   */
  addPortfolioItem() {
    if (!this.newPortfolioItem.title) {
      this.toastService.error('Please enter a project title');
      return;
    }
    
    if (!this.updateRequest.portfolioItems) {
      this.updateRequest.portfolioItems = [];
    }
    
    this.updateRequest.portfolioItems.push({ ...this.newPortfolioItem });
    this.newPortfolioItem = { title: '', description: '', url: '', technologies: '' };
  }
  
  /**
   * Remove portfolio item
   */
  removePortfolioItem(index: number) {
    if (this.updateRequest.portfolioItems) {
      this.updateRequest.portfolioItems.splice(index, 1);
    }
  }
  
  /**
   * Add language
   */
  addLanguage() {
    if (!this.newLanguage.name) {
      this.toastService.error('Please enter a language name');
      return;
    }
    
    if (!this.updateRequest.languages) {
      this.updateRequest.languages = [];
    }
    
    this.updateRequest.languages.push({ ...this.newLanguage });
    this.newLanguage = { name: '', code: '', proficiency: '' };
  }
  
  /**
   * Remove language
   */
  removeLanguage(index: number) {
    if (this.updateRequest.languages) {
      this.updateRequest.languages.splice(index, 1);
    }
  }
  
  /**
   * Add social link
   */
  addSocialLink() {
    if (!this.newSocialLink.platform || !this.newSocialLink.url) {
      this.toastService.error('Please enter platform and URL');
      return;
    }
    
    if (!this.updateRequest.socialLinks) {
      this.updateRequest.socialLinks = [];
    }
    
    this.updateRequest.socialLinks.push({ ...this.newSocialLink });
    this.newSocialLink = { platform: '', url: '', username: '' };
  }
  
  /**
   * Remove social link
   */
  removeSocialLink(index: number) {
    if (this.updateRequest.socialLinks) {
      this.updateRequest.socialLinks.splice(index, 1);
    }
  }
  
  /**
   * Update user profile
   */
  updateProfile() {
    if (!this.user) return;
    
    // Validate form
    if (!this.updateRequest.username || !this.updateRequest.email) {
      this.errorMessage = 'Please fill in all required fields.';
      this.toastService.error(this.errorMessage);
      return;
    }
    
    this.loading = true;
    this.successMessage = null;
    this.errorMessage = null;
    
    this.userService.updateUser(this.user.id, this.updateRequest).subscribe({
      next: (updatedUser) => {
        this.user = updatedUser;
        this.successMessage = 'Profile updated successfully!';
        this.toastService.success(this.successMessage);
        this.isEditMode = false;
        this.loading = false;
        
        // Clear success message after 3 seconds
        setTimeout(() => {
          this.successMessage = null;
        }, 3000);
      },
      error: (err) => {
        console.error('Error updating user:', err);
        let errorMsg = 'Failed to update profile. Please try again.';
        
        if (err.error?.message) {
          errorMsg = err.error.message;
        } else if (err.status === 403) {
          errorMsg = 'You do not have permission to update this profile.';
        } else if (err.status === 404) {
          errorMsg = 'User profile not found.';
        }
        
        this.errorMessage = errorMsg;
        this.toastService.error(this.errorMessage);
        this.loading = false;
      }
    });
  }
  
  /**
   * Get availability label
   */
  getAvailabilityLabel(value: string | undefined): string {
    const option = this.availabilityOptions.find(opt => opt.value === value);
    return option ? option.label : value || 'Offline';
  }
  
  /**
   * Get availability icon
   */
  getAvailabilityIcon(value: string | undefined): string {
    switch (value) {
      case 'ONLINE': return 'fa-circle';
      case 'BUSY': return 'fa-clock';
      case 'OFFLINE': return 'fa-circle';
      default: return 'fa-circle';
    }
  }
  
  /**
   * Get availability color
   */
  getAvailabilityColor(value: string | undefined): string {
    switch (value) {
      case 'ONLINE': return '#48bb78';
      case 'BUSY': return '#ed8936';
      case 'OFFLINE': return '#a0aec0';
      default: return '#a0aec0';
    }
  }
}
