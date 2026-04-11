import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { ProfileComponent } from './components/profile/profile.component';
import { AdminProfileComponent } from './components/admin-profile/admin-profile.component';
import { LoginComponent } from './components/login/login.component';
import { SignupComponent } from './components/signup/signup.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './components/reset-password/reset-password.component';
import { UsersComponent } from './components/users/users.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ProjectsComponent } from './components/projects/projects.component';
import { SettingsComponent } from './components/settings/settings.component';
import { PublicProfileComponent } from './components/public-profile/public-profile.component';
import { BrowseUsersComponent } from './components/browse-users/browse-users.component';
import { MessagingComponent } from './components/messaging/messaging.component';
import { ProjectDashboardComponent } from './components/project-dashboard/project-dashboard.component';
import { FeedComponent } from './components/feed/feed.component';
import { FeedAbonnementsComponent } from './components/feed-abonnements/feed-abonnements.component';
import { TestComponent } from './components/test/test.component';
import { AddQuestionComponent } from './components/add-question/add-question.component';
import { TestIntroComponent } from './components/test-intro/test-intro.component';
import { TestPassComponent } from './components/test-pass/test-pass.component';
import { AffectationComponent } from './components/affectation/affectation.component';
import { FreelancerOnlyComponent } from './components/freelancer-only/freelancer-only.component';
import { ComplaintsComponent } from './components/complaints/complaints.component';
import { AdminComplaintsComponent } from './components/admin-complaints/admin-complaints.component';
import { PenaltiesComponent } from './components/penalties/penalties.component';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';
import { FreelancerGuard } from './guards/freelancer.guard';
import { TestPassDeactivateGuard } from './guards/test-pass-deactivate.guard';

const routes: Routes = [
  // User-facing routes
  { path: '', component: HomeComponent },
  { path: 'home', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'browse-users', component: BrowseUsersComponent, canActivate: [AuthGuard] },
  { path: 'messages', component: MessagingComponent, canActivate: [AuthGuard] },
  { path: 'messages/:userId', component: MessagingComponent, canActivate: [AuthGuard] },
  { path: 'user/:id', component: PublicProfileComponent, canActivate: [AuthGuard] },
  { path: 'user/username/:username', component: PublicProfileComponent, canActivate: [AuthGuard] },
  { path: 'projects', component: ProjectsComponent, canActivate: [AuthGuard] },
  { path: 'projects/:projectId/dashboard', component: ProjectDashboardComponent, canActivate: [AuthGuard] },
  { path: 'feed', component: FeedComponent, canActivate: [AuthGuard] },
  { path: 'feed/abonnements', component: FeedAbonnementsComponent, canActivate: [AuthGuard] },
  { path: 'test', component: TestIntroComponent, canActivate: [FreelancerGuard] },
  { path: 'test/pass', component: TestPassComponent, canActivate: [FreelancerGuard], canDeactivate: [TestPassDeactivateGuard] },
  { path: 'freelancer-only', component: FreelancerOnlyComponent, canActivate: [AuthGuard] },
  { path: 'addquestion', component: AddQuestionComponent, canActivate: [AuthGuard] },
  { path: 'complaints', component: ComplaintsComponent, canActivate: [AuthGuard] },
  { path: 'penalties', component: PenaltiesComponent, canActivate: [AuthGuard] },

  // Admin routes
  { path: 'admin/dashboard', component: DashboardComponent, canActivate: [AdminGuard] },
  { path: 'admin/profile', component: AdminProfileComponent, canActivate: [AdminGuard] },
  { path: 'users', component: UsersComponent, canActivate: [AdminGuard] },
  { path: 'admin/users', component: UsersComponent, canActivate: [AdminGuard] },
  { path: 'admin/users/:id', component: PublicProfileComponent, canActivate: [AdminGuard] },
  { path: 'admin/test', component: TestComponent, canActivate: [AdminGuard] },
  { path: 'admin/complaints', component: AdminComplaintsComponent, canActivate: [AdminGuard] },
  { path: 'admin/affectations', component: AffectationComponent, canActivate: [AdminGuard] },
  { path: 'admin/projects', component: ProjectsComponent, canActivate: [AdminGuard] },
  { path: 'admin/settings', component: SettingsComponent, canActivate: [AdminGuard] },
  
  // Fallback route
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
