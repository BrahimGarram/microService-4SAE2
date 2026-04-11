import { Component, OnInit } from '@angular/core';
import { BadgeService } from '../../services/badge.service';
import { LeaderboardEntry } from '../../models/badge.model';

@Component({
  selector: 'app-my-badges',
  templateUrl: './my-badges.component.html',
  styleUrls: ['./my-badges.component.css']
})
export class MyBadgesComponent implements OnInit {
  profile: LeaderboardEntry | null = null;
  loading = false;
  error = '';

  constructor(private badgeService: BadgeService) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading = true;
    this.error = '';

    // Get email from localStorage token
    const email = this.getEmailFromToken();
    if (!email) {
      this.error = 'Could not determine your email. Please log in again.';
      this.loading = false;
      return;
    }

    this.badgeService.getFreelancerProfile(email).subscribe({
      next: (data) => { this.profile = data; this.loading = false; },
      error: (err) => {
        if (err.status === 404) {
          this.error = 'No profile found yet. Complete missions to earn badges!';
        } else {
          this.error = 'Failed to load your badges. Make sure badge-service is running.';
        }
        this.loading = false;
      }
    });
  }

  private getEmailFromToken(): string | null {
    try {
      const token = localStorage.getItem('kc-access-token');
      if (!token) return null;
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.email || null;
    } catch {
      return null;
    }
  }

  getLevelClass(level: string | null): string {
    if (!level) return '';
    const map: Record<string, string> = { BRONZE: 'level-bronze', SILVER: 'level-silver', GOLD: 'level-gold', PLATINUM: 'level-platinum' };
    return map[level] || '';
  }

  getLevelIcon(level: string | null): string {
    if (!level) return 'fas fa-user';
    const map: Record<string, string> = { BRONZE: 'fas fa-medal', SILVER: 'fas fa-medal', GOLD: 'fas fa-trophy', PLATINUM: 'fas fa-crown' };
    return map[level] || 'fas fa-award';
  }

  getXpProgressPercent(): number {
    return this.profile?.progressPercent ?? 0;
  }

  getXpLabel(): string {
    if (!this.profile) return '';
    if (this.profile.nextLevelName === 'MAX') return 'PLATINUM atteint — niveau maximum !';
    return `${this.profile.xpNeeded} XP pour atteindre ${this.profile.nextLevelName}`;
  }

  getProgressToNextLevel(): number {
    if (!this.profile) return 0;
    const missions = this.profile.missionsCompleted;
    if (missions < 5) return (missions / 5) * 100;
    if (missions < 15) return ((missions - 5) / 10) * 100;
    if (missions < 30) return ((missions - 15) / 15) * 100;
    return 100;
  }

  getNextLevelLabel(): string {
    if (!this.profile) return '';
    const missions = this.profile.missionsCompleted;
    if (missions < 5) return `${5 - missions} missions to BRONZE`;
    if (missions < 15) return `${15 - missions} missions to SILVER`;
    if (missions < 30) return `${30 - missions} missions to GOLD`;
    return 'PLATINUM reached!';
  }

  getStars(rating: number): number[] {
    return Array(Math.round(rating)).fill(0);
  }

  getEmptyStars(rating: number): number[] {
    return Array(5 - Math.round(rating)).fill(0);
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
