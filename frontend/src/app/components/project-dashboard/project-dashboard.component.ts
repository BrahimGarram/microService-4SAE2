import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectService, Project } from '../../services/project.service';
import { UserService, User, MessageService, Message } from '../../services/user.service';
import { ProposalService, Proposal } from '../../services/proposal.service';
import { ToastService } from '../../services/toast.service';
import { TaskService, Task, TaskStatus, CreateTaskRequest, UpdateTaskRequest } from '../../services/task.service';

const UPDATE_PREFIX = 'Update: ';

export type FeedFilter = 'all' | 'updates' | 'messages' | 'tasks';

export interface MessageGroup {
  dateLabel: string;
  messages: Message[];
}

@Component({
  selector: 'app-project-dashboard',
  templateUrl: './project-dashboard.component.html',
  styleUrls: ['./project-dashboard.component.css']
})
export class ProjectDashboardComponent implements OnInit {
  projectId: number | null = null;
  project: Project | null = null;
  proposals: Proposal[] = [];
  acceptedProposal: Proposal | null = null;
  currentUser: User | null = null;
  otherParticipant: User | null = null;
  clientUser: User | null = null;
  freelancerUser: User | null = null;
  messages: Message[] = [];
  adminJoinedSent = false;
  newMessageContent = '';
  updateTitle = '';
  updateContent = '';
  showUpdateForm = false;
  feedFilter: FeedFilter = 'all';
  loading = true;
  loadingMessages = false;
  sendingMessage = false;
  sendingUpdate = false;
  canAccess = false;
  isOwner = false;
  isAdmin = false;
  descriptionExpanded = false;

