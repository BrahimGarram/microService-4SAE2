import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { ContractService } from '../../services/contract.service';
import { PaymentService } from '../../services/payment.service';
import { ToastService } from '../../services/toast.service';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-contract-analytics',
  templateUrl: './contract-analytics.component.html',
  styleUrls: ['./contract-analytics.component.css']
})
export class ContractAnalyticsComponent implements OnInit, OnDestroy {

  @ViewChild('statusChart', { static: false }) statusChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('revenueChart', { static: false }) revenueChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('methodChart', { static: false }) methodChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('monthlyChart', { static: false }) monthlyChartRef!: ElementRef<HTMLCanvasElement>;

  contractStats: any = null;
  paymentStats: any = null;
  loading = true;

  private charts: Chart[] = [];

  constructor(
    private contractService: ContractService,
    private paymentService: PaymentService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  ngOnDestroy(): void {
    this.charts.forEach(c => c.destroy());
  }

  loadAll(): void {
    this.loading = true;
    this.contractService.getStats().subscribe({
      next: (cs) => {
        this.contractStats = cs;
        this.paymentService.getStats(new Date().getFullYear()).subscribe({
          next: (ps) => {
            this.paymentStats = ps;
            this.loading = false;
            setTimeout(() => this.initCharts(), 150);
          },
          error: () => { this.loading = false; this.toastService.error('Failed to load payment stats'); }
        });
      },
      error: () => { this.loading = false; this.toastService.error('Failed to load contract stats'); }
    });
  }

  private initCharts(): void {
    this.charts.forEach(c => c.destroy());
    this.charts = [];
    if (this.contractStats) {
      this.buildStatusChart();
      this.buildRevenueChart();
    }
    if (this.paymentStats) {
      this.buildMethodChart();
      this.buildMonthlyChart();
    }
  }

  private buildStatusChart(): void {
    const ctx = this.statusChartRef?.nativeElement?.getContext('2d');
    if (!ctx || !this.contractStats?.countByStatus) return;
    const labels = Object.keys(this.contractStats.countByStatus);
    const data = Object.values(this.contractStats.countByStatus) as number[];
    const colors = ['#48bb78','#FF6B35','#e53e3e','#718096','#e53e3e','#4299e1','#805ad5'];
    this.charts.push(new Chart(ctx, {
      type: 'doughnut',
      data: { labels, datasets: [{ data, backgroundColor: colors.slice(0, labels.length), borderWidth: 2, borderColor: '#fff' }] },
      options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } }
    }));
  }

  private buildRevenueChart(): void {
    const ctx = this.revenueChartRef?.nativeElement?.getContext('2d');
    if (!ctx || !this.contractStats?.revenueByFreelancer) return;
    const entries = Object.entries(this.contractStats.revenueByFreelancer).slice(0, 8);
    this.charts.push(new Chart(ctx, {
      type: 'bar',
      data: {
        labels: entries.map(([k]) => k.split('@')[0]),
        datasets: [{ label: 'Revenue', data: entries.map(([, v]) => v as number),
          backgroundColor: 'rgba(255,107,53,0.8)', borderColor: '#FF6B35', borderWidth: 2 }]
      },
      options: { responsive: true, maintainAspectRatio: false, indexAxis: 'y',
        plugins: { legend: { display: false } }, scales: { x: { beginAtZero: true } } }
    }));
  }

  private buildMethodChart(): void {
    const ctx = this.methodChartRef?.nativeElement?.getContext('2d');
    if (!ctx || !this.paymentStats?.revenueByMethod) return;
    const entries = Object.entries(this.paymentStats.revenueByMethod);
    this.charts.push(new Chart(ctx, {
      type: 'pie',
      data: {
        labels: entries.map(([k]) => k),
        datasets: [{ data: entries.map(([, v]) => v as number),
          backgroundColor: ['#667eea','#48bb78','#ed8936','#e53e3e'], borderWidth: 2, borderColor: '#fff' }]
      },
      options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } }
    }));
  }

  private buildMonthlyChart(): void {
    const ctx = this.monthlyChartRef?.nativeElement?.getContext('2d');
    if (!ctx || !this.paymentStats?.revenueByMonth) return;
    const entries = Object.entries(this.paymentStats.revenueByMonth);
    this.charts.push(new Chart(ctx, {
      type: 'line',
      data: {
        labels: entries.map(([k]) => k),
        datasets: [{ label: 'Monthly Revenue', data: entries.map(([, v]) => v as number),
          borderColor: '#667eea', backgroundColor: 'rgba(102,126,234,0.1)',
          tension: 0.4, fill: true, borderWidth: 3 }]
      },
      options: { responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true } } }
    }));
  }

  getKeys(obj: any): string[] { return obj ? Object.keys(obj) : []; }
  getValue(obj: any, key: string): any { return obj?.[key]; }
}
