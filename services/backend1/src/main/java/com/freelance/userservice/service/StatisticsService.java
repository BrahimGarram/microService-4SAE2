package com.freelance.userservice.service;

import com.freelance.userservice.dto.*;
import com.freelance.userservice.model.User;
import com.freelance.userservice.model.UserRole;
import com.freelance.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Statistics Service
 * 
 * Calculates dashboard statistics from user data
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StatisticsService {
    
    private final UserRepository userRepository;
    
    /**
     * Get all dashboard statistics
     * @return DashboardStatsDTO with all statistics
     */
    public DashboardStatsDTO getDashboardStats() {
        log.info("Calculating dashboard statistics");
        
        RealTimeStatsDTO realTimeStats = calculateRealTimeStats();
        List<UserGrowthDTO> userGrowth = calculateUserGrowth();
        List<RoleDistributionDTO> roleDistribution = calculateRoleDistribution();
        List<ActivityPeriodDTO> activityByPeriod = calculateActivityByPeriod();
        PerformanceIndicatorsDTO performanceIndicators = calculatePerformanceIndicators();
        List<CountryStatsDTO> topCountries = calculateTopCountries();
        
        return DashboardStatsDTO.builder()
                .realTimeStats(realTimeStats)
                .userGrowth(userGrowth)
                .roleDistribution(roleDistribution)
                .activityByPeriod(activityByPeriod)
                .performanceIndicators(performanceIndicators)
                .topCountries(topCountries)
                .build();
    }
    
    /**
     * Calculate real-time statistics
     */
    private RealTimeStatsDTO calculateRealTimeStats() {
        long totalUsers = userRepository.count();
        
        // New users this month
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        long newUsersThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);
        
        // Active users today (placeholder - would require activity tracking)
        long activeUsersToday = totalUsers; // Placeholder
        
        // Projects in progress (placeholder - would come from Project Service)
        long projectsInProgress = 0L; // Placeholder
        
        // Total revenue (placeholder - would come from Payment Service)
        double totalRevenue = 0.0; // Placeholder
        
        // Calculate percentage changes from last month
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfMonth.minusSeconds(1);
        long usersLastMonth = userRepository.countByCreatedAtBetween(startOfLastMonth, endOfLastMonth);
        
        double totalUsersChange = calculatePercentageChange(totalUsers, usersLastMonth);
        double newUsersChange = calculatePercentageChange(newUsersThisMonth, usersLastMonth);
        double activeUsersChange = 0.0; // Placeholder
        double projectsChange = 0.0; // Placeholder
        double revenueChange = 0.0; // Placeholder
        
        return RealTimeStatsDTO.builder()
                .totalUsers(totalUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .activeUsersToday(activeUsersToday)
                .projectsInProgress(projectsInProgress)
                .totalRevenue(totalRevenue)
                .totalUsersChange(totalUsersChange)
                .newUsersChange(newUsersChange)
                .activeUsersChange(activeUsersChange)
                .projectsChange(projectsChange)
                .revenueChange(revenueChange)
                .build();
    }
    
    /**
     * Calculate user growth over time (last 6 months)
     */
    private List<UserGrowthDTO> calculateUserGrowth() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Object[]> monthlyCounts = userRepository.countUsersByMonth(sixMonthsAgo);
        
        // Get all users to calculate cumulative counts
        List<User> allUsers = userRepository.findAll();
        Map<String, Long> monthlyMap = monthlyCounts.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> ((Number) arr[1]).longValue()
                ));
        
        // Generate list of last 6 months
        List<UserGrowthDTO> growth = new ArrayList<>();
        LocalDateTime current = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        for (int i = 5; i >= 0; i--) {
            LocalDateTime month = current.minusMonths(i);
            String periodKey = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String periodLabel = month.format(DateTimeFormatter.ofPattern("MMM"));
            
            long newUsers = monthlyMap.getOrDefault(periodKey, 0L);
            
            // Calculate cumulative count up to this month
            long cumulativeCount = allUsers.stream()
                    .filter(u -> u.getCreatedAt().isBefore(month.plusMonths(1)))
                    .count();
            
            growth.add(UserGrowthDTO.builder()
                    .period(periodLabel)
                    .userCount(cumulativeCount)
                    .newUsers(newUsers)
                    .build());
        }
        
        return growth;
    }
    
    /**
     * Calculate role distribution
     */
    private List<RoleDistributionDTO> calculateRoleDistribution() {
        long totalUsers = userRepository.count();
        if (totalUsers == 0) {
            return Collections.emptyList();
        }
        
        List<RoleDistributionDTO> distribution = new ArrayList<>();
        
        for (UserRole role : UserRole.values()) {
            long count = userRepository.countByRole(role);
            double percentage = (count * 100.0) / totalUsers;
            
            distribution.add(RoleDistributionDTO.builder()
                    .role(role.name())
                    .count(count)
                    .percentage(Math.round(percentage * 100.0) / 100.0) // Round to 2 decimals
                    .build());
        }
        
        return distribution;
    }
    
    /**
     * Calculate activity by period (last 7 days)
     */
    private List<ActivityPeriodDTO> calculateActivityByPeriod() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> dailyCounts = userRepository.countUsersByDay(sevenDaysAgo);
        
        Map<String, Long> dailyMap = dailyCounts.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> ((Number) arr[1]).longValue()
                ));
        
        List<ActivityPeriodDTO> activity = new ArrayList<>();
        LocalDateTime current = LocalDateTime.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime day = current.minusDays(i);
            String periodKey = day.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String periodLabel = day.format(DateTimeFormatter.ofPattern("EEE"));
            
            long registrations = dailyMap.getOrDefault(periodKey, 0L);
            long activeUsers = registrations; // Placeholder - would require activity tracking
            
            activity.add(ActivityPeriodDTO.builder()
                    .period(periodLabel)
                    .registrations(registrations)
                    .activeUsers(activeUsers)
                    .build());
        }
        
        return activity;
    }
    
    /**
     * Calculate performance indicators
     */
    private PerformanceIndicatorsDTO calculatePerformanceIndicators() {
        // Placeholder values - would require additional tracking
        double conversionRate = 75.5; // Placeholder
        double averageSessionTime = 12.3; // Placeholder (minutes)
        double retentionRate = 68.2; // Placeholder (%)
        long completedProjects = 0L; // Placeholder - would come from Project Service
        long cancelledProjects = 0L; // Placeholder - would come from Project Service
        
        double completionRate = 0.0;
        if (completedProjects + cancelledProjects > 0) {
            completionRate = (completedProjects * 100.0) / (completedProjects + cancelledProjects);
        }
        
        return PerformanceIndicatorsDTO.builder()
                .conversionRate(conversionRate)
                .averageSessionTime(averageSessionTime)
                .retentionRate(retentionRate)
                .completedProjects(completedProjects)
                .cancelledProjects(cancelledProjects)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .build();
    }
    
    /**
     * Calculate top countries
     * Gets real country statistics from database
     */
    private List<CountryStatsDTO> calculateTopCountries() {
        List<Object[]> countryCounts = userRepository.countUsersByCountry();
        long totalUsers = userRepository.count();
        
        if (totalUsers == 0) {
            return Collections.emptyList();
        }
        
        List<CountryStatsDTO> countries = new ArrayList<>();
        
        for (Object[] row : countryCounts) {
            String country = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            double percentage = (count * 100.0) / totalUsers;
            
            countries.add(CountryStatsDTO.builder()
                    .country(country != null ? country : "Unknown")
                    .userCount(count)
                    .percentage(Math.round(percentage * 100.0) / 100.0)
                    .build());
        }
        
        // Limit to top 10 countries
        return countries.stream()
                .limit(10)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate percentage change between two values
     * @param current Current value
     * @param previous Previous value
     * @return Percentage change (positive or negative)
     */
    private double calculatePercentageChange(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return Math.round(((current - previous) * 100.0 / previous) * 100.0) / 100.0;
    }
}
