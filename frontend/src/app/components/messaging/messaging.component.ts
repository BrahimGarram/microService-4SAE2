import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, Router, NavigationEnd } from '@angular/router';
import { UserService, User, MessageService, Message, SendMessageRequest } from '../../services/user.service';
import { GroupChatService, GroupChatDTO, GroupChatInvitationDTO, GroupMessageDTO } from '../../services/group-chat.service';
import { ToastService } from '../../services/toast.service';
import { VideoCallService, IncomingCall } from '../../services/video-call.service';
import { ProposalService } from '../../services/proposal.service';
import { NotificationService } from '../../services/notification.service';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-messaging',
  templateUrl: './messaging.component.html',
  styleUrls: ['./messaging.component.css']
})
export class MessagingComponent implements OnInit, OnDestroy {
  
  currentUser: User | null = null;
  conversations: User[] = [];
  selectedConversation: User | null = null;
  messages: Message[] = [];
  loading = false;
  loadingMessages = false;
  sendingMessage = false;

  // Group chats
  groupChats: GroupChatDTO[] = [];
  selectedGroup: GroupChatDTO | null = null;
  groupMessages: GroupMessageDTO[] = [];
  loadingGroupMessages = false;
  sendingGroupMessage = false;
  newGroupMessageContent = '';
  pendingInvitations: GroupChatInvitationDTO[] = [];
  loadingInvitations = false;
  showCreateGroupModal = false;
  createGroupName = '';
  createGroupSelectedIds: number[] = [];
  usersForInvite: User[] = [];
  loadingUsersForInvite = false;
  creatingGroup = false;
  invitationActionLoading: Record<number, boolean> = {};
  
  // New message
  newMessage: SendMessageRequest = { receiverId: 0, content: '' };
  
  // Unread count
  unreadCount = 0;

  // Proposal actions from inbox (messageId -> 'accepted' | 'rejected')
  proposalDecision: Record<number, 'accepted' | 'rejected'> = {};
  proposalActionLoading: Record<number, boolean> = {};

  // Video call
  showVideoCall = false;
  incomingCall: IncomingCall | null = null;
  incomingCallFromName = '';
  localStream: MediaStream | null = null;
  remoteStream: MediaStream | null = null;
  @ViewChild('localVideo') localVideoRef?: ElementRef<HTMLVideoElement>;
  @ViewChild('remoteVideo') remoteVideoRef?: ElementRef<HTMLVideoElement>;
  
  // Subscriptions
  private routerSubscription?: Subscription;
  private routeParamsSubscription?: Subscription;
  private videoCallSubscriptions: Subscription[] = [];
  
  constructor(
    private userService: UserService,
    private messageService: MessageService,
    private groupChatService: GroupChatService,
    private proposalService: ProposalService,
    private notificationService: NotificationService,
    private route: ActivatedRoute,
    public router: Router,
    private toastService: ToastService,
    public videoCallService: VideoCallService
  ) {}
  
