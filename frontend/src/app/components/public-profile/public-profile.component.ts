import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService, User, RatingService, Rating, CreateRatingRequest } from '../../services/user.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-public-profile',
  templateUrl: './public-profile.component.html',
  styleUrls: ['./public-profile.component.css']
})
export class PublicProfileComponent implements OnInit {
  
  user: User | null = null;
  loading = false;
  errorMessage: string | null = null;
  userId: number | null = null;
  username: string | null = null;
  
  // Rating data
  ratings: Rating[] = [];
  loadingRatings = false;
  showRatingForm = false;
  currentUser: User | null = null;
  hasRated = false;
  newRating: CreateRatingRequest = { rating: 5, comment: '' };
  
  // Expose router to template (for navigation)
  get routerInstance() {
    return this._routerInstance;
  }
  
  constructor(
    private userService: UserService,
    private ratingService: RatingService,
    private route: ActivatedRoute,
    private _routerInstance: Router,
    private toastService: ToastService
  ) {}
  
  ngOnInit() {
    this.loading = true;
    this.errorMessage = null;
    
    // Load current user to check if they can rate
    this.loadCurrentUser();
    
    // Check if we have an ID or username in the route
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.userId = +params['id'];
        this.loadUserById(this.userId);
      } else if (params['username']) {
        this.username = params['username'];
        if (this.username) {
          this.loadUserByUsername(this.username);
        }
      } else {
        this.errorMessage = 'Invalid user identifier';
        this.loading = false;
      }
    });
  }
  
  /**
   * Load current user
   */
  loadCurrentUser() {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
      },
      error: (err) => {
        console.error('Error loading current user:', err);
      }
    });
  }
  
  /**
   * Load user by ID
   */
  loadUserById(id: number) {
    this.loading = true;
    this.userService.getUserById(id).subscribe({
      next: (user) => {
        this.user = user;
        this.loadRatings(id);
        this.checkIfUserHasRated(id);
        this.loading = false;
        this.errorMessage = null;
      },
      error: (err) => {
        console.error('Error loading user:', err);
        this.errorMessage = 'Failed to load user profile. User may not exist.';
        this.toastService.error(this.errorMessage);
        this.loading = false;
      }
    });
  }
  
  /**
   * Load user by username
   */
  loadUserByUsername(username: string) {
    this.loading = true;
    this.userService.getUserByUsername(username).subscribe({
      next: (user) => {
        this.user = user;
        this.userId = user.id;
        this.loadRatings(user.id);
        this.checkIfUserHasRated(user.id);
        this.loading = false;
        this.errorMessage = null;
      },
      error: (err) => {
        console.error('Error loading user:', err);
        this.errorMessage = 'Failed to load user profile. User may not exist.';
        this.toastService.error(this.errorMessage);
        this.loading = false;
      }
    });
  }
  
  /**
   * Load ratings for user
   */
  loadRatings(userId: number) {
    this.loadingRatings = true;
    this.ratingService.getRatingsForUser(userId).subscribe({
      next: (ratings) => {
        this.ratings = ratings;
        this.loadingRatings = false;
      },
      error: (err) => {
        console.error('Error loading ratings:', err);
        this.loadingRatings = false;
      }
    });
  }
  
  /**
   * Check if current user has already rated this user
   */
  checkIfUserHasRated(userId: number) {
    if (!this.currentUser || this.currentUser.id === userId) {
      this.hasRated = false;
      return;
    }
    
    this.ratingService.getRatingsForUser(userId).subscribe({
      next: (ratings) => {
        this.hasRated = ratings.some(r => r.raterId === this.currentUser?.id);
      },
      error: (err) => {
        console.error('Error checking rating:', err);
      }
    });
  }
  
  /**
   * Submit rating
   */
  submitRating() {
    if (!this.user || !this.currentUser || !this.newRating.rating) {
      return;
    }
    
    if (this.currentUser.id === this.user.id) {
      this.toastService.error('You cannot rate yourself');
      return;
    }
    
    this.ratingService.createRating(this.user.id, this.newRating).subscribe({
      next: (rating) => {
        this.toastService.success('Rating submitted successfully!');
        this.showRatingForm = false;
        this.hasRated = true;
        this.loadRatings(this.user!.id);
        // Reload user to update average rating
        if (this.userId) {
          this.loadUserById(this.userId);
        }
        this.newRating = { rating: 5, comment: '' };
      },
      error: (err) => {
        console.error('Error submitting rating:', err);
        this.toastService.error('Failed to submit rating. Please try again.');
      }
    });
  }
  
  /**
   * Delete rating
   */
  deleteRating(ratingId: number) {
    if (confirm('Are you sure you want to delete this rating?')) {
      this.ratingService.deleteRating(ratingId).subscribe({
        next: () => {
          this.toastService.success('Rating deleted successfully!');
          this.hasRated = false;
          if (this.user) {
            this.loadRatings(this.user.id);
            // Reload user to update average rating
            if (this.userId) {
              this.loadUserById(this.userId);
            }
          }
        },
        error: (err) => {
          console.error('Error deleting rating:', err);
          this.toastService.error('Failed to delete rating. Please try again.');
        }
      });
    }
  }
  
  /**
   * Get star rating display
   */
  getStarRating(rating: number): string {
    return '★'.repeat(rating) + '☆'.repeat(5 - rating);
  }
  
  /**
   * Get availability label
   */
  getAvailabilityLabel(value: string | undefined): string {
    const options = [
      { value: 'ONLINE', label: 'Online' },
      { value: 'BUSY', label: 'Busy' },
      { value: 'OFFLINE', label: 'Offline' }
    ];
    const option = options.find(opt => opt.value === value);
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
  
  /**
   * Navigate to user's profile edit page if it's the current user
   */
  navigateToEdit() {
    if (this.user) {
      this._routerInstance.navigate(['/profile']);
    }
  }
}
