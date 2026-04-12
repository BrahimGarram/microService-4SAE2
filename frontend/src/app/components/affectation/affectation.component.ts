import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { AffectationTestService, AffectationTest, Status } from '../../services/affectation-test.service';
import { forkJoin } from 'rxjs';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

export interface AffectationStats {
  total: number;
  validatedTrue: number;
  validatedFalse: number;
  validatedFirstTime: number;
  scoreSuccess: number;
  scoreFail: number;
  byStatus: { status: Status; count: number }[];
}

@Component({
  selector: 'app-affectation',
  templateUrl: './affectation.component.html',
  styleUrls: ['./affectation.component.css']
})
export class AffectationComponent implements OnInit, OnDestroy {
  @ViewChild('chartValidated', { static: false }) chartValidatedRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('chartStatus', { static: false }) chartStatusRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('chartScore', { static: false }) chartScoreRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('chartEvolution', { static: false }) chartEvolutionRef!: ElementRef<HTMLCanvasElement>;

  stats: AffectationStats | null = null;
  list: AffectationTest[] = [];
  loading = true;
  error = '';
  /** Affectation sélectionnée pour le détail (modal) */
  selectedAffectation: AffectationTest | null = null;
  loadingDetail = false;
  /** Pagination : page courante (1-based) et taille de page */
  currentPage = 1;
  readonly pageSize = 10;

  private chartValidated: Chart | null = null;
  private chartStatus: Chart | null = null;
  private chartScore: Chart | null = null;
  private chartEvolution: Chart | null = null;

  readonly statusOrder: Status[] = ['IN_PROGRESS', 'COMPLETED', 'ABANDONED', 'FRAUD'];
  readonly statusLabels: Record<Status, string> = {
    IN_PROGRESS: 'In progress',
    COMPLETED: 'Completed',
    ABANDONED: 'Abandoned',
    FRAUD: 'Fraud'
  };

  constructor(private affectationService: AffectationTestService) {}

  ngOnInit(): void {
    this.load();
  }

  ngOnDestroy(): void {
    this.chartValidated?.destroy();
    this.chartStatus?.destroy();
    this.chartScore?.destroy();
    this.chartEvolution?.destroy();
  }

  load(): void {
    this.loading = true;
    this.error = '';

    forkJoin({
      list: this.affectationService.getAllAffectations(),
      validatedTrue: this.affectationService.getStatsValidatedTrue(),
      validatedFalse: this.affectationService.getStatsValidatedFalse(),
      validatedFirstTime: this.affectationService.getStatsValidatedFirstTime(),
      scoreSuccess: this.affectationService.getStatsScoreSuccess(),
      scoreFail: this.affectationService.getStatsScoreFail(),
      statusInProgress: this.affectationService.getStatsByStatus('IN_PROGRESS'),
      statusCompleted: this.affectationService.getStatsByStatus('COMPLETED'),
      statusAbandoned: this.affectationService.getStatsByStatus('ABANDONED'),
      statusFraud: this.affectationService.getStatsByStatus('FRAUD')
    }).subscribe({
      next: (res) => {
        this.list = res.list || [];
        this.currentPage = 1;
        this.stats = {
          total: this.list.length,
          validatedTrue: res.validatedTrue ?? 0,
          validatedFalse: res.validatedFalse ?? 0,
          validatedFirstTime: res.validatedFirstTime ?? 0,
          scoreSuccess: res.scoreSuccess ?? 0,
          scoreFail: res.scoreFail ?? 0,
          byStatus: [
            { status: 'IN_PROGRESS', count: res.statusInProgress ?? 0 },
            { status: 'COMPLETED', count: res.statusCompleted ?? 0 },
            { status: 'ABANDONED', count: res.statusAbandoned ?? 0 },
            { status: 'FRAUD', count: res.statusFraud ?? 0 }
          ]
        };
        this.loading = false;
        setTimeout(() => this.initCharts(), 150);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Unable to load assignment statistics.';
      }
    });
  }

  private initCharts(): void {
    if (!this.stats) return;
    this.initChartValidated();
    this.initChartStatus();
    this.initChartScore();
    this.initChartEvolution();
  }

