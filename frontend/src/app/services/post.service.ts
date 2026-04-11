import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export type PostType = 'VIDEO' | 'IMAGE' | 'TEXT';

export interface Post {
  id: number;
  userId: number;
  username: string;
  userProfilePictureUrl?: string;
  type: PostType;
  mediaUrl?: string;
  mediaUrls?: string[];
  caption?: string;
  createdAt: string;
  likeCount: number;
  commentCount: number;
  likedByCurrentUser: boolean;
}

export interface PostComment {
  id: number;
  postId: number;
  userId: number;
  username: string;
  userProfilePictureUrl?: string;
  content: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface FeedParams {
  page?: number;
  size?: number;
  type?: PostType | null;
  sort?: 'recent' | 'popular';
  search?: string | null;
  hashtag?: string | null;
  followingOnly?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private apiUrl = `${environment.apiGatewayUrl}/api/posts`;

  constructor(private http: HttpClient) {}

  getFeed(params: FeedParams = {}): Observable<PageResponse<Post>> {
    let p = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 10));
    if (params.type != null) p = p.set('type', params.type);
    if (params.sort != null) p = p.set('sort', params.sort);
    if (params.search != null && params.search !== '') p = p.set('search', params.search);
    if (params.hashtag != null && params.hashtag !== '') p = p.set('hashtag', params.hashtag);
    if (params.followingOnly === true) p = p.set('followingOnly', 'true');
    return this.http.get<PageResponse<Post>>(`${this.apiUrl}/feed`, { params: p });
  }

  getPost(id: number): Observable<Post> {
    return this.http.get<Post>(`${this.apiUrl}/${id}`);
  }

  getComments(postId: number, page = 0, size = 20): Observable<PageResponse<PostComment>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<PageResponse<PostComment>>(`${this.apiUrl}/${postId}/comments`, { params });
  }

  addComment(postId: number, content: string): Observable<PostComment> {
    return this.http.post<PostComment>(`${this.apiUrl}/${postId}/comments`, { content });
  }

  createPost(type: PostType, caption: string | null, file: File | null): Observable<Post> {
    const form = new FormData();
    form.append('type', type);
    if (caption != null && caption !== '') form.append('caption', caption);
    if (file != null) form.append('file', file);
    return this.http.post<Post>(this.apiUrl, form);
  }

  like(postId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${postId}/like`, null);
  }

  unlike(postId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${postId}/like`);
  }

  getPostShareUrl(postId: number): string {
    return `${window.location.origin}/feed?post=${postId}`;
  }
}
