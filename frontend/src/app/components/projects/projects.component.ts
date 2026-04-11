import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ToastService } from '../../services/toast.service';
import { ProjectService, Project, CreateProjectRequest, UpdateProjectRequest } from '../../services/project.service';
import { UserService, User } from '../../services/user.service';
import { TaskService, Task, TaskStatus, CreateTaskRequest, UpdateTaskRequest } from '../../services/task.service';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.css']
})
export class ProjectsComponent implements OnInit {
  projects: Project[] = [];
  filteredProjects: Project[] = [];
  isLoading = false;
  showAddModal = false;
  showEditModal = false;
  showDetailsModal = false;
  selectedProject: Project | null = null;
  projectOwner: User | null = null;
  loadingOwner = false;
  currentUserId: number | null = null;
  currentUser: User | null = null;
  isAdmin = false;
  editingProject: Project | null = null;
  
  // Tasks
  tasks: Task[] = [];
  tasksByStatus: { [key: string]: Task[] } = {
    'TO_DO': [],
    'IN_PROGRESS': [],
    'DONE': []
  };
  showTaskModal = false;
  editingTask: Task | null = null;
  newTask: CreateTaskRequest = {
    title: '',
    description: '',
    projectId: 0,
    createdBy: 0,
    priority: 0
  };
  loadingTasks = false;
  availableFreelancers: User[] = [];
  
  // Expose TaskStatus enum to template
  TaskStatus = TaskStatus;
  
  // View mode
  viewMode: 'grid' | 'kanban' = 'grid';
  showAdvancedFilters = false;
  
  // Statistics
  statistics = {
    total: 0,
    byStatus: {} as { [key: string]: number },
    byCategory: {} as { [key: string]: number },
    totalBudget: 0,
    averageBudget: 0,
    completionRate: 0
  };
  
  // Advanced filters
  budgetMin: number | null = null;
  budgetMax: number | null = null;
  dateFrom: string = '';
  dateTo: string = '';
  savedFilterPresets: any[] = [];
  
  // Bulk operations
  selectedProjects: Set<number> = new Set();
  selectAllMode = false;
  
  // Form data
  newProject: CreateProjectRequest = {
    title: '',
    description: '',
    status: 'DRAFT',
    ownerId: 0,
    budget: undefined,
    deadline: undefined,
    category: undefined,
    imageUrl: undefined,
    tags: undefined
  };
  
  editProject: UpdateProjectRequest = {
    title: '',
    description: '',
    status: 'DRAFT',
    budget: undefined,
    deadline: undefined,
    category: undefined,
    imageUrl: undefined,
    tags: undefined
  };
  
  // Filters
  searchTerm = '';
  selectedStatus = 'ALL';
  selectedCategory = 'ALL';
  sortBy = 'newest';
  
  // Available categories
  categories = [
    'Web Development',
    'Mobile Development',
    'UI/UX Design',
    'Graphic Design',
    'Digital Marketing',
    'Content Writing',
    'Data Science',
    'DevOps',
    'Other'
  ];

  constructor(
    private router: Router,
    private toastService: ToastService,
    private projectService: ProjectService,
    private userService: UserService,
    private taskService: TaskService,
    private keycloakService: KeycloakService
  ) {
    // Load saved filter presets from localStorage
    const saved = localStorage.getItem('projectFilterPresets');
    if (saved) {
      try {
        this.savedFilterPresets = JSON.parse(saved);
      } catch (e) {
        this.savedFilterPresets = [];
      }
    }
    
    // Load saved view mode
    const savedMode = localStorage.getItem('projectViewMode');
    if (savedMode === 'kanban' || savedMode === 'grid') {
      this.viewMode = savedMode;
    }
  }

  async ngOnInit() {
    await this.loadCurrentUser();
    await this.checkAdminRole();
    this.loadProjects();
  }

  async loadCurrentUser() {
    try {
      // Try to get user from service
      const user = await this.userService.getCurrentUser().toPromise();
      if (user && user.id) {
        this.currentUserId = user.id;
        this.currentUser = user;
        this.newProject.ownerId = user.id;
        console.log('User loaded successfully:', user.id);
      } else {
        // If user service fails, try to sync user first
        console.warn('User service returned no ID, trying to sync user...');
        await this.syncUser();
      }
    } catch (error: any) {
      console.error('Error loading current user from service:', error);
      // If 404 or user not found, try to sync
      if (error.status === 404) {
        console.log('User not found in database, attempting to sync...');
        await this.syncUser();
      } else {
        // Try alternative method
        await this.loadUserFromKeycloak();
      }
    }
  }