  private initChartValidated(): void {
    if (!this.chartValidatedRef?.nativeElement || !this.stats) return;
    this.chartValidated?.destroy();
    const ctx = this.chartValidatedRef.nativeElement.getContext('2d');
    if (!ctx) return;
    this.chartValidated = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['Passed', 'Not passed'],
        datasets: [{
          data: [this.stats.validatedTrue, this.stats.validatedFalse],
          backgroundColor: ['#FF6B35', '#1a202c'],
          borderColor: ['#fff', '#fff'],
          borderWidth: 2,
          hoverOffset: 8
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom' },
          tooltip: {
            callbacks: {
              label: (c) => {
                const t = this.stats!.validatedTrue + this.stats!.validatedFalse;
                const p = t ? ((c.parsed / t) * 100).toFixed(1) : '0';
                return `${c.label}: ${c.parsed} (${p}%)`;
              }
            }
          }
        }
      }
    });
  }

  private initChartStatus(): void {
    if (!this.chartStatusRef?.nativeElement || !this.stats) return;
    this.chartStatus?.destroy();
    const ctx = this.chartStatusRef.nativeElement.getContext('2d');
    if (!ctx) return;
    const labels = this.stats.byStatus.map(s => this.statusLabels[s.status]);
    const data = this.stats.byStatus.map(s => s.count);
    this.chartStatus = new Chart(ctx, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: 'Count',
          data,
          backgroundColor: ['#1a202c', '#FF6B35', '#64748b'],
          borderColor: ['#1a202c', '#ea580c', '#475569'],
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: { backgroundColor: '#1a202c' }
        },
        scales: {
          y: { beginAtZero: true, ticks: { stepSize: 1 } }
        }
      }
    });
  }

  private initChartScore(): void {
    if (!this.chartScoreRef?.nativeElement || !this.stats) return;
    this.chartScore?.destroy();
    const ctx = this.chartScoreRef.nativeElement.getContext('2d');
    if (!ctx) return;
    this.chartScore = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['Score ≥ 50%', 'Score < 50%'],
        datasets: [{
          data: [this.stats.scoreSuccess, this.stats.scoreFail],
          backgroundColor: ['#FF6B35', '#1a202c'],
          borderColor: ['#fff', '#fff'],
          borderWidth: 2,
          hoverOffset: 8
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom' },
          tooltip: {
            callbacks: {
              label: (c) => {
                const t = this.stats!.scoreSuccess + this.stats!.scoreFail;
                const p = t ? ((c.parsed / t) * 100).toFixed(1) : '0';
                return `${c.label}: ${c.parsed} (${p}%)`;
              }
            }
          }
        }
      }
    });
  }

  private initChartEvolution(): void {
    if (!this.chartEvolutionRef?.nativeElement) return;
    this.chartEvolution?.destroy();
    const ctx = this.chartEvolutionRef.nativeElement.getContext('2d');
    if (!ctx) return;

    const completed = this.list.filter(a => a.status === 'COMPLETED' && a.score != null && a.freelancerId != null);
    const byFreelancer = new Map<number, AffectationTest[]>();
    completed.forEach(a => {
      const fid = a.freelancerId!;
      if (!byFreelancer.has(fid)) byFreelancer.set(fid, []);
      byFreelancer.get(fid)!.push(a);
    });
    byFreelancer.forEach(arr => arr.sort((x, y) => new Date(x.assignedAt || 0).getTime() - new Date(y.assignedAt || 0).getTime()));

    const colors = ['#FF6B35', '#1a202c', '#16a34a', '#2563eb', '#7c3aed', '#0891b2', '#ea580c', '#64748b'];
    const datasets = Array.from(byFreelancer.entries()).map(([fid, arr], idx) => {
      const color = colors[idx % colors.length];
      return {
        label: this.getFreelancerShortLabel(fid),
        data: arr.map(a => ({ x: new Date(a.assignedAt!).getTime(), y: a.score! })),
        borderColor: color,
        backgroundColor: color,
        borderWidth: 2.5,
        fill: false,
        tension: 0.2,
        pointRadius: 8,
        pointHoverRadius: 12,
        pointBackgroundColor: color,
        pointBorderColor: '#fff',
        pointBorderWidth: 2,
        pointStyle: 'circle'
      };
    });

    this.chartEvolution = new Chart(ctx, {
      type: 'line',
      data: { datasets },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: true },
        plugins: {
          legend: { position: 'bottom' },
          tooltip: {
            callbacks: {
              label: (c) => `${c.dataset.label}: ${c.parsed.y}%`
            }
          }
        },
        scales: {
          x: {
            type: 'linear',
            title: { display: true, text: 'Date' },
            ticks: {
              callback: (v: number | string) => {
                const n = typeof v === 'number' ? v : Number(v);
                if (isNaN(n)) return String(v);
                const d = new Date(n);
                return isNaN(d.getTime()) ? String(v) : d.toLocaleDateString('en-GB', { day: '2-digit', month: '2-digit', year: '2-digit' });
              }
            }
          },
          y: {
            min: 0,
            max: 100,
            title: { display: true, text: 'Score (%)' },
            ticks: { stepSize: 10 }
          }
        }
      }
    });
  }

  /**
   * Libellé compact pour le freelancer dans la légende du graphique.
   * Utilise le username (ou nom complet) tronqué, sinon fallback sur #id.
   */
  private getFreelancerShortLabel(fid: number): string {
    const withName = this.list.find(
      a => a.freelancerId === fid && (a.freelancerFirstName || a.freelancerLastName)
    );
    let label = '';
    if (withName) {
      const full =
        ((withName.freelancerFirstName || '') + ' ' + (withName.freelancerLastName || '')).trim() ||
        withName.freelancerFirstName ||
        withName.freelancerLastName ||
        '';
      label = full || `#${fid}`;
    } else {
      label = `#${fid}`;
    }
    return label.length > 12 ? label.slice(0, 12) + '…' : label;
  }

  /** Liste triée par date (plus récent en premier). */
  getSortedList(): AffectationTest[] {
    return this.list
      .slice()
      .sort((a, b) => (new Date(b.assignedAt || 0).getTime() - new Date(a.assignedAt || 0).getTime()));
  }

  /** Liste paginée : 10 affectations pour la page courante. */
  getPaginatedList(): AffectationTest[] {
    const sorted = this.getSortedList();
    const start = (this.currentPage - 1) * this.pageSize;
    return sorted.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    const total = this.getSortedList().length;
    return total === 0 ? 1 : Math.ceil(total / this.pageSize);
  }

  get paginationStart(): number {
    const total = this.getSortedList().length;
    if (total === 0) return 0;
    return (this.currentPage - 1) * this.pageSize + 1;
  }

  get paginationEnd(): number {
    const total = this.getSortedList().length;
    const end = this.currentPage * this.pageSize;
    return Math.min(end, total);
  }

  goToPage(page: number): void {
    const p = Math.max(1, Math.min(page, this.totalPages));
    this.currentPage = p;
  }

  formatDate(s: string | undefined): string {
    if (!s) return '—';
    return new Date(s).toLocaleDateString('en-GB', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getStatusLabel(status: string | undefined): string {
    if (!status) return '—';
    return this.statusLabels[status as Status] ?? status;
  }

  /**
   * Pour les tests complétés : affiche le temps passé (ex. "2 min 30 s").
   * Pour les autres : "Abandonné" ou "En cours".
   */
  formatTimeSpent(a: AffectationTest): string {
    const status = a.status;
    if (status === 'COMPLETED' && a.timeSpent != null) {
      const sec = a.timeSpent;
      const min = Math.floor(sec / 60);
      const s = sec % 60;
      if (min === 0) return s + ' s';
      if (s === 0) return min + ' min';
      return min + ' min ' + s + ' s';
    }
    if (status === 'ABANDONED') return 'Abandoned';
    if (status === 'FRAUD') return 'Fraud';
    if (status === 'IN_PROGRESS') return 'In progress';
    return '—';
  }

  get successRate(): number {
    if (!this.stats || this.stats.total === 0) return 0;
    return Math.round((this.stats.validatedTrue / this.stats.total) * 100);
  }

  /** Scores par domaine pour l'affectation sélectionnée (décodés depuis domainScoresJson). */
  get selectedDomainScores(): { [domain: string]: number } {
    if (!this.selectedAffectation || !this.selectedAffectation.domainScoresJson) return {};
    try {
      return JSON.parse(this.selectedAffectation.domainScoresJson) || {};
    } catch {
      return {};
    }
  }

  get selectedDomainKeys(): string[] {
    return Object.keys(this.selectedDomainScores || {});
  }

  openDetailModal(a: AffectationTest): void {
    if (!a.id) return;
    this.selectedAffectation = null;
    this.loadingDetail = true;
    this.affectationService.getAffectationById(a.id).subscribe({
      next: (full) => {
        this.selectedAffectation = full;
        this.loadingDetail = false;
      },
      error: () => {
        this.loadingDetail = false;
      }
    });
  }

  closeDetailModal(): void {
    this.selectedAffectation = null;
  }
}
