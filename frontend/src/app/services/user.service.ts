import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * User Interface
 * Matches the UserDTO from backend
 */
export interface Skill {
  id?: number;
  name: string;
  level?: string;
  yearsOfExperience?: number;
}

export interface PortfolioItem {
  id?: number;
  title: string;
  description?: string;
  url?: string;
  imageUrl?: string;
  technologies?: string;
  completedDate?: string;
  displayOrder?: number;
}

export interface Language {
  id?: number;
  name: string;
  code?: string;
  proficiency?: string;
}

export interface SocialLink {
  id?: number;
  platform: string;
  url: string;
  username?: string;
}

export interface User {
  id: number;
  keycloakId?: string;
  username: string;
  email?: string;
  role: 'USER' | 'FREELANCER' | 'ADMIN';
  createdAt: string;
  updatedAt?: string;
  country?: string;
  profilePicture?: string;
  bio?: string;
  city?: string;
  timezone?: string;
  hourlyRate?: number;
  availability?: 'ONLINE' | 'BUSY' | 'OFFLINE';
  verified?: boolean;
  skills?: Skill[];
  portfolioItems?: PortfolioItem[];
  languages?: Language[];
  socialLinks?: SocialLink[];
  averageRating?: number;
  ratingCount?: number;
}

/**
 * Update User Request Interface
 */
export interface UpdateUserRequest {
  username?: string;
  email?: string;
  bio?: string;
  city?: string;
  timezone?: string;
  hourlyRate?: number;
  availability?: 'ONLINE' | 'BUSY' | 'OFFLINE';
  skills?: Skill[];
  portfolioItems?: PortfolioItem[];
  languages?: Language[];
  socialLinks?: SocialLink[];
}

/**
 * User Service
 * 
 * Provides methods to interact with the User Microservice backend.
 * All requests automatically include JWT token via AuthInterceptor.
 */
@Injectable({
  providedIn: 'root'
})
export class UserService {
  
  private apiUrl = 'http://localhost:8081/api/users';  // Backend runs on port 8081
  
  constructor(private http: HttpClient) {}
  
  /**
   * Get current authenticated user
   * GET /api/users/me
   */
  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`);
  }
  
  /**
   * Get all users (Admin only)
   * GET /api/users
   */
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }
  
  /**
   * Get public users (for browsing/searching)
   * GET /api/users/public
   * Any authenticated user can access this
   * Returns only public information (no email, keycloakId, etc.)
   */
  getPublicUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/public`);
  }
  
  /**
   * Get user by ID (public profile)
   * GET /api/users/{id}
   * Any authenticated user can view other users' profiles
   */
  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }
  
  /**
   * Get user by username (public profile)
   * GET /api/users/username/{username}
   * Any authenticated user can view other users' profiles
   */
  getUserByUsername(username: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/username/${username}`);
  }
  
  /**
   * Sync user from Keycloak to database
   * POST /api/users/sync
   * Called after first login to create user profile
   */
  syncUser(): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/sync`, {});
  }
  
  /**
   * Update user profile
   * PUT /api/users/{id}
   */
  updateUser(id: number, request: UpdateUserRequest): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, request);
  }
  
  /**
   * Delete user (Admin only)
   * DELETE /api/users/{id}
   */
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
  
  /**
   * Request password reset email
   * POST /api/users/forgot-password
   * Public endpoint (no authentication required)
   */
  forgotPassword(email: string): Observable<{ message: string; token?: string }> {
    return this.http.post<{ message: string; token?: string }>(`${this.apiUrl}/forgot-password`, { email });
  }
  
  /**
   * Reset password using token
   * POST /api/users/reset-password
   * Public endpoint (no authentication required)
   */
  resetPassword(token: string, newPassword: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/reset-password`, { 
      token, 
      newPassword 
    });
  }
  
  /**
   * Upload profile picture
   * POST /api/users/{id}/profile-picture
   * @param userId User ID
   * @param file File to upload
   */
  uploadProfilePicture(userId: number, file: File): Observable<User> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<User>(`${this.apiUrl}/${userId}/profile-picture`, formData);
  }
}

/**
 * Rating Interface
 */
export interface Rating {
  id?: number;
  raterId: number;
  raterUsername: string;
  ratedUserId: number;
  rating: number; // 1 to 5
  comment?: string;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Create Rating Request Interface
 */
export interface CreateRatingRequest {
  rating: number; // 1 to 5
  comment?: string;
}

/**
 * Rating Service
 */
@Injectable({
  providedIn: 'root'
})
export class RatingService {
  
  private apiUrl = 'http://localhost:8081/api/users';
  
  constructor(private http: HttpClient) {}
  
  /**
   * Create or update a rating for a user
   * POST /api/users/{id}/ratings
   */
  createRating(userId: number, request: CreateRatingRequest): Observable<Rating> {
    return this.http.post<Rating>(`${this.apiUrl}/${userId}/ratings`, request);
  }
  
  /**
   * Get all ratings for a user
   * GET /api/users/{id}/ratings
   */
  getRatingsForUser(userId: number): Observable<Rating[]> {
    return this.http.get<Rating[]>(`${this.apiUrl}/${userId}/ratings`);
  }
  
  /**
   * Delete a rating
   * DELETE /api/users/ratings/{ratingId}
   */
  deleteRating(ratingId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/ratings/${ratingId}`);
  }
}

/**
 * Message Interface
 */
export interface Message {
  id?: number;
  senderId: number;
  senderUsername: string;
  receiverId: number;
  receiverUsername: string;
  subject?: string;
  content: string;
  isRead?: boolean;
  sentAt?: string;
  readAt?: string;
}

/**
 * Send Message Request Interface
 */
export interface SendMessageRequest {
  receiverId: number;
  subject?: string;
  content: string;
}

/**
 * Message Service
 */
@Injectable({
  providedIn: 'root'
})
export class MessageService {
  
  private apiUrl = 'http://localhost:8081/api/messages';
  
  constructor(private http: HttpClient) {}
  
  /**
   * Send a message
   * POST /api/messages
   */
  sendMessage(request: SendMessageRequest): Observable<Message> {
    return this.http.post<Message>(this.apiUrl, request);
  }
  
  /**
   * Get conversation between two users
   * GET /api/messages/conversation/{userId}
   */
  getConversation(userId: number): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.apiUrl}/conversation/${userId}`);
  }
  
  /**
   * Get list of conversation partners
   * GET /api/messages/conversations
   */
  getConversations(): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/conversations`);
  }
  
  /**
   * Get received messages
   * GET /api/messages/received
   */
  getReceivedMessages(): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.apiUrl}/received`);
  }
  
  /**
   * Get sent messages
   * GET /api/messages/sent
   */
  getSentMessages(): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.apiUrl}/sent`);
  }
  
  /**
   * Get unread messages
   * GET /api/messages/unread
   */
  getUnreadMessages(): Observable<{ count: number; messages: Message[] }> {
    return this.http.get<{ count: number; messages: Message[] }>(`${this.apiUrl}/unread`);
  }
  
  /**
   * Mark message as read
   * PUT /api/messages/{messageId}/read
   */
  markAsRead(messageId: number): Observable<Message> {
    return this.http.put<Message>(`${this.apiUrl}/${messageId}/read`, {});
  }
  
  /**
   * Mark conversation as read
   * PUT /api/messages/conversation/{userId}/read
   */
  markConversationAsRead(userId: number): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/conversation/${userId}/read`, {});
  }
  
  /**
   * Delete a message
   * DELETE /api/messages/{messageId}
   */
  deleteMessage(messageId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${messageId}`);
  }
}