  // Tasks (Kanban) – only in project room (after proposal accepted)
  tasks: Task[] = [];
  tasksByStatus: { [key: string]: Task[] } = { 'TO_DO': [], 'IN_PROGRESS': [], 'DONE': [] };
  showTaskModal = false;
  editingTask: Task | null = null;
  newTask: CreateTaskRequest = { title: '', projectId: 0, createdBy: 0, priority: 0 };
  loadingTasks = false;
  TaskStatus = TaskStatus;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projectService: ProjectService,
    private userService: UserService,
    private messageService: MessageService,
    private proposalService: ProposalService,
    private toastService: ToastService,
    private taskService: TaskService
  ) {}

  ngOnInit() {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.route.paramMap.subscribe(params => {
          const id = params.get('projectId');
          this.projectId = id ? +id : null;
          if (this.projectId) this.loadProject();
          else this.loading = false;
        });
      },
      error: () => {
        this.currentUser = null;
        this.route.paramMap.subscribe(params => {
          const id = params.get('projectId');
          this.projectId = id ? +id : null;
          if (this.projectId) this.loadProject();
          else this.loading = false;
        });
      }
    });
  }

  loadProject() {
    if (!this.projectId) return;
    this.loading = true;
    this.projectService.getProjectById(this.projectId).subscribe({
      next: (project) => {
        this.project = project;
        this.loadProposals();
      },
      error: (err) => {
        this.loading = false;
        const msg = err?.status === 404 ? 'Project not found.' : 'Unable to load project. Check that the API Gateway and project-service are running.';
        this.toastService.error(msg);
        this.router.navigate(['/projects']);
      }
    });
  }

  loadProposals() {
    if (!this.projectId) return;
    this.proposalService.getByProject(this.projectId).subscribe({
      next: (list) => {
        this.proposals = list || [];
        this.acceptedProposal = this.proposals.find(p => p.status === 'ACCEPTED') || null;
        this.checkAccess();
        if (this.canAccess) {
          this.loadParticipants();
          this.loadOtherParticipant();
          this.loadMessages();
          this.loadTasks();
        }
        this.loading = false;
      },
      error: () => {
        this.proposals = [];
        this.acceptedProposal = null;
        this.checkAccess();
        if (this.canAccess) {
          this.loadParticipants();
          this.loadOtherParticipant();
          this.loadMessages();
          this.loadTasks();
        }
        this.loading = false;
      }
    });
  }

  checkAccess() {
    if (!this.currentUser || !this.project) {
      this.canAccess = false;
      return;
    }
    this.isAdmin = this.currentUser.role === 'ADMIN';
    this.isOwner = this.project.ownerId === this.currentUser.id;
    const isAssignedFreelancer = this.acceptedProposal?.freelancerId === this.currentUser.id;
    this.canAccess = this.isOwner || !!isAssignedFreelancer || this.isAdmin;
  }

  /** Load client and freelancer for the Participants section. */
  loadParticipants() {
    if (!this.project) return;
    this.clientUser = null;
    this.freelancerUser = null;
    this.userService.getUserById(this.project.ownerId).subscribe({
      next: (user) => { this.clientUser = user; },
      error: () => { this.clientUser = null; }
    });
    const freelancerId = this.acceptedProposal?.freelancerId;
    if (freelancerId) {
      this.userService.getUserById(freelancerId).subscribe({
        next: (user) => { this.freelancerUser = user; },
        error: () => { this.freelancerUser = null; }
      });
    }
  }

  loadOtherParticipant() {
    if (!this.currentUser || !this.project) return;
    // Admin: use client (owner) as "other" so messages sent by admin go to project room (visible to both)
    const otherId = this.isAdmin
      ? this.project.ownerId
      : this.isOwner
        ? this.acceptedProposal?.freelancerId
        : this.project.ownerId;
    if (!otherId) return;
    this.userService.getUserById(otherId).subscribe({
      next: (user) => { this.otherParticipant = user; },
      error: () => { this.otherParticipant = null; }
    });
  }

  loadMessages() {
    if (!this.projectId) return;
    this.loadingMessages = true;
    this.messageService.getProjectMessages(this.projectId).subscribe({
      next: (list) => {
        this.messages = list || [];
        this.loadingMessages = false;
        setTimeout(() => this.scrollToBottom(), 100);
        if (this.isAdmin && !this.adminJoinedSent) this.maybeSendAdminJoined();
      },
      error: () => {
        this.loadingMessages = false;
      }
    });
  }

  /** Send "An admin joined your conversation" once when admin opens the room. */
  maybeSendAdminJoined() {
    if (!this.currentUser || !this.projectId || !this.project || !this.otherParticipant) return;
    const alreadySent = this.messages.some(m => m.senderId === this.currentUser!.id && m.subject === 'Admin joined');
    if (alreadySent) {
      this.adminJoinedSent = true;
      return;
    }
    this.adminJoinedSent = true;
    this.messageService.sendMessage({
      receiverId: this.project.ownerId,
      projectId: this.projectId,
      subject: 'Admin joined',
      content: 'An admin joined your conversation.'
    }).subscribe({
      next: () => this.loadMessages(),
      error: () => { this.adminJoinedSent = false; }
    });
  }

  get filteredMessages(): Message[] {
    if (this.feedFilter === 'updates') return this.messages.filter(m => this.isUpdateMessage(m));
    if (this.feedFilter === 'messages') return this.messages.filter(m => !this.isUpdateMessage(m));
    return this.messages;
  }

  messageGroups(): MessageGroup[] {
    const list = this.filteredMessages;
    const groups: MessageGroup[] = [];
    let currentLabel = '';
    let currentBatch: Message[] = [];
    for (const m of list) {
      const label = this.getDateLabel(m.sentAt);
      if (label !== currentLabel) {
        if (currentBatch.length) groups.push({ dateLabel: currentLabel, messages: currentBatch });
        currentLabel = label;
        currentBatch = [m];
      } else {
        currentBatch.push(m);
      }
    }
    if (currentBatch.length) groups.push({ dateLabel: currentLabel, messages: currentBatch });
    return groups;
  }

  getDateLabel(sentAt?: string): string {
    if (!sentAt) return '';
    const d = new Date(sentAt);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    if (d.toDateString() === today.toDateString()) return 'Today';
    if (d.toDateString() === yesterday.toDateString()) return 'Yesterday';
    return d.toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric', year: d.getFullYear() !== today.getFullYear() ? 'numeric' : undefined });
  }

  isUpdateMessage(message: Message): boolean {
    const sub = message.subject?.trim();
    return !!sub && sub.startsWith(UPDATE_PREFIX);
  }

  isAdminJoinedMessage(message: Message): boolean {
    return message.subject?.trim() === 'Admin joined';
  }

  getUpdateTitle(message: Message): string {
    if (!this.isUpdateMessage(message)) return '';
    return (message.subject || '').replace(UPDATE_PREFIX, '').trim() || 'Progress update';
  }

  sendMessage() {
    const content = this.newMessageContent?.trim();
    if (!content || !this.otherParticipant || !this.currentUser || !this.projectId) return;
    this.sendingMessage = true;
    this.messageService.sendMessage({
      receiverId: this.otherParticipant.id,
      content,
      projectId: this.projectId
    }).subscribe({
      next: () => {
        this.newMessageContent = '';
        this.sendingMessage = false;
        this.loadMessages();
      },
      error: (err) => {
        this.sendingMessage = false;
        this.toastService.error(err.error?.error || 'Failed to send message.');
      }
    });
  }

  postUpdate() {
    const content = (this.updateContent || this.updateTitle || '').trim();
    if (!content || !this.otherParticipant || !this.currentUser || !this.projectId) return;
    const title = this.updateTitle?.trim() || 'Progress update';
    this.sendingUpdate = true;
    this.messageService.sendMessage({
      receiverId: this.otherParticipant.id,
      subject: UPDATE_PREFIX + title,
      content: this.updateContent?.trim() || title,
      projectId: this.projectId
    }).subscribe({
      next: () => {
        this.updateTitle = '';
        this.updateContent = '';
        this.showUpdateForm = false;
        this.sendingUpdate = false;
        this.loadMessages();
        this.toastService.success('Update posted.');
      },
      error: (err) => {
        this.sendingUpdate = false;
        this.toastService.error(err.error?.error || 'Failed to post update.');
      }
    });
  }

  isMyMessage(message: Message): boolean {
    return message.senderId === this.currentUser?.id;
  }

  canPostUpdate(): boolean {
    return !this.isOwner && !this.isAdmin && !!this.otherParticipant && !!this.projectId;
  }

  scrollToBottom() {
    const el = document.getElementById('project-dashboard-messages');
    if (el) el.scrollTop = el.scrollHeight;
  }

  goBack() {
    this.router.navigate(this.isAdmin ? ['/admin/projects'] : ['/projects']);
  }

  viewProject() {
    if (this.projectId) this.router.navigate(['/projects', this.projectId]);
  }

  formatBudget(budget?: number): string {
    if (budget == null) return '—';
    return new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(budget);
  }

  formatDeadline(deadline?: string): string {
    if (!deadline) return '—';
    return new Date(deadline).toLocaleDateString(undefined, { dateStyle: 'medium' });
  }

  descriptionShort(desc?: string, max = 120): string {
    if (!desc) return '';
    return desc.length <= max ? desc : desc.slice(0, max) + '…';
  }

  loadTasks() {
    if (!this.projectId) return;
    this.loadingTasks = true;
    this.taskService.getTasksByProject(this.projectId).subscribe({
      next: (tasks) => {
        this.tasks = Array.isArray(tasks) ? tasks : [];
        this.organizeTasksByStatus();
        this.loadingTasks = false;
      },
      error: (err) => {
        this.tasks = [];
        this.organizeTasksByStatus();
        this.toastService.error(err?.error?.message || 'Impossible de charger les tâches. Vérifiez que la gateway et le project-service sont démarrés.');
        this.loadingTasks = false;
      }
    });
  }

  organizeTasksByStatus() {
    this.tasksByStatus = { 'TO_DO': [], 'IN_PROGRESS': [], 'DONE': [] };
    this.tasks.forEach(task => {
      const status = (task.status || 'TO_DO').toString();
      if (this.tasksByStatus[status]) this.tasksByStatus[status].push(task);
    });
  }

  canManageTasks(): boolean {
    return !!this.currentUser && (this.isOwner || this.isAdmin);
  }

  openTaskModal(task?: Task) {
    if (!this.currentUser?.id || !this.projectId) {
      this.toastService.error('Session ou projet manquant. Rechargez la page.');
      return;
    }
    if (task) {
      this.editingTask = task;
      this.newTask = {
        title: task.title,
        description: task.description || '',
        projectId: task.projectId,
        assignedTo: task.assignedTo,
        createdBy: this.currentUser.id,
        dueDate: task.dueDate,
        priority: task.priority ?? 0
      };
    } else {
      this.editingTask = null;
      this.newTask = {
        title: '',
        description: '',
        projectId: this.projectId,
        createdBy: this.currentUser.id,
        priority: 0
      };
    }
    this.showTaskModal = true;
  }

  closeTaskModal() {
    this.showTaskModal = false;
    this.editingTask = null;
    this.newTask = { title: '', projectId: this.projectId || 0, createdBy: this.currentUser?.id || 0, priority: 0 };
  }

  saveTask() {
    if (!this.newTask.title || this.newTask.title.trim().length < 3) {
      this.toastService.error('Task title must be at least 3 characters');
      return;
    }
    if (!this.projectId || !this.currentUser?.id) return;
    const taskData: CreateTaskRequest = {
      title: this.newTask.title.trim(),
      description: this.newTask.description?.trim() || undefined,
      projectId: this.projectId,
      createdBy: this.currentUser.id,
      assignedTo: this.newTask.assignedTo || undefined,
      dueDate: this.newTask.dueDate || undefined,
      priority: this.newTask.priority ?? 0
    };
    if (this.editingTask?.id) {
      const updateRequest: UpdateTaskRequest = {
        title: taskData.title,
        description: taskData.description,
        assignedTo: taskData.assignedTo,
        dueDate: taskData.dueDate,
        priority: taskData.priority,
        status: this.editingTask.status
      };
      this.taskService.updateTask(this.editingTask.id, updateRequest).subscribe({
        next: () => {
          this.toastService.success('Task updated');
          this.closeTaskModal();
          this.loadTasks();
        },
        error: (err) => this.toastService.error(err?.error?.message || 'Failed to update task')
      });
    } else {
      this.taskService.createTask(taskData).subscribe({
        next: () => {
          this.toastService.success('Tâche créée.');
          this.closeTaskModal();
          this.loadTasks();
        },
        error: (err) => {
          const e = err?.error;
          let msg = (typeof e === 'string' ? e : null) || e?.error || e?.message || err?.message || err?.statusText;
          if (!msg && e && typeof e === 'object') {
            const first = Object.values(e)[0];
            msg = Array.isArray(first) ? first[0] : String(first);
          }
          this.toastService.error(msg || 'Impossible de créer la tâche. Vérifiez la connexion et que le project-service est démarré.');
        }
      });
    }
  }

  deleteTask(taskId: number, taskTitle: string) {
    if (!confirm(`Delete "${taskTitle}"?`)) return;
    this.taskService.deleteTask(taskId).subscribe({
      next: () => {
        this.toastService.success('Task deleted');
        this.loadTasks();
      },
      error: () => this.toastService.error('Failed to delete task')
    });
  }

  updateTaskStatus(event: DragEvent, newStatus: TaskStatus) {
    const json = event.dataTransfer?.getData('task');
    if (!json) return;
    try {
      const task: Task = JSON.parse(json);
      if (!task.id) return;
      this.taskService.updateTask(task.id, { status: newStatus }).subscribe({
        next: () => {
          const idx = this.tasks.findIndex(t => t.id === task.id);
          if (idx !== -1) { this.tasks[idx].status = newStatus; this.organizeTasksByStatus(); }
        },
        error: () => this.toastService.error('Failed to update status')
      });
    } catch (_) {}
  }

  onTaskDragStart(event: DragEvent, task: Task) {
    if (event.dataTransfer) event.dataTransfer.setData('task', JSON.stringify(task));
  }

  getTaskStatusDisplay(status: TaskStatus): string {
    return this.taskService.getStatusDisplayName(status);
  }

  getPriorityDisplay(priority: number): string {
    return this.taskService.getPriorityDisplayName(priority);
  }

  getPriorityColorClass(priority: number): string {
    return this.taskService.getPriorityColorClass(priority);
  }

  formatDate(d?: string): string {
    if (!d) return '';
    return new Date(d).toLocaleDateString(undefined, { dateStyle: 'short' });
  }

  /** Assignable users in project room: client + freelancer */
  get taskAssignableUsers(): User[] {
    const list: User[] = [];
    if (this.clientUser) list.push(this.clientUser);
    if (this.freelancerUser && this.freelancerUser.id !== this.clientUser?.id) list.push(this.freelancerUser);
    return list;
  }
}
