import { Component, OnInit, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { PostService, Post, PostType, PostComment, FeedParams } from '../../services/post.service';

export interface CaptionSegment {
  type: 'text' | 'hashtag' | 'mention';
  value: string;
}

@Component({
  selector: 'app-feed',
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.css']
})
export class FeedComponent implements OnInit {
  posts: Post[] = [];
  page = 0;
  size = 5;
  totalPages = 0;
  loading = false;
  loadingMore = false;
  activeTab: 'pour-toi' | 'abonnements' = 'pour-toi';
  filterType: PostType | null = null;
  sort: 'recent' | 'popular' = 'recent';
  searchQuery = '';
  hashtagQuery = '';
  showCreateModal = false;
  createType: PostType = 'TEXT';
  createCaption = '';
  createFile: File | null = null;
  creating = false;
  createError = '';
  showCommentsForPostId: number | null = null;
  comments: PostComment[] = [];
  commentsPage = 0;
  commentsTotalPages = 0;
  loadingComments = false;
  addCommentText = '';
  submittingComment = false;
  heartAnimPostId: number | null = null;
  carouselIndex: Record<number, number> = {};
  fullScreenMode = false;

  constructor(
    private postService: PostService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (typeof window !== 'undefined' && window.location.search) {
      const params = new URLSearchParams(window.location.search);
      const postId = params.get('post');
      if (postId) this.hashtagQuery = '';
      const tag = params.get('hashtag');
      if (tag) this.hashtagQuery = tag;
    }
    this.loadFeed();
  }

  @HostListener('window:scroll', [])
  onScroll(): void {
    if (this.loadingMore || this.page >= this.totalPages) return;
    const threshold = 200;
    const pos = window.innerHeight + window.scrollY;
    const max = document.body.scrollHeight - threshold;
    if (pos >= max) this.loadMore();
  }

  getFeedParams(pageOffset = 0): FeedParams {
    return {
      page: this.page + pageOffset,
      size: this.size,
      type: this.filterType ?? undefined,
      sort: this.sort,
      search: this.searchQuery || undefined,
      hashtag: this.hashtagQuery || undefined,
      followingOnly: this.activeTab === 'abonnements'
    };
  }

  loadFeed(): void {
    this.loading = true;
    this.page = 0;
    this.postService.getFeed({ ...this.getFeedParams(0), page: 0 }).subscribe({
      next: (res) => {
        this.posts = res.content;
        this.page = res.number;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  loadMore(): void {
    if (this.page + 1 >= this.totalPages) return;
    this.loadingMore = true;
    this.postService.getFeed(this.getFeedParams(1)).subscribe({
      next: (res) => {
        this.posts = [...this.posts, ...res.content];
        this.page = res.number;
        this.totalPages = res.totalPages;
        this.loadingMore = false;
      },
      error: () => { this.loadingMore = false; }
    });
  }

  setTab(tab: 'pour-toi' | 'abonnements'): void {
    this.activeTab = tab;
    this.loadFeed();
  }

  setFilter(type: PostType | null): void {
    this.filterType = type;
    this.loadFeed();
  }

  setSort(s: 'recent' | 'popular'): void {
    this.sort = s;
    this.loadFeed();
  }

  onSearch(): void {
    this.loadFeed();
  }

  goToHashtag(tag: string): void {
    const t = tag.startsWith('#') ? tag : '#' + tag;
    this.hashtagQuery = t;
    this.router.navigate(['/feed'], { queryParams: { hashtag: t.replace('#', '') } });
    this.loadFeed();
  }

  toggleLike(post: Post): void {
    if (post.likedByCurrentUser) {
      this.postService.unlike(post.id).subscribe({
        next: () => {
          post.likedByCurrentUser = false;
          post.likeCount--;
        }
      });
    } else {
      this.postService.like(post.id).subscribe({
        next: () => {
          post.likedByCurrentUser = true;
          post.likeCount++;
        }
      });
    }
  }

  onDoubleTap(post: Post, event: MouseEvent): void {
    if (post.likedByCurrentUser) return;
    this.postService.like(post.id).subscribe({
      next: () => {
        post.likedByCurrentUser = true;
        post.likeCount++;
        this.heartAnimPostId = post.id;
        setTimeout(() => { this.heartAnimPostId = null; }, 800);
      }
    });
  }

  sharePost(post: Post): void {
    const url = this.postService.getPostShareUrl(post.id);
    if (navigator.share) {
      navigator.share({
        title: 'Post',
        url,
        text: post.caption || ''
      }).catch(() => this.copyShareUrl(url));
    } else {
      this.copyShareUrl(url);
    }
  }

  private copyShareUrl(url: string): void {
    navigator.clipboard.writeText(url).then(() => {
      alert('Lien copié dans le presse-papiers.');
    });
  }

  openComments(post: Post): void {
    this.showCommentsForPostId = post.id;
    this.comments = [];
    this.commentsPage = 0;
    this.addCommentText = '';
    this.loadComments();
  }

  closeComments(): void {
    this.showCommentsForPostId = null;
  }

  loadComments(): void {
    if (this.showCommentsForPostId == null) return;
    this.loadingComments = true;
    this.postService.getComments(this.showCommentsForPostId, this.commentsPage, 20).subscribe({
      next: (res) => {
        if (this.commentsPage === 0) this.comments = res.content;
        else this.comments = [...this.comments, ...res.content];
        this.commentsTotalPages = res.totalPages;
        this.loadingComments = false;
      },
      error: () => { this.loadingComments = false; }
    });
  }

  submitComment(): void {
    if (this.showCommentsForPostId == null || !this.addCommentText.trim()) return;
    this.submittingComment = true;
    this.postService.addComment(this.showCommentsForPostId, this.addCommentText.trim()).subscribe({
      next: (newComment) => {
        this.comments = [newComment, ...this.comments];
        const post = this.posts.find(p => p.id === this.showCommentsForPostId);
        if (post) post.commentCount++;
        this.addCommentText = '';
        this.submittingComment = false;
      },
      error: () => { this.submittingComment = false; }
    });
  }

  parseCaption(caption: string | undefined): CaptionSegment[] {
    if (!caption) return [];
    const segments: CaptionSegment[] = [];
    const regex = /(#\w+)|(@\w+)/g;
    let lastIndex = 0;
    let m;
    while ((m = regex.exec(caption)) !== null) {
      if (m.index > lastIndex) {
        segments.push({ type: 'text', value: caption.slice(lastIndex, m.index) });
      }
      segments.push({
        type: m[0].startsWith('#') ? 'hashtag' : 'mention',
        value: m[0]
      });
      lastIndex = regex.lastIndex;
    }
    if (lastIndex < caption.length) {
      segments.push({ type: 'text', value: caption.slice(lastIndex) });
    }
    return segments.length ? segments : [{ type: 'text', value: caption }];
  }

  goToMention(username: string): void {
    this.router.navigate(['/user/username', username.replace('@', '')]);
  }

  getMediaList(post: Post): string[] {
    if (post.mediaUrls && post.mediaUrls.length > 0) return post.mediaUrls;
    if (post.mediaUrl) return [post.mediaUrl];
    return [];
  }

  getCarouselIndex(postId: number): number {
    return this.carouselIndex[postId] ?? 0;
  }

  setCarouselIndex(postId: number, index: number): void {
    this.carouselIndex[postId] = index;
  }

  openCreateModal(): void {
    this.showCreateModal = true;
    this.createType = 'TEXT';
    this.createCaption = '';
    this.createFile = null;
    this.createError = '';
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
    this.createFile = null;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) this.createFile = input.files[0];
  }

  submitPost(): void {
    if (this.createType !== 'TEXT' && !this.createFile) {
      this.createError = 'Veuillez sélectionner un fichier.';
      return;
    }
    this.creating = true;
    this.createError = '';
    this.postService.createPost(this.createType, this.createCaption || null, this.createFile).subscribe({
      next: (newPost) => {
        this.posts = [newPost, ...this.posts];
        this.creating = false;
        this.closeCreateModal();
      },
      error: (err) => {
        this.createError = err.error?.message || 'Erreur lors de la création.';
        this.creating = false;
      }
    });
  }

  toggleFullScreen(): void {
    this.fullScreenMode = !this.fullScreenMode;
  }

  toggleVideoMute(event: Event): void {
    const video = event.target as HTMLVideoElement;
    if (video) video.muted = !video.muted;
  }

  carouselPrev(postId: number): void {
    const current = this.getCarouselIndex(postId);
    this.setCarouselIndex(postId, Math.max(0, current - 1));
  }

  carouselNext(postId: number, post: Post): void {
    const list = this.getMediaList(post);
    const current = this.getCarouselIndex(postId);
    this.setCarouselIndex(postId, Math.min(list.length - 1, current + 1));
  }
}
