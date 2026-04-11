import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Statistics Interfaces
 * Match the DTOs from backend
 */
export interface RealTimeStats {
  totalUsers: number;
  newUsersThisMonth: number;
  activeUsersToday: number;
  projectsInProgress: number;
  totalRevenue: number;
  totalUsersChange: number;
  newUsersChange: number;
  activeUsersChange: number;
  projectsChange: number;
  revenueChange: number;
}

export interface UserGrowth {
  period: string;
  userCount: number;
  newUsers: number;
}

export interface RoleDistribution {
  role: string;
  count: number;
  percentage: number;
}

export interface ActivityPeriod {
  period: string;
  registrations: number;
  activeUsers: number;
}

export interface PerformanceIndicators {
  conversionRate: number;
  averageSessionTime: number;
  retentionRate: number;
  completedProjects: number;
  cancelledProjects: number;
  completionRate: number;
}

export interface CountryStats {
  country: string;
  userCount: number;
  percentage: number;
}

export interface DashboardStats {
  realTimeStats: RealTimeStats;
  userGrowth: UserGrowth[];
  roleDistribution: RoleDistribution[];
  activityByPeriod: ActivityPeriod[];
  performanceIndicators: PerformanceIndicators;
  topCountries: CountryStats[];
}

/**
 * Statistics Service
 * 
 * Provides methods to fetch dashboard statistics from the backend.
 */
@Injectable({
  providedIn: 'root'
})
export class StatisticsService {
  
  private apiUrl = 'http://localhost:8081/api/users';  // Backend runs on port 8081
  
  constructor(private http: HttpClient) {}
  
  /**
   * Get all dashboard statistics
   * GET /api/users/stats
   * Admin only
   */
  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/stats`);
  }
}