  async syncUser() {
    try {
      console.log('Syncing user to backend...');
      const user = await this.userService.syncUser().toPromise();
      if (user && user.id) {
        this.currentUserId = user.id;
        this.currentUser = user;
        this.newProject.ownerId = user.id;
        console.log('User synced successfully:', user.id);
      } else {
        await this.loadUserFromKeycloak();
      }
    } catch (error) {
      console.error('Error syncing user:', error);
      await this.loadUserFromKeycloak();
    }
  }

  async loadUserFromKeycloak() {
    try {
      // Try to get user ID from Keycloak token
      const token = await this.keycloakService.getToken();
      if (token) {
        const tokenPayload = JSON.parse(atob(token.split('.')[1]));
        const keycloakId = tokenPayload.sub;
        
        // Try to get user by keycloakId or use a fallback
        // For now, we'll try to get user from backend using keycloakId
        try {
          const user = await this.userService.getCurrentUser().toPromise();
          if (user && user.id) {
            this.currentUserId = user.id;
            this.currentUser = user;
            this.newProject.ownerId = user.id;
            console.log('User loaded from retry:', user.id);
            return;
          }
        } catch (e) {
          console.error('Retry failed:', e);
        }
        
        // Last resort: show error but allow user to try
        console.error('Could not load user ID');
        this.toastService.error('Unable to load user information. Please refresh the page or log out and log back in.');
      }
    } catch (error) {
      console.error('Error loading user from Keycloak:', error);
      this.toastService.error('Failed to load user information. Please refresh the page.');
    }
  }

  async checkAdminRole() {
    try {
      const roles = await this.keycloakService.getUserRoles();
      this.isAdmin = roles.some((role: string) => role.toUpperCase() === 'ADMIN');
    } catch (error) {
      console.error('Error checking admin role:', error);
    }
  }

  loadProjects() {
    this.isLoading = true;
    
    // Always load all projects for the projects page
    // Users can see all projects, but can only edit/delete their own
    this.projectService.getAllProjects().subscribe({
      next: (projects) => {
        this.projects = projects || [];
        this.calculateStatistics();
        this.applyFilters();
        this.isLoading = false;
        if (projects.length === 0) {
          console.log('No projects found');
        }
      },
      error: (error) => {
        console.error('Error loading projects:', error);
        if (error.status === 0 || error.status === 404) {
          this.toastService.error('Project service is not available. Please make sure the backend is running on port 8082.');
        } else {
          this.toastService.error('Failed to load projects: ' + (error.message || 'Unknown error'));
        }
        this.projects = [];
        this.filteredProjects = [];
        this.isLoading = false;
      }
    });
  }

  applyFilters() {
    let filtered = [...this.projects];
    
    // Search filter
    if (this.searchTerm) {
      filtered = filtered.filter(p => 
        p.title.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        (p.description && p.description.toLowerCase().includes(this.searchTerm.toLowerCase())) ||
        (p.category && p.category.toLowerCase().includes(this.searchTerm.toLowerCase())) ||
        (p.tags && p.tags.toLowerCase().includes(this.searchTerm.toLowerCase()))
      );
    }
    
    // Status filter
    if (this.selectedStatus !== 'ALL') {
      filtered = filtered.filter(p => p.status === this.selectedStatus);
    }
    
    // Category filter
    if (this.selectedCategory !== 'ALL') {
      filtered = filtered.filter(p => p.category === this.selectedCategory);
    }
    
    // Budget range filter
    if (this.budgetMin !== null && this.budgetMin !== undefined) {
      filtered = filtered.filter(p => (p.budget || 0) >= this.budgetMin!);
    }
    if (this.budgetMax !== null && this.budgetMax !== undefined) {
      filtered = filtered.filter(p => (p.budget || 0) <= this.budgetMax!);
    }
    
    // Date range filter
    if (this.dateFrom) {
      const fromDate = new Date(this.dateFrom);
      filtered = filtered.filter(p => {
        if (!p.createdAt) return false;
        return new Date(p.createdAt) >= fromDate;
      });
    }
    if (this.dateTo) {
      const toDate = new Date(this.dateTo);
      toDate.setHours(23, 59, 59, 999); // End of day
      filtered = filtered.filter(p => {
        if (!p.createdAt) return false;
        return new Date(p.createdAt) <= toDate;
      });
    }
    
    // Sort
    if (this.sortBy === 'newest') {
      filtered.sort((a, b) => {
        const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
        const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
        return dateB - dateA;
      });
    } else if (this.sortBy === 'budget') {
      filtered.sort((a, b) => (b.budget || 0) - (a.budget || 0));
    } else if (this.sortBy === 'status') {
      filtered.sort((a, b) => a.status.localeCompare(b.status));
    }
    
    this.filteredProjects = filtered;
  }

  navigateTo(route: string) {
    this.router.navigate([route]);
  }

