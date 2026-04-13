import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { ChatService, ChatMessage } from '../../services/chat.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-chatbox',
  templateUrl: './chatbox.component.html',
  styleUrls: ['./chatbox.component.css']
})
export class ChatboxComponent implements OnInit, OnDestroy, AfterViewChecked {
  isOpen = false;
  isLoggedIn = false;
  messages: ChatMessage[] = [];
  inputText = '';
  loading = false;
  private scrollToBottomRequested = false;

  @ViewChild('messagesContainer') private messagesContainer!: ElementRef<HTMLDivElement>;

  constructor(
    private keycloakService: KeycloakService,
    private chatService: ChatService,
    private toastService: ToastService
  ) {}

  async ngOnInit() {
    this.isLoggedIn = await this.keycloakService.isLoggedIn();
    if (!this.isLoggedIn) {
      const token = localStorage.getItem('kc-access-token');
      if (token) this.isLoggedIn = true;
    }
  }

  ngOnDestroy() {}

  ngAfterViewChecked() {
    if (this.scrollToBottomRequested) {
      this.scrollToBottom();
      this.scrollToBottomRequested = false;
    }
  }

  toggle() {
    this.isOpen = !this.isOpen;
    if (this.isOpen) this.scrollToBottomRequested = true;
  }

  close() {
    this.isOpen = false;
  }

  send() {
    const text = this.inputText?.trim();
    if (!text || this.loading || !this.isLoggedIn) return;

    this.messages.push({ role: 'user', content: text });
    this.inputText = '';
    this.loading = true;
    this.scrollToBottomRequested = true;

    const history = this.messages.slice(-20).map(m => ({ role: m.role, content: m.content }));
    if (history.length > 1) history.pop(); // current message is sent separately

    this.chatService.sendMessage({
      message: text,
      history: history.length > 1 ? history : undefined
    }).subscribe({
      next: (res) => {
        this.messages.push({ role: 'assistant', content: res.reply || '' });
        this.loading = false;
        this.scrollToBottomRequested = true;
      },
      error: (err) => {
        this.loading = false;
        const msg = err.error?.error || err.message || 'AI unavailable.';
        this.toastService.error(msg);
        this.messages.push({
          role: 'assistant',
          content: 'Désolé, une erreur est survenue. Vérifiez qu\'Ollama est démarré (ollama run llama3.2).'
        });
        this.scrollToBottomRequested = true;
      }
    });
  }

  onKeyDown(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  private scrollToBottom() {
    try {
      const el = this.messagesContainer?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch {}
  }
}
