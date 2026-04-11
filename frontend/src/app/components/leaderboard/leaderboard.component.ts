import { Component, OnInit } from '@angular/core';
import { BadgeService } from '../../services/badge.service';
import { LeaderboardEntry } from '../../models/badge.model';

@Component({
  selector: 'app-leaderboard',
  templateUrl: './leaderboard.component.html',
  styleUrls: ['./leaderboard.component.css']
})
export class LeaderboardComponent implements OnInit {
  entries: LeaderboardEntry[] = [];
  loading = false;
  error = '';
  selectedEntry: LeaderboardEntry | null = null;

  constructor(private badgeService: BadgeService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.badgeService.getLeaderboard().subscribe({
      next: (data) => { this.entries = data; this.loading = false; },
      error: () => { this.error = 'Failed to load leaderboard. Make sure badge-service is running.'; this.loading = false; }
    });
  }

  getRankIcon(rank: number): string {
    if (rank === 1) return 'fas fa-crown rank-gold';
    if (rank === 2) return 'fas fa-medal rank-silver';
    if (rank === 3) return 'fas fa-medal rank-bronze';
    return 'fas fa-hashtag rank-default';
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

  getStars(rating: number): number[] {
    return Array(Math.round(rating)).fill(0);
  }

  getEmptyStars(rating: number): number[] {
    return Array(5 - Math.round(rating)).fill(0);
  }

  viewDetails(entry: LeaderboardEntry): void {
    this.selectedEntry = entry;
  }

  formatEarnedDate(dateStr: string | undefined): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
