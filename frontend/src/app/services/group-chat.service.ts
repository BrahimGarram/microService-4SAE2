import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface GroupChatMemberDTO {
  userId: number;
  username: string;
}

export interface GroupChatDTO {
  id: number;
  name: string;
  createdById: number;
  createdByUsername: string;
  createdAt: string;
  members: GroupChatMemberDTO[];
}

export interface GroupChatInvitationDTO {
  id: number;
  groupChatId: number;
  groupChatName: string;
  inviterId: number;
  inviterUsername: string;
  inviteeId: number;
  status: string;
  createdAt: string;
}

export interface GroupMessageDTO {
  id: number;
  groupChatId: number;
  senderId: number;
  senderUsername: string;
  content: string;
  sentAt: string;
}

export interface CreateGroupChatRequest {
  name?: string;
  inviteeIds: number[];
}

export interface SendGroupMessageRequest {
  groupChatId: number;
  content: string;
}

@Injectable({
  providedIn: 'root'
})
export class GroupChatService {
  private apiUrl = `${environment.apiGatewayUrl}/api/group-chats`;

  constructor(private http: HttpClient) {}

  createGroupChat(request: CreateGroupChatRequest): Observable<GroupChatDTO> {
    return this.http.post<GroupChatDTO>(this.apiUrl, request);
  }

  getMyGroupChats(): Observable<GroupChatDTO[]> {
    return this.http.get<GroupChatDTO[]>(this.apiUrl);
  }

  getGroupChat(id: number): Observable<GroupChatDTO> {
    return this.http.get<GroupChatDTO>(`${this.apiUrl}/${id}`);
  }

  getMyPendingInvitations(): Observable<GroupChatInvitationDTO[]> {
    return this.http.get<GroupChatInvitationDTO[]>(`${this.apiUrl}/invitations`);
  }

  acceptInvitation(invitationId: number): Observable<GroupChatDTO> {
    return this.http.post<GroupChatDTO>(`${this.apiUrl}/invitations/${invitationId}/accept`, {});
  }

  declineInvitation(invitationId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/invitations/${invitationId}/decline`, {});
  }

  getGroupMessages(groupChatId: number): Observable<GroupMessageDTO[]> {
    return this.http.get<GroupMessageDTO[]>(`${this.apiUrl}/${groupChatId}/messages`);
  }

  sendGroupMessage(request: SendGroupMessageRequest): Observable<GroupMessageDTO> {
    return this.http.post<GroupMessageDTO>(`${this.apiUrl}/messages`, request);
  }
}