  addProject() {
    this.newProject = {
      title: '',
      description: '',
      status: 'DRAFT',
      ownerId: this.currentUserId || 0,
      budget: undefined,
      deadline: undefined,
      category: undefined,
      imageUrl: undefined,
      tags: undefined
    };
    this.showAddModal = true;
  }

  closeModal() {
    this.showAddModal = false;
  }

  async saveProject() {
    // Validation côté client - plus détaillée
    if (!this.newProject.title || !this.newProject.title.trim()) {
      this.toastService.error('Project title is required');
      return;
    }
    
    const trimmedTitle = this.newProject.title.trim();
    if (trimmedTitle.length < 3) {
      this.toastService.error('Title must be at least 3 characters');
      return;
    }
    
    if (trimmedTitle.length > 200) {
      this.toastService.error('Title cannot exceed 200 characters');
      return;
    }
    
    // Check category - REQUIRED
    if (!this.newProject.category || this.newProject.category.trim() === '') {
      this.toastService.error('Category is required');
      return;
    }
    
    // Check budget - REQUIRED
    if (this.newProject.budget === undefined || this.newProject.budget === null) {
      this.toastService.error('Budget is required');
      return;
    }
    if (this.newProject.budget < 0) {
      this.toastService.error('Budget must be positive or zero');
      return;
    }
    // Vérifier le format (max 10 chiffres avant la virgule, 2 après)
    const budgetStr = this.newProject.budget.toString();
    const parts = budgetStr.split('.');
    if (parts[0].length > 10) {
      this.toastService.error('Budget cannot exceed 10 digits before decimal point');
      return;
    }
    if (parts[1] && parts[1].length > 2) {
      this.toastService.error('Budget cannot have more than 2 decimal places');
      return;
    }
    
    // Check status - REQUIRED
    if (!this.newProject.status) {
      this.toastService.error('Status is required');
      return;
    }
    
    // Check deadline - REQUIRED
    if (!this.newProject.deadline) {
      this.toastService.error('Deadline is required');
      return;
    }
    
    if (this.newProject.description && this.newProject.description.length > 5000) {
      this.toastService.error('Description cannot exceed 5000 characters');
      return;
    }
    
    if (this.newProject.imageUrl && this.newProject.imageUrl.trim()) {
      if (this.newProject.imageUrl.length > 500) {
        this.toastService.error('Image URL cannot exceed 500 characters');
        return;
      }
      // Validation d'URL basique
      try {
        new URL(this.newProject.imageUrl);
      } catch (e) {
        this.toastService.error('Please enter a valid URL for the image');
        return;
      }
    }
    
    if (this.newProject.tags && this.newProject.tags.length > 500) {
      this.toastService.error('Tags cannot exceed 500 characters');
      return;
    }
    
    if (this.newProject.category && this.newProject.category.length > 100) {
      this.toastService.error('Category cannot exceed 100 characters');
      return;
    }
    
    // Vérifier que ownerId est présent
    if (!this.newProject.ownerId || this.newProject.ownerId === 0) {
      this.toastService.error('Owner ID is missing. Please refresh the page.');
      return;
    }

    // Try to reload user if missing
    if (!this.newProject.ownerId || this.newProject.ownerId === 0) {
      console.log('Owner ID missing, attempting to reload user...');
      await this.loadCurrentUser();
      
      // If still missing, try one more time with delay
      if (!this.newProject.ownerId || this.newProject.ownerId === 0) {
        await new Promise(resolve => setTimeout(resolve, 500));
        await this.loadCurrentUser();
      }
      
      if (!this.newProject.ownerId || this.newProject.ownerId === 0) {
        this.toastService.error('User information is missing. Please refresh the page or log out and log back in.');
        return;
      }
    }

    // Ensure status is set
    if (!this.newProject.status) {
      this.newProject.status = 'DRAFT';
    }

    console.log('Creating project with data:', this.newProject);

    this.projectService.createProject(this.newProject).subscribe({
      next: (project) => {
        console.log('Project created successfully:', project);
        this.toastService.success('Project created successfully!');
        this.closeModal();
        // Small delay to ensure backend has saved
        setTimeout(() => {
          this.loadProjects();
        }, 300);
      },
      error: (error) => {
        console.error('Error creating project:', error);
        if (error.status === 0 || error.status === 404) {
          this.toastService.error('Project service is not available. Please make sure the backend is running on port 8082.');
        } else if (error.status === 400) {
          this.toastService.error('Invalid project data. Please check all fields.');
        } else {
          this.toastService.error('Failed to create project: ' + (error.error?.error || error.message || 'Unknown error'));
        }
      }
    });
  }

