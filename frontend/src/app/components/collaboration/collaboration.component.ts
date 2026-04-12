import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  CollaborationApiService,
  CollaborationFullDto,
  CollaborationPublicDto
} from '../../services/collaboration-api.service';
import { ToastService } from '../../services/toast.service';
import { isAdminRole, isCollaborateurRole } from '../../utils/role-utils';

@Component({
  selector: 'app-collaboration',
  templateUrl: './collaboration.component.html',
  styleUrls: ['./collaboration.component.css']
})
export class CollaborationComponent implements OnInit {
  readonly canSubmitAds = isCollaborateurRole() || isAdminRole();

  restrictedMessage = false;
  mine: CollaborationFullDto[] = [];
  allApproved: CollaborationPublicDto[] = [];
  loading = false;
  submitting = false;

  form = {
    type: 'IMAGE' as 'IMAGE' | 'VIDEO',
    priority: 'LOW' as 'LOW' | 'MEDIUM' | 'HIGH',
    publishedAt: ''
  };
  selectedFile: File | null = null;

  constructor(
    private route: ActivatedRoute,
    private api: CollaborationApiService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((q) => {
      this.restrictedMessage = q.get('restricted') === '1';
    });
    this.load();
  }

  load(): void {
    this.loading = true;
    this.api.getPublic().subscribe({
      next: (g) => {
        this.allApproved = g.all || [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.toast.error('Impossible de charger les publicités.');
      }
    });
    if (this.canSubmitAds) {
      this.api.getMine().subscribe({
        next: (list) => {
          this.mine = list || [];
        },
        error: () => {
          this.mine = [];
        }
      });
    }
  }

  onFileChange(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
    const f = this.selectedFile;
    if (!f) {
      return;
    }
    const name = f.name.toLowerCase();
    const byType = (f as File).type || '';
    if (byType.startsWith('video/') || /\.(mp4|webm|mov|m4v)$/i.test(name)) {
      this.form.type = 'VIDEO';
    } else if (byType.startsWith('image/') || /\.(jpe?g|png|gif|webp|bmp)$/i.test(name)) {
      this.form.type = 'IMAGE';
    }
  }

  submit(ev?: Event): void {
    ev?.preventDefault();
    if (!this.selectedFile) {
      this.toast.error('Choisissez un fichier image ou vidéo.');
      return;
    }
    const fd = new FormData();
    fd.append('file', this.selectedFile);
    fd.append('type', this.form.type);
    fd.append('priority', this.form.priority);
    if (this.form.publishedAt) {
      fd.append('publishedAt', this.form.publishedAt);
    }
    this.submitting = true;
    this.api.create(fd).subscribe({
      next: () => {
        this.toast.success('Demande envoyée. En attente de validation par un administrateur.');
        this.selectedFile = null;
        this.form.publishedAt = '';
        this.submitting = false;
        this.load();
      },
      error: (err) => {
        this.submitting = false;
        const msg = err.error?.error || err.message || 'Échec de l’envoi.';
        this.toast.error(msg);
      }
    });
  }

  mediaUrl(item: { mediaUrl: string }): string {
    return this.api.absoluteMediaUrl(item.mediaUrl);
  }

  statusLabel(s: string): string {
    if (s === 'PENDING') {
      return 'En attente de validation';
    }
    if (s === 'APPROVED') {
      return 'Approuvée';
    }
    if (s === 'REJECTED') {
      return 'Refusée';
    }
    return s;
  }
}
