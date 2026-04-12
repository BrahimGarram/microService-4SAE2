import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CollaborationApiService, CollaborationFullDto } from '../../services/collaboration-api.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-admin-collaborations',
  templateUrl: './admin-collaborations.component.html',
  styleUrls: ['./admin-collaborations.component.css']
})
export class AdminCollaborationsComponent implements OnInit, OnDestroy {
  pending: CollaborationFullDto[] = [];
  loading = false;
  /** Object URLs (JWT via intercepteur HttpClient, pas affichables en src direct). */
  previewUrls: Record<number, string> = {};

  constructor(
    private api: CollaborationApiService,
    private toast: ToastService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.load();
  }

  ngOnDestroy(): void {
    Object.values(this.previewUrls).forEach((u) => URL.revokeObjectURL(u));
  }

  load(): void {
    Object.values(this.previewUrls).forEach((u) => URL.revokeObjectURL(u));
    this.previewUrls = {};
    this.loading = true;
    this.api.getPending().subscribe({
      next: (list) => {
        this.pending = list || [];
        this.loading = false;
        for (const p of this.pending) {
          this.http
            .get(this.api.absolutePreviewUrl(p.id), { responseType: 'blob' })
            .subscribe({
              next: (blob) => {
                this.previewUrls[p.id] = URL.createObjectURL(blob);
              },
              error: () => {
                /* ignore preview failure */
              }
            });
        }
      },
      error: () => {
        this.loading = false;
        this.toast.error('Impossible de charger les demandes.');
      }
    });
  }

  approve(id: number): void {
    this.api.approve(id).subscribe({
      next: () => {
        this.toast.success('Publicité approuvée.');
        this.load();
      },
      error: () => this.toast.error('Échec de l’approbation.')
    });
  }

  reject(id: number): void {
    this.api.reject(id).subscribe({
      next: () => {
        this.toast.success('Demande refusée.');
        this.load();
      },
      error: () => this.toast.error('Échec du refus.')
    });
  }

  mediaUrl(item: CollaborationFullDto): string {
    return this.previewUrls[item.id] || '';
  }
}