  editProjectClick(project: Project) {
    this.editingProject = project;
    
    // Format deadline for datetime-local input (YYYY-MM-DDTHH:mm)
    let deadlineFormatted = '';
    if (project.deadline) {
      const deadlineDate = new Date(project.deadline);
      deadlineFormatted = deadlineDate.toISOString().slice(0, 16);
    }
    
    this.editProject = {
      title: project.title,
      description: project.description || '',
      status: project.status,
      budget: project.budget,
      deadline: deadlineFormatted || undefined,
      category: project.category,
      imageUrl: project.imageUrl,
      tags: project.tags
    };
    this.showEditModal = true;
  }

  closeEditModal() {
    this.showEditModal = false;
    this.editingProject = null;
  }

  updateProject() {
    if (!this.editingProject || !this.editProject.title) {
      this.toastService.error('Title is required');
      return;
    }

    this.projectService.updateProject(this.editingProject.id!, this.editProject).subscribe({
      next: (project) => {
        this.toastService.success('Project updated successfully!');
        this.closeEditModal();
        this.loadProjects();
      },
      error: (error) => {
        console.error('Error updating project:', error);
        this.toastService.error('Failed to update project');
      }
    });
  }

  deleteProject(id: number, title: string) {
    // Find the project to check permissions
    const project = this.projects.find(p => p.id === id);
    if (project && !this.canDeleteProject(project)) {
      this.toastService.error('You do not have permission to delete this project');
      return;
    }
    
    if (confirm(`Are you sure you want to delete "${title}"?`)) {
      this.projectService.deleteProject(id).subscribe({
        next: () => {
          this.toastService.success(`Project "${title}" has been deleted`);
          this.loadProjects();
        },
        error: (error) => {
          console.error('Error deleting project:', error);
          this.toastService.error('Failed to delete project');
        }
      });
    }
  }

  canEditProject(project: Project): boolean {
    // Admin can edit all projects, users can only edit their own
    return this.isAdmin || project.ownerId === this.currentUserId;
  }

