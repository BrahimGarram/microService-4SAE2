import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, NavigationEnd } from '@angular/router';
import { UserService, User, MessageService, Message, SendMessageRequest } from '../../services/user.service';
import { ToastService } from '../../services/toast.service';
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
  
  // New message
  newMessage: SendMessageRequest = { receiverId: 0, content: '' };
  
  // Unread count
  unreadCount = 0;
  
  // Subscriptions
  private routerSubscription?: Subscription;
  private routeParamsSubscription?: Subscription;
  
  constructor(
    private userService: UserService,
    private messageService: MessageService,
    private route: ActivatedRoute,
    public router: Router,
    private toastService: ToastService
  ) {}
  
  ngOnInit() {
    this.loadCurrentUser();
    this.loadConversations();
    this.loadUnreadCount();
    
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
        
        // Reload messages if we have a selected conversation
        if (this.selectedConversation) {
          this.loadMessages(this.selectedConversation.id);
        }
      }
    });
  }
  
  ngOnDestroy() {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
    if (this.routeParamsSubscription) {
      this.routeParamsSubscription.unsubscribe();
    }
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
  
  /**
   * Open conversation with a user
   */
  openConversation(userId: number) {
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
}
