import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
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

  // Complaints microservice base URL (see complaints-service application.yml)
  private apiUrl = environment.complaintsServiceUrl + '/report';
  
  // ImgBB API configuration
  private imgbbApiKey = environment.imgbbApiKey;
  private imgbbApiUrl = environment.imgbbApiUrl;

  constructor(private http: HttpClient) {}

  /**
   * Upload image to ImgBB
   * ImgBB API expects the key as a query parameter and image as form data
   * We need to bypass the Authorization header that Angular adds automatically
   */
  uploadImageToImgBB(file: File): Observable<any> {
    return new Observable(observer => {
      const formData = new FormData();
      formData.append('image', file);
      
      // Use native fetch API to avoid Angular interceptors adding Authorization header
      fetch(`${this.imgbbApiUrl}?key=${this.imgbbApiKey}`, {
        method: 'POST',
        body: formData
      })
      .then(response => response.json())
      .then(data => {
        observer.next(data);
        observer.complete();
      })
      .catch(error => {
        observer.error(error);
      });
    });
  }

  /**
   * Get all complaints for a specific user.
   */
  getComplaintsForUser(userId: number): Observable<Complaint[]> {
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.get<Complaint[]>(`${this.apiUrl}/retrieve-all-complaints`, { params });
  }

  /**
   * Get all complaints for admin (all users).
   */
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
      userEmail?: string;  // Add user email
    }
  ): Observable<Complaint> {
    const formData = new FormData();
    formData.append('title', data.title);
    formData.append('description', data.description);
    formData.append('priority', data.claimPriority);
    
    // Add user email if provided
    if (data.userEmail) {
      formData.append('userEmail', data.userEmail);
    }
    
    // If imageUrl is provided (from ImgBB), send it with metadata
    if (data.imageUrl) {
      formData.append('imageUrl', data.imageUrl);
      if (data.fileName) formData.append('fileName', data.fileName);
      if (data.fileType) formData.append('fileType', data.fileType);
      if (data.fileSize) formData.append('fileSize', data.fileSize.toString());
    }
    // Otherwise, send the file for local storage
    else if (data.file) {
      formData.append('file', data.file);
    }

    const params = new HttpParams().set('userId', userId.toString());
    return this.http.post<Complaint>(`${this.apiUrl}/create-claim`, formData, { params });
  }

  /**
   * Update an existing claim
   */
  updateClaim(
    claimId: number,
    userId: number,
    data: UpdateComplaintRequest
  ): Observable<Complaint> {
    // Build a complete Complaint object for the backend
    // Only include fields that should be updated
    const complaintUpdate = {
      idReclamation: data.idReclamation,
      userId: userId,
      title: data.title,
      description: data.description,
      claimPriority: data.claimPriority,
      claimStatus: data.claimStatus,
      resolutionNote: data.resolutionNote,
      isVisible: true // Ensure it stays visible
    };

    console.log('Sending update request:', complaintUpdate);

    const params = new HttpParams().set('userId', userId.toString());
    return this.http.put<Complaint>(`${this.apiUrl}/update-claim`, complaintUpdate, { params });
  }

  /**
   * Update an existing claim by admin (no userId check)
   */
  updateClaimByAdmin(
    data: UpdateComplaintRequest
  ): Observable<Complaint> {
    const complaintUpdate = {
      idReclamation: data.idReclamation,
      title: data.title,
      description: data.description,
      claimPriority: data.claimPriority,
      claimStatus: data.claimStatus,
      resolutionNote: data.resolutionNote,
      isVisible: true
    };

    console.log('Sending admin update request:', complaintUpdate);

    return this.http.put<Complaint>(`${this.apiUrl}/admin/update-claim`, complaintUpdate);
  }

  /**
   * Delete a claim
   */
  deleteClaim(claimId: number, userId: number): Observable<void> {
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.delete<void>(`${this.apiUrl}/drop-claim/${claimId}`, { params });
  }
}
