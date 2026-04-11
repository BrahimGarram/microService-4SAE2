import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserService, User } from '../../services/user.service';
import { FollowService } from '../../services/user.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-feed-abonnements',
  templateUrl: './feed-abonnements.component.html',
  styleUrls: ['./feed-abonnements.component.css']
})
export class FeedAbonnementsComponent implements OnInit {
  users: User[] = [];
  followingIds: Set<number> = new Set();
  currentUserId: number | null = null;
  loading = false;
  loadingFollow: Set<number> = new Set();
  error: string | null = null;

  constructor(
    private userService: UserService,
    private followService: FollowService,
    private router: Router,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.userService.getCurrentUser().subscribe({
      next: (me) => {
        this.currentUserId = me.id;
        this.loadFollowingAndUsers();
      },
      error: () => {
        this.error = 'Session expirée. Connectez-vous.';
        this.loading = false;
      }
    });
  }

  loadFollowingAndUsers(): void {
    this.loading = true;
    this.error = null;
    this.followService.getFollowingIds().subscribe({
      next: (ids) => {
        this.followingIds = new Set(ids);
        this.loadUsers();
      },
      error: () => {
        this.followingIds = new Set();
        this.loadUsers();
      }
    });
  }

  loadUsers(): void {
    this.userService.getPublicUsers().subscribe({
      next: (list) => {
        this.users = (list || []).filter(u => u.id !== this.currentUserId);
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger les utilisateurs.';
        this.loading = false;
      }
    });
  }

  isFollowing(userId: number): boolean {
    return this.followingIds.has(userId);
  }

  toggleFollow(user: User): void {
    if (this.loadingFollow.has(user.id)) return;
    const follow = !this.isFollowing(user.id);
    this.loadingFollow.add(user.id);
    const req = follow
      ? this.followService.follow(user.id)
      : this.followService.unfollow(user.id);
    req.subscribe({
      next: () => {
        if (follow) this.followingIds.add(user.id);
        else this.followingIds.delete(user.id);
        this.loadingFollow.delete(user.id);
        this.toastService.success(follow ? `Vous suivez ${user.username}` : `Vous ne suivez plus ${user.username}`);
      },
      error: () => {
        this.loadingFollow.delete(user.id);
        this.toastService.error('Action impossible. Réessayez.');
      }
    });
  }

  goToFeed(): void {
    this.router.navigate(['/feed']);
  }
}