  ngOnInit() {
    this.loadCurrentUser();
    this.loadConversations();
    this.loadGroupChats();
    this.loadPendingInvitations();
    this.loadUnreadCount();
    this.videoCallService.connect().catch(() => {});
    
    // Check if we have a user ID in route (direct message)
    this.routeParamsSubscription = this.route.params.subscribe(params => {
      if (params['userId']) {
        const userId = +params['userId'];
        this.openConversation(userId);
      }
    });
    
    // Reload conversations and messages when navigating back to this page
    this.routerSubscription = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      if (event.url.includes('/messages')) {
        // Reload conversations to get any new messages
        this.loadConversations();
        this.loadUnreadCount();
        
        // Reload messages if we have a selected conversation or group
        if (this.selectedConversation) {
          this.loadMessages(this.selectedConversation.id);
        }
        if (this.selectedGroup) {
          this.loadGroupMessages(this.selectedGroup.id);
        }
        this.loadGroupChats();
        this.loadPendingInvitations();
      }
    });

    // Video call state
    this.videoCallSubscriptions.push(
      this.videoCallService.localStream$.subscribe(s => {
        this.localStream = s;
        setTimeout(() => this.attachStreamToVideo(this.localVideoRef, s), 100);
      }),
      this.videoCallService.remoteStream$.subscribe(s => {
        this.remoteStream = s;
        setTimeout(() => this.attachStreamToVideo(this.remoteVideoRef, s), 100);
      }),
      this.videoCallService.incomingCall$.subscribe(call => {
        this.incomingCall = call;
        this.incomingCallFromName = this.getIncomingCallUserName();
        if (call && !this.conversations.find(u => u.id === call.fromUserId)) {
          this.userService.getUserById(call.fromUserId).subscribe({
            next: u => this.incomingCallFromName = u.username || u.email || 'Someone',
            error: () => {}
          });
        }
      }),
      this.videoCallService.callEnded$.subscribe(() => {
        this.showVideoCall = false;
        this.incomingCall = null;
        this.localStream = null;
        this.remoteStream = null;
      }),
      this.videoCallService.error$.subscribe(msg => this.toastService.error(msg))
    );
  }
  
  ngOnDestroy() {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
    if (this.routeParamsSubscription) {
      this.routeParamsSubscription.unsubscribe();
    }
    this.videoCallSubscriptions.forEach(s => s.unsubscribe());
    this.videoCallService.disconnect();
  }
  
  /**
   * Load current user
   */
  loadCurrentUser() {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
      },
      error: (err) => {
        console.error('Error loading current user:', err);
      }
    });
  }
  
  /**
   * Load conversations (users you've messaged with)
   */
  loadConversations() {
    this.loading = true;
    this.messageService.getConversations().subscribe({
      next: (userIds) => {
        if (userIds.length === 0) {
          this.conversations = [];
          this.loading = false;
          return;
        }
        
        // Load user details for each conversation partner
        const userPromises = userIds.map(id => 
          this.userService.getUserById(id).toPromise()
        );
        
        Promise.all(userPromises).then(users => {
          this.conversations = users.filter(u => u !== null && u !== undefined) as User[];
          this.loading = false;
        }).catch(err => {
          console.error('Error loading conversation partners:', err);
          this.loading = false;
        });
      },
      error: (err) => {
        console.error('Error loading conversations:', err);
        this.loading = false;
      }
    });
  }
  
  /**
   * Load unread messages count
   */
  loadUnreadCount() {
    this.messageService.getUnreadMessages().subscribe({
      next: (response) => {
        this.unreadCount = response.count;
      },
      error: (err) => {
        console.error('Error loading unread count:', err);
      }
    });
  }
  
  loadGroupChats() {
    this.groupChatService.getMyGroupChats().subscribe({
      next: (list) => { this.groupChats = list || []; },
      error: () => { this.groupChats = []; }
    });
  }

  loadPendingInvitations() {
    this.loadingInvitations = true;
    this.groupChatService.getMyPendingInvitations().subscribe({
      next: (list) => {
        this.pendingInvitations = list || [];
        this.loadingInvitations = false;
      },
      error: () => { this.loadingInvitations = false; this.pendingInvitations = []; }
    });
  }

  openGroup(group: GroupChatDTO) {
    this.selectedGroup = group;
    this.selectedConversation = null;
    this.loadGroupMessages(group.id);
  }

  loadGroupMessages(groupChatId: number) {
    this.loadingGroupMessages = true;
    this.groupChatService.getGroupMessages(groupChatId).subscribe({
      next: (list) => {
        this.groupMessages = list || [];
        this.loadingGroupMessages = false;
        setTimeout(() => this.scrollToBottom(), 200);
      },
      error: () => { this.loadingGroupMessages = false; this.groupMessages = []; }
    });
  }

  sendGroupMessage() {
    const content = this.newGroupMessageContent?.trim();
    if (!content || !this.selectedGroup || !this.currentUser) return;
    this.sendingGroupMessage = true;
    this.groupChatService.sendGroupMessage({
      groupChatId: this.selectedGroup.id,
      content
    }).subscribe({
      next: () => {
        this.newGroupMessageContent = '';
        this.sendingGroupMessage = false;
        this.loadGroupMessages(this.selectedGroup!.id);
        this.toastService.success('Message sent.');
      },
      error: (err) => {
        this.sendingGroupMessage = false;
        this.toastService.error(err.error?.error || 'Failed to send message.');
      }
    });
  }

  openCreateGroupModal() {
    this.showCreateGroupModal = true;
    this.createGroupName = '';
    this.createGroupSelectedIds = [];
    this.loadUsersForInvite();
  }

  loadUsersForInvite() {
    this.loadingUsersForInvite = true;
    this.userService.getPublicUsers().subscribe({
      next: (users) => {
        this.usersForInvite = (users || []).filter(u => u.id !== this.currentUser?.id);
        this.loadingUsersForInvite = false;
      },
      error: () => { this.loadingUsersForInvite = false; this.usersForInvite = []; }
    });
  }

  toggleInvitee(userId: number) {
    const i = this.createGroupSelectedIds.indexOf(userId);
    if (i >= 0) this.createGroupSelectedIds.splice(i, 1);
    else this.createGroupSelectedIds.push(userId);
  }

  isInviteeSelected(userId: number): boolean {
    return this.createGroupSelectedIds.includes(userId);
  }

  createGroup() {
    if (this.createGroupSelectedIds.length === 0) {
      this.toastService.error('Select at least one person to invite.');
      return;
    }
    this.creatingGroup = true;
    this.groupChatService.createGroupChat({
      name: this.createGroupName?.trim() || undefined,
      inviteeIds: [...this.createGroupSelectedIds]
    }).subscribe({
      next: (group) => {
        this.creatingGroup = false;
        this.showCreateGroupModal = false;
        this.loadGroupChats();
        this.loadPendingInvitations();
        this.toastService.success('Group created. Invitations sent.');
        this.selectedGroup = group;
        this.selectedConversation = null;
        this.groupMessages = [];
        this.loadGroupMessages(group.id);
      },
      error: (err) => {
        this.creatingGroup = false;
        this.toastService.error(err.error?.error || 'Failed to create group.');
      }
    });
  }

  acceptGroupInvitation(inv: GroupChatInvitationDTO) {
    this.invitationActionLoading[inv.id] = true;
    this.groupChatService.acceptInvitation(inv.id).subscribe({
      next: (group) => {
        this.invitationActionLoading[inv.id] = false;
        this.loadPendingInvitations();
        this.loadGroupChats();
        this.toastService.success('You joined the group.');
        this.selectedGroup = group;
        this.selectedConversation = null;
        this.loadGroupMessages(group.id);
      },
      error: (err) => {
        this.invitationActionLoading[inv.id] = false;
        this.toastService.error(err.error?.error || 'Failed to accept.');
      }
    });
  }

  declineGroupInvitation(inv: GroupChatInvitationDTO) {
    this.invitationActionLoading[inv.id] = true;
    this.groupChatService.declineInvitation(inv.id).subscribe({
      next: () => {
        this.invitationActionLoading[inv.id] = false;
        this.pendingInvitations = this.pendingInvitations.filter(i => i.id !== inv.id);
        this.toastService.success('Invitation declined.');
      },
      error: (err) => {
        this.invitationActionLoading[inv.id] = false;
        this.toastService.error(err.error?.error || 'Failed to decline.');
      }
    });
  }

  closeCreateGroupModal() {
    this.showCreateGroupModal = false;
  }

  isMyGroupMessage(msg: GroupMessageDTO): boolean {
    return this.currentUser?.id === msg.senderId;
  }

  getGroupMembersPreview(group: GroupChatDTO | null): string {
    if (!group?.members?.length) return '';
    return group.members.map(m => m.username).join(', ');
  }

  /**
   * Open conversation with a user
   */
  openConversation(userId: number) {
    this.selectedGroup = null;
    // Find user in conversations or load it
    let user = this.conversations.find(u => u.id === userId);
    
    if (!user) {
      this.userService.getUserById(userId).subscribe({
        next: (loadedUser) => {
          this.selectedConversation = loadedUser;
          this.loadMessages(userId);
          // Add to conversations if not already there
          if (!this.conversations.find(u => u.id === userId)) {
            this.conversations.unshift(loadedUser);
          }
        },
        error: (err) => {
          console.error('Error loading user:', err);
          this.toastService.error('Failed to load user');
        }
      });
    } else {
      this.selectedConversation = user;
      this.loadMessages(userId);
    }
  }
  
  /**
   * Load messages for a conversation
   */
  loadMessages(userId: number) {
    if (!userId) {
      console.error('Invalid userId for loading messages');
      return;
    }
    
    this.loadingMessages = true;
    this.messageService.getConversation(userId).subscribe({
      next: (messages) => {
        // Sort messages by sentAt to ensure correct chronological order
        messages.sort((a, b) => {
          const dateA = a.sentAt ? new Date(a.sentAt).getTime() : 0;
          const dateB = b.sentAt ? new Date(b.sentAt).getTime() : 0;
          return dateA - dateB;
        });
        
        this.messages = messages;
        this.loadingMessages = false;

        // Load actual proposal status so we hide Accept/Refuse when already accepted or rejected
        const proposalMessages = messages.filter(m => m.proposalId && m.receiverId === this.currentUser?.id);
        proposalMessages.forEach(msg => {
          this.proposalService.getById(msg.proposalId!).subscribe({
            next: (p) => {
              if (p.status === 'ACCEPTED') this.proposalDecision[msg.id!] = 'accepted';
              else if (p.status === 'REJECTED') this.proposalDecision[msg.id!] = 'rejected';
            },
            error: () => {}
          });
        });
        
        // Mark conversation as read
        this.messageService.markConversationAsRead(userId).subscribe({
          next: () => {
            this.loadUnreadCount();
          },
          error: (err) => {
            console.error('Error marking conversation as read:', err);
            // Don't show error to user, just log it
          }
        });
        
        // Scroll to bottom after messages are loaded and rendered
        setTimeout(() => {
          this.scrollToBottom();
        }, 200);
      },
      error: (err) => {
        console.error('Error loading messages:', err);
        this.toastService.error('Failed to load messages. Please try again.');
        this.loadingMessages = false;
      }
    });
  }
  
  /** True if this message is a proposal alert (receiver can accept/refuse) */
  isProposalMessage(message: Message): boolean {
    return !!(message.proposalId && message.receiverId === this.currentUser?.id);
  }

  /** Can the current user respond (accept/refuse) to this proposal message */
  canRespondToProposal(message: Message): boolean {
    return this.isProposalMessage(message) && !this.proposalDecision[message.id!];
  }

  acceptProposalFromMessage(message: Message) {
    if (!message.proposalId || !this.currentUser?.id) return;
    const freelancerId = message.senderId;
    this.proposalActionLoading[message.id!] = true;
    this.proposalService.accept(message.proposalId, this.currentUser.id).subscribe({
      next: () => {
        this.proposalDecision[message.id!] = 'accepted';
        this.proposalActionLoading[message.id!] = false;
        this.toastService.success('Proposal accepted.');
        this.notificationService.create({
          targetUserId: freelancerId,
          type: 'PROPOSAL_ACCEPTED',
          message: 'Votre proposition a été acceptée. Vous pouvez commencer le projet depuis la page Projets.',
          relatedId: message.projectId ?? undefined
        }).subscribe({ error: () => {} });
        // No message in conversation – freelancer is notified via the notification bell only
      },
      error: (err) => {
        this.proposalActionLoading[message.id!] = false;
        this.toastService.error(err.error?.error || 'Failed to accept proposal.');
      }
    });
  }

  refuseProposalFromMessage(message: Message) {
    if (!message.proposalId || !this.currentUser?.id) return;
    const freelancerId = message.senderId;
    this.proposalActionLoading[message.id!] = true;
    this.proposalService.reject(message.proposalId, this.currentUser.id).subscribe({
      next: () => {
        this.proposalDecision[message.id!] = 'rejected';
        this.proposalActionLoading[message.id!] = false;
        this.toastService.success('Proposal declined.');
        this.notificationService.create({
          targetUserId: freelancerId,
          type: 'PROPOSAL_REJECTED',
          message: 'Votre proposition a été refusée.',
          relatedId: message.projectId ?? undefined
        }).subscribe({ error: () => {} });
        const clientName = this.currentUser?.username || 'The client';
        this.messageService.sendMessage({
          receiverId: freelancerId,
          subject: 'Proposal declined',
          content: clientName + ' has declined your proposal.'
        }).subscribe({ error: () => {} });
      },
      error: (err) => {
        this.proposalActionLoading[message.id!] = false;
        this.toastService.error(err.error?.error || 'Failed to decline proposal.');
      }
    });
  }

  /**
   * Send a message
   */
  sendMessage() {
    if (!this.selectedConversation || !this.newMessage.content.trim()) {
      return;
    }
    
    this.sendingMessage = true;
    this.newMessage.receiverId = this.selectedConversation.id;
    const messageContent = this.newMessage.content.trim();
    this.newMessage.content = ''; // Clear input immediately for better UX
    
    this.messageService.sendMessage({ ...this.newMessage, content: messageContent }).subscribe({
      next: (message) => {
        // Reload messages from server to ensure consistency
        if (this.selectedConversation) {
          this.loadMessages(this.selectedConversation.id);
        }
        // Reload conversations to update the list
        this.loadConversations();
        this.loadUnreadCount();
        this.sendingMessage = false;
        this.toastService.success('Message sent successfully!');
      },
      error: (err) => {
        console.error('Error sending message:', err);
        // Restore message content if sending failed
        this.newMessage.content = messageContent;
        this.toastService.error('Failed to send message. Please try again.');
        this.sendingMessage = false;
      }
    });
  }
  
  /**
   * Scroll to bottom of messages
   */
  scrollToBottom() {
    const messagesContainer = document.querySelector('.messages-container');
    if (messagesContainer) {
      messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
  }
  
  /**
   * Check if message is from current user
   */
  isMyMessage(message: Message): boolean {
    return this.currentUser?.id === message.senderId;
  }
  
  /**
   * Get conversation partner name
   */
  getPartnerName(user: User): string {
    return user.username || 'Unknown User';
  }
  
  /**
   * Track by function for message list (improves performance)
   */
  trackByMessageId(index: number, message: Message): any {
    return message.id || index;
  }

  async startVideoCall(): Promise<void> {
    if (!this.selectedConversation) return;
    await this.videoCallService.connect();
    await this.videoCallService.startCall(this.selectedConversation.id);
    this.showVideoCall = true;
  }

  async acceptVideoCall(): Promise<void> {
    if (!this.incomingCall) return;
    await this.videoCallService.connect();
    await this.videoCallService.acceptCall(this.incomingCall.fromUserId);
    this.incomingCall = null;
    this.showVideoCall = true;
  }

  declineVideoCall(): void {
    this.videoCallService.declineCall();
    this.incomingCall = null;
  }

  endVideoCall(): void {
    this.videoCallService.endCall();
    this.showVideoCall = false;
  }

  getIncomingCallUserName(): string {
    if (!this.incomingCall) return '';
    const user = this.conversations.find(u => u.id === this.incomingCall!.fromUserId);
    return user ? (user.username || user.email || 'Someone') : 'Someone';
  }

  private attachStreamToVideo(ref: ElementRef<HTMLVideoElement> | undefined, stream: MediaStream | null): void {
    if (!ref?.nativeElement) return;
    ref.nativeElement.srcObject = stream;
    if (stream) ref.nativeElement.play().catch(() => {});
  }
}
