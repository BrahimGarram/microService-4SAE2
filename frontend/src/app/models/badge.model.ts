export type BadgeLevel = 'BRONZE' | 'SILVER' | 'GOLD' | 'PLATINUM';

export interface Badge {
  id?: number;
  name: string;
  description: string;
  iconUrl?: string;
  level: BadgeLevel;
  minMissionsCompleted: number;
  minAverageRating: number;
  active: boolean;
  earnedAt?: string;
}

export interface LeaderboardEntry {
  rank: number;
  freelancerEmail: string;
  freelancerName: string;
  missionsCompleted: number;
  averageRating: number;
  currentLevel: BadgeLevel | null;
  currentBadgeName: string | null;
  badges: Badge[];
  // XP system
  totalXp: number;
  xpForCurrentLevel: number;
  xpForNextLevel: number;
  xpProgress: number;
  xpNeeded: number;
  progressPercent: number;
  nextLevelName: string;
}

export interface FreelancerStats {
  freelancerEmail: string;
  freelancerName: string;
  missionsCompleted: number;
  averageRating: number;
}
