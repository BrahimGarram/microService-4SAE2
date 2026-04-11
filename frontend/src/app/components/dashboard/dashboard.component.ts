import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { ToastService } from '../../services/toast.service';
import { StatisticsService, DashboardStats } from '../../services/statistics.service';
import { ProjectService, Project } from '../../services/project.service';
import { Chart, registerables } from 'chart.js';

// Register Chart.js components
Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  
  @ViewChild('userGrowthChart', { static: false }) userGrowthChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('roleDistributionChart', { static: false }) roleDistributionChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('activityChart', { static: false }) activityChartRef!: ElementRef<HTMLCanvasElement>;
  
  stats: DashboardStats | null = null;
  loading = true;
  error: string | null = null;
  
  // Projects
  projects: Project[] = [];
  projectsLoading = false;
  projectsError: string | null = null;
  showAllProjects = false;
  
  // Chart instances
  private userGrowthChart: Chart | null = null;
  private roleDistributionChart: Chart | null = null;
  private activityChart: Chart | null = null;
  
  constructor(
    private toastService: ToastService,
    private statisticsService: StatisticsService,
    private projectService: ProjectService,
    private router: Router
  ) {}
  
  ngOnInit() {
    console.log('🔵 DASHBOARD: ngOnInit() called');
    this.loadStats();
    // Load projects immediately - don't wait for stats
    console.log('🔵 DASHBOARD: Calling loadProjects()');
    this.loadProjects();
    this.logTokenForPostman();
    setTimeout(() => {
      this.toastService.success('Welcome to Freelancify Admin Dashboard!');
    }, 500);
  }
  
  /**
   * Log JWT token to console for Postman testing
   */
  private logTokenForPostman() {
    // Try to get token from localStorage first
    const token = localStorage.getItem('kc-access-token');
    
    if (token) {
      try {
        // Decode token to get info
        const tokenPayload = JSON.parse(atob(token.split('.')[1]));
        
        console.log('');
        console.log('========================================');
        console.log('🔑 JWT TOKEN FOR POSTMAN TESTING');
        console.log('========================================');
        console.log('');
        console.log('📋 Full Token:');
        console.log(token);
        console.log('');
        console.log('📊 Token Info:');
        console.log('  - Username:', tokenPayload.preferred_username || tokenPayload.username || 'N/A');
        console.log('  - User ID:', tokenPayload.sub || 'N/A');
        console.log('  - Roles:', tokenPayload.realm_access?.roles || []);
        console.log('  - Expires:', tokenPayload.exp ? new Date(tokenPayload.exp * 1000).toLocaleString() : 'N/A');
        console.log('');
        console.log('📝 Use in Postman:');
        console.log('  Header: Authorization');
        console.log('  Value: Bearer ' + token);
        console.log('');
        console.log('🌐 Example API Call:');
        console.log('  GET http://localhost:8081/api/users/stats');
        console.log('  Headers:');
        console.log('    Authorization: Bearer ' + token);
        console.log('');
        console.log('  POST http://localhost:8081/api/users/update-countries');
        console.log('  Headers:');
        console.log('    Authorization: Bearer ' + token);
        console.log('    Content-Type: application/json');
        console.log('  Body:');
        console.log('    {');
        console.log('      "testIp": "8.8.8.8"');
        console.log('    }');
        console.log('');
        console.log('========================================');
        console.log('');
      } catch (error) {
        console.error('Error decoding token:', error);
        console.log('Raw token:', token);
      }
    } else {
      console.warn('No token found in localStorage. Please login first.');
    }
  }
  
  ngOnDestroy() {
    // Destroy charts to prevent memory leaks
    if (this.userGrowthChart) {
      this.userGrowthChart.destroy();
    }
    if (this.roleDistributionChart) {
      this.roleDistributionChart.destroy();
    }
    if (this.activityChart) {
      this.activityChart.destroy();
    }
  }
  
  /**
   * Load statistics from backend
   */
  loadStats() {
    this.loading = true;
    this.error = null;
    
    this.statisticsService.getDashboardStats().subscribe({
      next: (stats) => {
        this.stats = stats;
        this.loading = false;
        
        // Initialize charts after data is loaded
        setTimeout(() => {
          this.initCharts();
        }, 100);
      },
      error: (err) => {
        console.error('Error loading stats:', err);
        this.error = 'Failed to load dashboard statistics';
        this.loading = false;
        this.toastService.error('Failed to load dashboard statistics');
      }
    });
  }
  
  /**
   * Initialize all charts
   */
  initCharts() {
    if (!this.stats) return;
    
    this.initUserGrowthChart();
    this.initRoleDistributionChart();
    this.initActivityChart();
  }
  
  /**
   * Initialize user growth line chart
   */
  initUserGrowthChart() {
    if (!this.userGrowthChartRef || !this.stats) return;
    
    const ctx = this.userGrowthChartRef.nativeElement.getContext('2d');
    if (!ctx) return;
    
    if (this.userGrowthChart) {
      this.userGrowthChart.destroy();
    }
    
    this.userGrowthChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: this.stats.userGrowth.map(g => g.period),
        datasets: [{
          label: 'Total Users',
          data: this.stats.userGrowth.map(g => g.userCount),
          borderColor: '#FF6B35',
          backgroundColor: 'rgba(255, 107, 53, 0.1)',
          tension: 0.4,
          fill: true,
          borderWidth: 3
        }, {
          label: 'New Users',
          data: this.stats.userGrowth.map(g => g.newUsers),
          borderColor: '#4299e1',
          backgroundColor: 'rgba(66, 153, 225, 0.1)',
          tension: 0.4,
          fill: true,
          borderWidth: 3
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'top',
          },
          title: {
            display: false
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              stepSize: 1
            }
          }
        }
      }
    });
  }
  
  /**
   * Initialize role distribution pie chart
   */
  initRoleDistributionChart() {
    if (!this.roleDistributionChartRef || !this.stats) return;
    
    const ctx = this.roleDistributionChartRef.nativeElement.getContext('2d');
    if (!ctx) return;
    
    if (this.roleDistributionChart) {
      this.roleDistributionChart.destroy();
    }
    
    const colors = ['#FF6B35', '#4299e1', '#ed8936'];
    
    this.roleDistributionChart = new Chart(ctx, {
      type: 'pie',
      data: {
        labels: this.stats.roleDistribution.map(r => r.role),
        datasets: [{
          data: this.stats.roleDistribution.map(r => r.count),
          backgroundColor: colors.slice(0, this.stats.roleDistribution.length),
          borderWidth: 2,
          borderColor: '#fff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
          },
          tooltip: {
            callbacks: {
              label: (context) => {
                const label = context.label || '';
                const value = context.parsed || 0;
                const total = context.dataset.data.reduce((a: number, b: number) => a + b, 0);
                const percentage = ((value / total) * 100).toFixed(1);
                return `${label}: ${value} (${percentage}%)`;
              }
            }
          }
        }
      }
    });
  }
  
  /**
   * Initialize activity bar chart
   */
  initActivityChart() {
    if (!this.activityChartRef || !this.stats) return;
    
    const ctx = this.activityChartRef.nativeElement.getContext('2d');
    if (!ctx) return;
    
    if (this.activityChart) {
      this.activityChart.destroy();
    }
    
    this.activityChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: this.stats.activityByPeriod.map(a => a.period),
        datasets: [{
          label: 'Registrations',
          data: this.stats.activityByPeriod.map(a => a.registrations),
          backgroundColor: 'rgba(255, 107, 53, 0.8)',
          borderColor: '#FF6B35',
          borderWidth: 2
        }, {
          label: 'Active Users',
          data: this.stats.activityByPeriod.map(a => a.activeUsers),
          backgroundColor: 'rgba(66, 153, 225, 0.8)',
          borderColor: '#4299e1',
          borderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'top',
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              stepSize: 1
            }
          }
        }
      }
    });
  }
  
  /**
   * Format number with K/M suffixes
   */
  formatNumber(num: number): string {
    if (num >= 1000000) {
      return (num / 1000000).toFixed(1) + 'M';
    } else if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
  }
  
  /**
   * Get change indicator class
   */
  getChangeClass(change: number): string {
    return change >= 0 ? 'positive' : 'negative';
  }
  
  /**
   * Navigate to users page
   */
  navigateToUsers() {
    this.router.navigate(['/users']);
  }
  
  /**
   * Load recent projects
   */
  loadProjects() {
    console.log('🔵 DASHBOARD: loadProjects() called');
    this.projectsLoading = true;
    this.projectsError = null;
    console.log('🔵 DASHBOARD: Loading projects from http://localhost:8082/api/projects');
    
    this.projectService.getAllProjects().subscribe({
      next: (projects) => {
        console.log('🟢 DASHBOARD: Projects received:', projects);
        console.log('🟢 DASHBOARD: Projects type:', typeof projects);
        console.log('🟢 DASHBOARD: Projects is array:', Array.isArray(projects));
        console.log('🟢 DASHBOARD: Projects length:', projects?.length);
        
        if (!projects) {
          console.log('🟡 DASHBOARD: Projects is null/undefined');
          this.projects = [];
          this.projectsLoading = false;
          return;
        }
        
        if (!Array.isArray(projects)) {
          console.error('🔴 DASHBOARD: Projects is not an array:', projects);
          this.projects = [];
          this.projectsLoading = false;
          return;
        }
        
        if (projects.length === 0) {
          console.log('🟡 DASHBOARD: No projects in array');
          this.projects = [];
          this.projectsLoading = false;
          return;
        }
        
        // Sort by newest and take first 6
        const sortedProjects = [...projects].sort((a, b) => {
          const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
          const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
          return dateB - dateA;
        });
        
        this.projects = this.showAllProjects 
          ? sortedProjects 
          : sortedProjects.slice(0, 6);
        
        this.projectsLoading = false;
        this.projectsError = null; // Ensure error is cleared
        console.log('✅ DASHBOARD: Projects loaded successfully:', this.projects.length);
        console.log('✅ DASHBOARD: Projects data:', JSON.stringify(this.projects, null, 2));
        console.log('✅ DASHBOARD: Final state - Loading:', this.projectsLoading, 'Error:', this.projectsError, 'Count:', this.projects.length);
        console.log('✅ DASHBOARD: projectsLoading =', this.projectsLoading);
        console.log('✅ DASHBOARD: projectsError =', this.projectsError);
        console.log('✅ DASHBOARD: projects.length =', this.projects.length);
        console.log('✅ DASHBOARD: Should display?', !this.projectsLoading && !this.projectsError && this.projects.length > 0);
      },
      error: (error) => {
        console.error('🔴 DASHBOARD: Error loading projects:', error);
        console.error('🔴 DASHBOARD: Error details:', {
          status: error.status,
          statusText: error.statusText,
          message: error.message,
          url: error.url
        });
        
        if (error.status === 0) {
          this.projectsError = 'Cannot connect to project service. Please make sure the backend is running on port 8082.';
        } else if (error.status === 404) {
          this.projectsError = 'Project service endpoint not found. Please check if the backend is running.';
        } else {
          this.projectsError = `Failed to load projects: ${error.message || 'Unknown error'}`;
        }
        
        this.projects = [];
        this.projectsLoading = false;
      }
    });
  }

  /**
   * Toggle show all projects
   */
  toggleShowAllProjects() {
    this.showAllProjects = !this.showAllProjects;
    this.loadProjects();
  }

  /**
   * Get status class for project
   */
  getStatusClass(status: string): string {
    const statusMap: { [key: string]: string } = {
      'DRAFT': 'status-pending',
      'OPEN': 'status-active',
      'IN_PROGRESS': 'status-active',
      'COMPLETED': 'status-completed',
      'CANCELLED': 'status-cancelled'
    };
    return statusMap[status] || 'status-pending';
  }

  /**
   * Get status display text
   */
  getStatusDisplay(status: string): string {
    const statusMap: { [key: string]: string } = {
      'DRAFT': 'Draft',
      'OPEN': 'Open',
      'IN_PROGRESS': 'In Progress',
      'COMPLETED': 'Completed',
      'CANCELLED': 'Cancelled'
    };
    return statusMap[status] || status;
  }

  /**
   * Format currency
   */
  formatCurrency(amount?: number): string {
    if (amount === undefined || amount === null) return 'N/A';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    }).format(amount);
  }

  /**
   * Format date
   */
  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  /**
   * Track by function for ngFor
   */
  trackByProjectId(index: number, project: Project): number {
    return project.id || index;
  }

  /**
   * Navigate to projects page
   */
  navigateToProjects() {
    this.router.navigate(['/admin/projects']);
  }

  navigateToContracts() {
    this.router.navigate(['/admin/contracts']);
  }

  navigateToPayments() {
    this.router.navigate(['/admin/payments']);
  }
  
  /**
   * Refresh statistics
   */
  refreshStats() {
    this.loadStats();
  }
  
  // Expose Math to template
  Math = Math;
}