  canDeleteProject(project: Project): boolean {
    // Admin can delete all projects, users can only delete their own
    if (this.isAdmin) {
      return true;
    }
    // For regular users, check if they own the project
    if (project.ownerId && this.currentUserId) {
      return project.ownerId === this.currentUserId;
    }
    return false;
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  formatDateTime(dateString?: string): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatCurrency(amount?: number): string {
    if (!amount) return 'N/A';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  getStatusClass(status: string): string {
    const statusMap: { [key: string]: string } = {
      'DRAFT': 'status-pending',
      'OPEN': 'status-active',
      'IN_PROGRESS': 'status-active',
      'COMPLETED': 'status-completed',
      'CANCELLED': 'status-cancelled'
    };
    return statusMap[status] || 'status-pending';
  }

  getStatusDisplay(status: string): string {
    const statusMap: { [key: string]: string } = {
      'DRAFT': 'Draft',
      'OPEN': 'Open',
      'IN_PROGRESS': 'In Progress',
      'COMPLETED': 'Completed',
      'CANCELLED': 'Cancelled'
    };
    return statusMap[status] || status;
  }

  /**
   * Get default placeholder image based on category
   */
  getDefaultImage(category?: string): string {
    const categoryImages: { [key: string]: string } = {
      'Web Development': 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=400&h=300&fit=crop',
      'Mobile Development': 'https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=400&h=300&fit=crop',
      'UI/UX Design': 'https://images.unsplash.com/photo-1561070791-2526d30994b5?w=400&h=300&fit=crop',
      'Graphic Design': 'https://images.unsplash.com/photo-1561070791-2526d30994b5?w=400&h=300&fit=crop',
      'Digital Marketing': 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=400&h=300&fit=crop',
      'Content Writing': 'https://images.unsplash.com/photo-1455390582262-044cdead277a?w=400&h=300&fit=crop',
      'Data Science': 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=400&h=300&fit=crop',
      'DevOps': 'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=400&h=300&fit=crop',
      'Other': 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=400&h=300&fit=crop'
    };
    return categoryImages[category || 'Other'] || categoryImages['Other'];
  }

  /**
   * Get project image URL (use uploaded image or default based on category)
   */
  getProjectImage(project: Project): string {
    if (project.imageUrl) {
      return project.imageUrl;
    }
    return this.getDefaultImage(project.category);
  }

  /**
   * Get unique categories from projects
   */
  getAvailableCategories(): string[] {
    const cats = this.projects
      .map(p => p.category)
      .filter((cat): cat is string => !!cat)
      .filter((cat, index, self) => self.indexOf(cat) === index);
    return cats.sort();
  }

  /**
   * Handle image load error - set fallback image
   */
  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    if (img) {
      img.src = 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=400&h=300&fit=crop';
    }
  }

  /**
   * Check if form is valid for submission
   */
  isFormValid(): boolean {
    // Check title - REQUIRED
    if (!this.newProject.title || !this.newProject.title.trim()) {
      return false;
    }
    const trimmedTitle = this.newProject.title.trim();
    if (trimmedTitle.length < 3 || trimmedTitle.length > 200) {
      return false;
    }
    
    // Check category - REQUIRED
    if (!this.newProject.category || this.newProject.category.trim() === '') {
      return false;
    }
    
    // Check budget - REQUIRED
    if (this.newProject.budget === undefined || this.newProject.budget === null) {
      return false;
    }
    if (this.newProject.budget < 0) {
      return false;
    }
    
    // Check status - REQUIRED
    if (!this.newProject.status) {
      return false;
    }
    
    // Check deadline - REQUIRED
    if (!this.newProject.deadline) {
      return false;
    }
    
    // Check description length - OPTIONAL but must be valid if provided
    if (this.newProject.description && this.newProject.description.length > 5000) {
      return false;
    }
    
    // Check image URL - OPTIONAL but must be valid if provided
    if (this.newProject.imageUrl && this.newProject.imageUrl.trim()) {
      if (this.newProject.imageUrl.length > 500) {
        return false;
      }
      // Don't validate URL format here - backend will handle it
      // Empty or invalid URL format won't block submission
    }
    
    // Check tags length - OPTIONAL but must be valid if provided
    if (this.newProject.tags && this.newProject.tags.length > 500) {
      return false;
    }
    
    // Don't check ownerId here - saveProject() will handle it
    // This allows the button to be enabled even if ownerId needs to be reloaded
    return true;
  }

  /**
   * Check if edit form is valid for submission
   */
  isEditFormValid(): boolean {
    // Check title - REQUIRED
    if (!this.editProject.title || !this.editProject.title.trim()) {
      return false;
    }
    const trimmedTitle = this.editProject.title.trim();
    if (trimmedTitle.length < 3 || trimmedTitle.length > 200) {
      return false;
    }
    
    // Check category - REQUIRED
    if (!this.editProject.category || this.editProject.category.trim() === '') {
      return false;
    }
    
    // Check budget - REQUIRED
    if (this.editProject.budget === undefined || this.editProject.budget === null) {
      return false;
    }
    if (this.editProject.budget < 0) {
      return false;
    }
    
    // Check status - REQUIRED
    if (!this.editProject.status) {
      return false;
    }
    
    // Check deadline - REQUIRED
    if (!this.editProject.deadline) {
      return false;
    }
    
    // Check description length - OPTIONAL but must be valid if provided
    if (this.editProject.description && this.editProject.description.length > 5000) {
      return false;
    }
    
    // Check image URL - OPTIONAL but must be valid if provided
    if (this.editProject.imageUrl && this.editProject.imageUrl.trim()) {
      if (this.editProject.imageUrl.length > 500) {
        return false;
      }
    }
    
    // Check tags length - OPTIONAL but must be valid if provided
    if (this.editProject.tags && this.editProject.tags.length > 500) {
      return false;
    }
    
    return true;
  }

  /**
   * Validate URL format
   */
  isValidUrl(url: string): boolean {
    if (!url || !url.trim()) return true; // Empty is valid (optional)
    try {
      new URL(url);
      return true;
    } catch {
      return false;
    }
  }

  // ========== PROJECT DETAILS MODAL ==========
  viewProjectDetails(project: Project) {
    this.selectedProject = project;
    this.showDetailsModal = true;
    this.loadProjectOwner(project.ownerId);
    if (project.id) {
      this.loadTasks(project.id);
      this.loadFreelancers();
    }
  }

  closeDetailsModal() {
    this.showDetailsModal = false;
    this.selectedProject = null;
    this.projectOwner = null;
    this.tasks = [];
    this.tasksByStatus = { 'TO_DO': [], 'IN_PROGRESS': [], 'DONE': [] };
  }
  
  /**
   * Load tasks for a project
   */
  loadTasks(projectId: number) {
    this.loadingTasks = true;
    this.taskService.getTasksByProject(projectId).subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.organizeTasksByStatus();
        this.loadingTasks = false;
      },
      error: (error) => {
        console.error('Error loading tasks:', error);
        this.toastService.error('Failed to load tasks');
        this.loadingTasks = false;
      }
    });
  }
  
  /**
   * Organize tasks by status for Kanban view
   */
  organizeTasksByStatus() {
    this.tasksByStatus = {
      'TO_DO': [],
      'IN_PROGRESS': [],
      'DONE': []
    };
    this.tasks.forEach(task => {
      if (this.tasksByStatus[task.status]) {
        this.tasksByStatus[task.status].push(task);
      }
    });
  }
  
  /**
   * Load available freelancers for task assignment
   */
  loadFreelancers() {
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        // Filter users with FREELANCER role
        this.availableFreelancers = users.filter(u => 
          u.role === 'FREELANCER'
        );
      },
      error: (error) => {
        console.error('Error loading freelancers:', error);
      }
    });
  }
  
  /**
   * Open task creation modal
   */
  openTaskModal(task?: Task) {
    console.log('openTaskModal called', task);
    console.log('selectedProject:', this.selectedProject);
    console.log('currentUserId:', this.currentUserId);
    
    if (task) {
      this.editingTask = task;
      this.newTask = {
        title: task.title,
        description: task.description || '',
        projectId: task.projectId,
        assignedTo: task.assignedTo,
        createdBy: task.createdBy,
        dueDate: task.dueDate,
        priority: task.priority
      };
    } else {
      this.editingTask = null;
      
      // Ensure we have the project ID and user ID
      if (!this.selectedProject?.id) {
        this.toastService.error('No project selected');
        return;
      }
      
      if (!this.currentUserId || this.currentUserId === 0) {
        this.toastService.error('User ID is missing. Please refresh the page.');
        console.error('currentUserId is missing:', this.currentUserId);
        return;
      }
      
      this.newTask = {
        title: '',
        description: '',
        projectId: this.selectedProject.id,
        createdBy: this.currentUserId,
        priority: 0
      };
    }
    this.showTaskModal = true;
  }
  
  /**
   * Close task modal
   */
  closeTaskModal() {
    this.showTaskModal = false;
    this.editingTask = null;
    this.newTask = {
      title: '',
      description: '',
      projectId: 0,
      createdBy: 0,
      priority: 0
    };
  }
  
  /**
   * Save task (create or update)
   */
  saveTask() {
    console.log('saveTask called');
    console.log('newTask:', this.newTask);
    console.log('selectedProject:', this.selectedProject);
    console.log('currentUserId:', this.currentUserId);
    
    if (!this.newTask.title || this.newTask.title.trim().length < 3) {
      this.toastService.error('Task title must be at least 3 characters');
      return;
    }
    
    if (!this.selectedProject?.id) {
      this.toastService.error('Project ID is missing');
      return;
    }
    
    if (!this.currentUserId || this.currentUserId === 0) {
      this.toastService.error('User ID is missing. Please refresh the page.');
      console.error('currentUserId is missing:', this.currentUserId);
      return;
    }
    
    // Prepare task data
    const taskData: CreateTaskRequest = {
      title: this.newTask.title.trim(),
      description: this.newTask.description?.trim() || undefined,
      projectId: this.selectedProject.id,
      createdBy: this.currentUserId,
      assignedTo: this.newTask.assignedTo || undefined,
      dueDate: this.newTask.dueDate || undefined,
      priority: this.newTask.priority || 0
    };
    
    console.log('Task data to send:', taskData);
    
    if (this.editingTask && this.editingTask.id) {
      // Update existing task
      const updateRequest: UpdateTaskRequest = {
        title: taskData.title,
        description: taskData.description,
        assignedTo: taskData.assignedTo,
        dueDate: taskData.dueDate,
        priority: taskData.priority,
        status: this.editingTask.status
      };
      
      console.log('Updating task:', this.editingTask.id, updateRequest);
      
      this.taskService.updateTask(this.editingTask.id, updateRequest).subscribe({
        next: (response) => {
          console.log('Task updated successfully:', response);
          this.toastService.success('Task updated successfully');
          this.closeTaskModal();
          if (this.selectedProject?.id) {
            this.loadTasks(this.selectedProject.id);
          }
        },
        error: (error) => {
          console.error('Error updating task:', error);
          console.error('Error details:', JSON.stringify(error, null, 2));
          const errorMessage = error?.error?.message || error?.message || 'Failed to update task';
          this.toastService.error(errorMessage);
        }
      });
    } else {
      // Create new task
      console.log('Creating new task:', taskData);
      
      this.taskService.createTask(taskData).subscribe({
        next: (response) => {
          console.log('Task created successfully:', response);
          this.toastService.success('Task created successfully');
          this.closeTaskModal();
          if (this.selectedProject?.id) {
            this.loadTasks(this.selectedProject.id);
          }
        },
        error: (error) => {
          console.error('Error creating task:', error);
          console.error('Error details:', JSON.stringify(error, null, 2));
          console.error('Error status:', error?.status);
          console.error('Error error:', error?.error);
          
          // Extract error message
          let errorMessage = 'Failed to create task';
          if (error?.error) {
            if (typeof error.error === 'string') {
              errorMessage = error.error;
            } else if (error.error.message) {
              errorMessage = error.error.message;
            } else if (error.error.error) {
              errorMessage = error.error.error;
            } else if (Object.keys(error.error).length > 0) {
              // Validation errors
              const firstError = Object.values(error.error)[0];
              errorMessage = Array.isArray(firstError) ? firstError[0] : String(firstError);
            }
          }
          
          this.toastService.error(errorMessage);
        }
      });
    }
  }
  
  /**
   * Delete a task
   */
  deleteTask(taskId: number, taskTitle: string) {
    if (confirm(`Are you sure you want to delete "${taskTitle}"?`)) {
      this.taskService.deleteTask(taskId).subscribe({
        next: () => {
          this.toastService.success('Task deleted successfully');
          if (this.selectedProject?.id) {
            this.loadTasks(this.selectedProject.id);
          }
        },
        error: (error) => {
          console.error('Error deleting task:', error);
          this.toastService.error('Failed to delete task');
        }
      });
    }
  }
  
  /**
   * Update task status (for drag & drop)
   */
  updateTaskStatus(event: DragEvent, newStatus: TaskStatus) {
    event.preventDefault();
    const taskData = event.dataTransfer?.getData('task');
    if (!taskData) return;
    
    try {
      const task: Task = JSON.parse(taskData);
      if (!task.id) return;
      
      const updateRequest: UpdateTaskRequest = {
        status: newStatus
      };
      
      this.taskService.updateTask(task.id, updateRequest).subscribe({
        next: () => {
          // Update local task
          const taskIndex = this.tasks.findIndex(t => t.id === task.id);
          if (taskIndex !== -1) {
            this.tasks[taskIndex].status = newStatus;
            this.organizeTasksByStatus();
          }
        },
        error: (error) => {
          console.error('Error updating task status:', error);
          this.toastService.error('Failed to update task status');
        }
      });
    } catch (e) {
      console.error('Error parsing task data:', e);
    }
  }
  
  /**
   * Get status display name
   */
  getTaskStatusDisplay(status: TaskStatus): string {
    return this.taskService.getStatusDisplayName(status);
  }
  
  /**
   * Get priority display name
   */
  getPriorityDisplay(priority: number): string {
    return this.taskService.getPriorityDisplayName(priority);
  }
  
  /**
   * Get priority color class
   */
  getPriorityColorClass(priority: number): string {
    return this.taskService.getPriorityColorClass(priority);
  }
  
  /**
   * Check if user can manage tasks (owner or admin)
   */
  canManageTasks(): boolean {
    if (this.isAdmin) return true;
    if (this.selectedProject && this.currentUserId) {
      return this.selectedProject.ownerId === this.currentUserId;
    }
    return false;
  }

  loadProjectOwner(ownerId: number) {
    this.loadingOwner = true;
    this.userService.getUserById(ownerId).subscribe({
      next: (user) => {
        this.projectOwner = user;
        this.loadingOwner = false;
      },
      error: (error) => {
        console.error('Error loading project owner:', error);
        this.loadingOwner = false;
      }
    });
  }

  // ========== STATISTICS ==========
  calculateStatistics() {
    const stats = {
      total: this.projects.length,
      byStatus: {} as { [key: string]: number },
      byCategory: {} as { [key: string]: number },
      totalBudget: 0,
      averageBudget: 0,
      completionRate: 0
    };

    let completedCount = 0;
    let budgetCount = 0;

    this.projects.forEach(project => {
      const status = project.status || 'DRAFT';
      stats.byStatus[status] = (stats.byStatus[status] || 0) + 1;
      if (status === 'COMPLETED') completedCount++;

      const category = project.category || 'Uncategorized';
      stats.byCategory[category] = (stats.byCategory[category] || 0) + 1;

      if (project.budget) {
        stats.totalBudget += project.budget;
        budgetCount++;
      }
    });

    stats.averageBudget = budgetCount > 0 ? stats.totalBudget / budgetCount : 0;
    stats.completionRate = stats.total > 0 ? (completedCount / stats.total) * 100 : 0;

    this.statistics = stats;
  }

  // ========== VIEW MODE ==========
  setViewMode(mode: 'grid' | 'kanban') {
    this.viewMode = mode;
    localStorage.setItem('projectViewMode', mode);
  }

  // ========== ADVANCED FILTERING ==========
  clearAdvancedFilters() {
    this.budgetMin = null;
    this.budgetMax = null;
    this.dateFrom = '';
    this.dateTo = '';
    this.applyFilters();
  }

  saveFilterPreset() {
    const preset = {
      name: `Preset ${this.savedFilterPresets.length + 1}`,
      filters: {
        searchTerm: this.searchTerm,
        selectedStatus: this.selectedStatus,
        selectedCategory: this.selectedCategory,
        sortBy: this.sortBy,
        budgetMin: this.budgetMin,
        budgetMax: this.budgetMax,
        dateFrom: this.dateFrom,
        dateTo: this.dateTo
      }
    };
    this.savedFilterPresets.push(preset);
    localStorage.setItem('projectFilterPresets', JSON.stringify(this.savedFilterPresets));
    this.toastService.success('Filter preset saved!');
  }

  loadFilterPreset(preset: any) {
    this.searchTerm = preset.filters.searchTerm || '';
    this.selectedStatus = preset.filters.selectedStatus || 'ALL';
    this.selectedCategory = preset.filters.selectedCategory || 'ALL';
    this.sortBy = preset.filters.sortBy || 'newest';
    this.budgetMin = preset.filters.budgetMin || null;
    this.budgetMax = preset.filters.budgetMax || null;
    this.dateFrom = preset.filters.dateFrom || '';
    this.dateTo = preset.filters.dateTo || '';
    this.applyFilters();
  }

  // ========== BULK OPERATIONS ==========
  toggleSelectAll() {
    if (this.selectAllMode) {
      this.selectedProjects.clear();
    } else {
      this.filteredProjects.forEach(p => {
        if (p.id) this.selectedProjects.add(p.id);
      });
    }
    this.selectAllMode = !this.selectAllMode;
  }

  toggleProjectSelection(projectId: number | undefined) {
    if (!projectId) return;
    if (this.selectedProjects.has(projectId)) {
      this.selectedProjects.delete(projectId);
    } else {
      this.selectedProjects.add(projectId);
    }
  }

  isProjectSelected(projectId: number | undefined): boolean {
    return projectId ? this.selectedProjects.has(projectId) : false;
  }

  onBulkStatusChange(event: Event) {
    const select = event.target as HTMLSelectElement;
    const newStatus = select.value;
    if (!newStatus) return;
    this.bulkStatusChange(newStatus);
    select.value = ''; // Reset dropdown
  }

  bulkStatusChange(newStatus: string) {
    if (this.selectedProjects.size === 0) {
      this.toastService.error('Please select at least one project');
      return;
    }

    const updates = Array.from(this.selectedProjects).map(id => {
      return this.projectService.updateProject(id, { status: newStatus as any }).toPromise();
    });

    Promise.all(updates).then(() => {
      this.toastService.success(`Updated ${this.selectedProjects.size} project(s)`);
      this.selectedProjects.clear();
      this.selectAllMode = false;
      this.loadProjects();
    }).catch(error => {
      this.toastService.error('Failed to update projects');
      console.error(error);
    });
  }

  getStatusTypesCount(): number {
    return Object.keys(this.statistics.byStatus).length;
  }

  bulkDelete() {
    if (this.selectedProjects.size === 0) {
      this.toastService.error('Please select at least one project');
      return;
    }

    if (!confirm(`Are you sure you want to delete ${this.selectedProjects.size} project(s)?`)) {
      return;
    }

    const deletes = Array.from(this.selectedProjects).map(id => {
      return this.projectService.deleteProject(id).toPromise();
    });

    Promise.all(deletes).then(() => {
      this.toastService.success(`Deleted ${this.selectedProjects.size} project(s)`);
      this.selectedProjects.clear();
      this.selectAllMode = false;
      this.loadProjects();
    }).catch(error => {
      this.toastService.error('Failed to delete projects');
      console.error(error);
    });
  }

  exportToCSV() {
    const headers = ['Title', 'Description', 'Status', 'Category', 'Budget', 'Deadline', 'Created At', 'Tags'];
    const rows = this.filteredProjects.map(p => [
      p.title || '',
      p.description || '',
      p.status || '',
      p.category || '',
      p.budget?.toString() || '',
      p.deadline || '',
      p.createdAt || '',
      p.tags || ''
    ]);

    const csvContent = [
      headers.join(','),
      ...rows.map(row => row.map(cell => `"${cell.replace(/"/g, '""')}"`).join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `projects_${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
    this.toastService.success('Projects exported to CSV');
  }

  // ========== KANBAN VIEW ==========
  getProjectsByStatus(status: string): Project[] {
    return this.filteredProjects.filter(p => p.status === status);
  }

  onKanbanDrop(event: any, newStatus: string) {
    event.preventDefault();
    const projectId = event.dataTransfer.getData('projectId');
    const project = this.projects.find(p => p.id === parseInt(projectId));
    if (project && project.id) {
      this.projectService.updateProject(project.id, { status: newStatus as any }).subscribe({
        next: () => {
          this.toastService.success('Project status updated');
          this.loadProjects();
        },
        error: (error) => {
          this.toastService.error('Failed to update project status');
          console.error(error);
        }
      });
    }
  }

  onKanbanDragStart(event: DragEvent, project: Project) {
    if (project.id) {
      event.dataTransfer?.setData('projectId', project.id.toString());
    }
  }

  onKanbanDragOver(event: DragEvent) {
    event.preventDefault();
  }
  
  /**
   * Handle task drag start
   */
  onTaskDragStart(event: DragEvent, task: Task) {
    if (event.dataTransfer) {
      event.dataTransfer.setData('task', JSON.stringify(task));
    }
  }
}
