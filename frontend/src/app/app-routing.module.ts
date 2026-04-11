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
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';
import { ComplaintsComponent } from './components/complaints/complaints.component';
import { AdminComplaintsComponent } from './components/admin-complaints/admin-complaints.component';
import { PenaltiesComponent } from './components/penalties/penalties.component';
import { SubscriptionPlansComponent } from './components/subscription-plans/subscription-plans.component';
import { MySubscriptionComponent } from './components/my-subscription/my-subscription.component';
import { AdminSubscriptionsComponent } from './components/admin-subscriptions/admin-subscriptions.component';
import { ContractsComponent } from './components/contracts/contracts.component';
import { PaymentsComponent } from './components/payments/payments.component';
import { MyContractsComponent } from './components/my-contracts/my-contracts.component';
import { ContractAnalyticsComponent } from './components/contract-analytics/contract-analytics.component';
import { AdminBadgesComponent } from './components/admin-badges/admin-badges.component';
import { LeaderboardComponent } from './components/leaderboard/leaderboard.component';
import { MyBadgesComponent } from './components/my-badges/my-badges.component';

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
  { path: 'complaints', component: ComplaintsComponent, canActivate: [AuthGuard] },
  { path: 'penalties', component: PenaltiesComponent, canActivate: [AuthGuard] },
  { path: 'subscription-plans', component: SubscriptionPlansComponent, canActivate: [AuthGuard] },
  { path: 'my-subscription', component: MySubscriptionComponent, canActivate: [AuthGuard] },
  { path: 'contract', component: MyContractsComponent, canActivate: [AuthGuard] },
  { path: 'my-badges', component: MyBadgesComponent, canActivate: [AuthGuard] },
  { path: 'leaderboard', component: LeaderboardComponent, canActivate: [AuthGuard] },

  // Admin routes
  { path: 'admin/dashboard', component: DashboardComponent, canActivate: [AdminGuard] },
  { path: 'admin/profile', component: AdminProfileComponent, canActivate: [AdminGuard] },
  { path: 'admin/complaints', component: AdminComplaintsComponent, canActivate: [AdminGuard] },
  { path: 'admin/users', component: UsersComponent, canActivate: [AdminGuard] },
  { path: 'admin/subscriptions', component: AdminSubscriptionsComponent, canActivate: [AdminGuard] },
  { path: 'admin/contracts', component: ContractsComponent, canActivate: [AdminGuard] },
  { path: 'admin/payments', component: PaymentsComponent, canActivate: [AdminGuard] },
  { path: 'admin/contract-analytics', component: ContractAnalyticsComponent, canActivate: [AdminGuard] },
  { path: 'admin/badges', component: AdminBadgesComponent, canActivate: [AdminGuard] },
  { path: 'admin/leaderboard', component: LeaderboardComponent, canActivate: [AdminGuard] },
  { path: 'admin/test', component: SettingsComponent, canActivate: [AdminGuard] },
  { path: 'admin/projects', component: ProjectsComponent, canActivate: [AdminGuard] },
  { path: 'admin/settings', component: SettingsComponent, canActivate: [AdminGuard] },
  { path: 'users', component: UsersComponent, canActivate: [AdminGuard] },
  
  // Fallback route
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
