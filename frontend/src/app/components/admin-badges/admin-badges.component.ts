import { Component, OnInit } from '@angular/core';
import { BadgeService } from '../../services/badge.service';
import { Badge, BadgeLevel, FreelancerStats } from '../../models/badge.model';

@Component({
  selector: 'app-admin-badges',
  templateUrl: './admin-badges.component.html',
  styleUrls: ['./admin-badges.component.css']
})
export class AdminBadgesComponent implements OnInit {
  badges: Badge[] = [];
  loading = false;
  error = '';
  showModal = false;
  editMode = false;
  saving = false;

  // Stats update form
  showStatsModal = false;
  statsForm: FreelancerStats = { freelancerEmail: '', freelancerName: '', missionsCompleted: 0, averageRating: 0 };
  statsLoading = false;
  statsSuccess = '';

  levels: BadgeLevel[] = ['BRONZE', 'SILVER', 'GOLD', 'PLATINUM'];

  form: Badge = {
    name: '', description: '', iconUrl: '', level: 'BRONZE',
    minMissionsCompleted: 0, minAverageRating: 0, active: true
  };
  editingId: number | null = null;

  constructor(private badgeService: BadgeService) {}

  ngOnInit(): void {
    this.loadBadges();
  }

  loadBadges(): void {
    this.loading = true;
    this.error = '';
    this.badgeService.getAllBadges().subscribe({
      next: (data) => { this.badges = data; this.loading = false; },
      error: () => { this.error = 'Failed to load badges. Make sure badge-service is running on port 8085.'; this.loading = false; }
    });
  }

  openCreate(): void {
    this.editMode = false;
    this.editingId = null;
    this.form = { name: '', description: '', iconUrl: '', level: 'BRONZE', minMissionsCompleted: 0, minAverageRating: 0, active: true };
    this.showModal = true;
  }

  openEdit(badge: Badge): void {
    this.editMode = true;
    this.editingId = badge.id!;
    this.form = { ...badge };
    this.showModal = true;
  }

  save(): void {
    this.saving = true;
    const obs = this.editMode
      ? this.badgeService.updateBadge(this.editingId!, this.form)
      : this.badgeService.createBadge(this.form);

    obs.subscribe({
      next: () => { this.showModal = false; this.saving = false; this.loadBadges(); },
      error: () => { this.saving = false; }
    });
  }

  delete(id: number): void {
    if (!confirm('Delete this badge?')) return;
    this.badgeService.deleteBadge(id).subscribe({ next: () => this.loadBadges() });
  }

  openStatsModal(): void {
    this.statsForm = { freelancerEmail: '', freelancerName: '', missionsCompleted: 0, averageRating: 0 };
    this.statsSuccess = '';
    this.showStatsModal = true;
  }

  submitStats(): void {
    this.statsLoading = true;
    this.statsSuccess = '';
    this.badgeService.updateFreelancerStats(this.statsForm).subscribe({
      next: (entry) => {
        this.statsLoading = false;
        this.statsSuccess = `Updated! ${entry.freelancerName} is now rank #${entry.rank} — Level: ${entry.currentLevel || 'None'}`;
      },
      error: () => { this.statsLoading = false; }
    });
  }

  getLevelClass(level: string): string {
    const map: Record<string, string> = { BRONZE: 'level-bronze', SILVER: 'level-silver', GOLD: 'level-gold', PLATINUM: 'level-platinum' };
    return map[level] || '';
  }

  getLevelIcon(level: string): string {
    const map: Record<string, string> = { BRONZE: 'fas fa-medal', SILVER: 'fas fa-medal', GOLD: 'fas fa-trophy', PLATINUM: 'fas fa-crown' };
    return map[level] || 'fas fa-award';
  }
}
