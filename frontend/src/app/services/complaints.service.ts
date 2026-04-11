import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type ClaimStatus = 'Pending' | 'Under_Review' | 'Resolved' | 'Rejected' | 'Closed';
export type ClaimPriority = 'High' | 'Medium' | 'Low' | 'Urgent';

export interface ClaimAttachment {
  idAttachment?: number;
  fileName?: string;
  fileUrl: string;
  fileType?: string;
  fileSize?: number;
  uploadedAt?: string;
  claimId?: number;
  uploadedById?: number;
}

export interface Complaint {
  idReclamation: number;
  userId: number;
  title: string;
  description: string;
  createdAt?: string;
  updatedAt?: string;
  claimStatus: ClaimStatus;
  claimPriority: ClaimPriority;
  resolutionNote?: string;
  resolvedAt?: string;
  isVisible?: boolean;
  claimAttachment?: ClaimAttachment | null;
}

export interface CreateComplaintRequest {
  title: string;
  description: string;
  claimPriority: ClaimPriority;
}

export interface UpdateComplaintRequest {
  idReclamation: number;
  title: string;
  description: string;
  claimPriority: ClaimPriority;
  claimStatus: ClaimStatus;
  resolutionNote?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ComplaintsService {
  private apiUrl = environment.complaintsServiceUrl + '/report';
  private imgbbApiKey = environment.imgbbApiKey;
  private imgbbApiUrl = environment.imgbbApiUrl;

  constructor(private http: HttpClient) {}

  uploadImageToImgBB(file: File): Observable<any> {
    return new Observable(observer => {
      const formData = new FormData();
      formData.append('image', file);
      fetch(`${this.imgbbApiUrl}?key=${this.imgbbApiKey}`, {
        method: 'POST',
        body: formData
      })
        .then(response => response.json())
        .then(data => {
          observer.next(data);
          observer.complete();
        })
        .catch(error => observer.error(error));
    });
  }

  getComplaintsForUser(userId: number): Observable<Complaint[]> {
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.get<Complaint[]>(`${this.apiUrl}/retrieve-all-complaints`, { params });
  }

  getAllComplaintsForAdmin(): Observable<Complaint[]> {
    return this.http.get<Complaint[]>(`${this.apiUrl}/admin/retrieve-all-complaints`);
  }

  createClaimWithFile(
    userId: number,
    data: {
      title: string;
      description: string;
      claimPriority: ClaimPriority;
      file?: File | null;
      imageUrl?: string;
      fileName?: string;
      fileType?: string;
      fileSize?: number;
      userEmail?: string;
    }
  ): Observable<Complaint> {
    const formData = new FormData();
    formData.append('title', data.title);
    formData.append('description', data.description);
    formData.append('priority', data.claimPriority);
    if (data.userEmail) formData.append('userEmail', data.userEmail);
    if (data.imageUrl) {
      formData.append('imageUrl', data.imageUrl);
      if (data.fileName) formData.append('fileName', data.fileName);
      if (data.fileType) formData.append('fileType', data.fileType);
      if (data.fileSize) formData.append('fileSize', data.fileSize.toString());
    } else if (data.file) {
      formData.append('file', data.file);
    }
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.post<Complaint>(`${this.apiUrl}/create-claim`, formData, { params });
  }

  updateClaim(claimId: number, userId: number, data: UpdateComplaintRequest): Observable<Complaint> {
    const complaintUpdate = {
      idReclamation: data.idReclamation,
      userId,
      title: data.title,
      description: data.description,
      claimPriority: data.claimPriority,
      claimStatus: data.claimStatus,
      resolutionNote: data.resolutionNote,
      isVisible: true
    };
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.put<Complaint>(`${this.apiUrl}/update-claim`, complaintUpdate, { params });
  }

  updateClaimByAdmin(data: UpdateComplaintRequest): Observable<Complaint> {
    const complaintUpdate = {
      idReclamation: data.idReclamation,
      title: data.title,
      description: data.description,
      claimPriority: data.claimPriority,
      claimStatus: data.claimStatus,
      resolutionNote: data.resolutionNote,
      isVisible: true
    };
    return this.http.put<Complaint>(`${this.apiUrl}/admin/update-claim`, complaintUpdate);
  }

  deleteClaim(claimId: number, userId: number): Observable<void> {
    const params = new HttpParams().set('userId', userId.toString());
    const headers = new HttpHeaders({ 'X-Confirm-Delete': 'true' });
    return this.http.delete<void>(`${this.apiUrl}/drop-claim/${claimId}`, { params, headers });
  }
}
