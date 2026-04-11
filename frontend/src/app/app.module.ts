import { NgModule, APP_INITIALIZER } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';
import { AuthInterceptor } from './interceptors/auth.interceptor';
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
import { ComplaintsComponent } from './components/complaints/complaints.component';
import { AdminComplaintsComponent } from './components/admin-complaints/admin-complaints.component';
import { PenaltiesComponent } from './components/penalties/penalties.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { FooterComponent } from './components/footer/footer.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { HeaderComponent } from './components/header/header.component';
import { ToastComponent } from './components/toast/toast.component';
import { SubscriptionPlansComponent } from './components/subscription-plans/subscription-plans.component';
import { MySubscriptionComponent } from './components/my-subscription/my-subscription.component';
import { AdminSubscriptionsComponent } from './components/admin-subscriptions/admin-subscriptions.component';
import { SubscriptionButtonComponent } from './components/subscription-button/subscription-button.component';
import { ContractsComponent } from './components/contracts/contracts.component';
import { SignatureModalComponent } from './components/signature-modal/signature-modal.component';
import { PaymentsComponent } from './components/payments/payments.component';
import { MyContractsComponent } from './components/my-contracts/my-contracts.component';
import { ContractAnalyticsComponent } from './components/contract-analytics/contract-analytics.component';
import { AdminBadgesComponent } from './components/admin-badges/admin-badges.component';
import { LeaderboardComponent } from './components/leaderboard/leaderboard.component';
import { MyBadgesComponent } from './components/my-badges/my-badges.component';

/**
 * Initialize Keycloak
 * This function is called before the app starts to initialize Keycloak
 */
function initializeKeycloak(keycloak: KeycloakService) {
  return () => {
    return keycloak.init({
      config: {
        url: 'http://localhost:8180',
        realm: 'projetpidev',
        clientId: 'freelance-client'
      },
      initOptions: {
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
        checkLoginIframe: false
      },
      loadUserProfileAtStartUp: true
    });
  };
}

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    ProfileComponent,
    AdminProfileComponent,
    LoginComponent,
    SignupComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent,
    UsersComponent,
    DashboardComponent,
    ProjectsComponent,
    SettingsComponent,
    PublicProfileComponent,
    BrowseUsersComponent,
    MessagingComponent,
    ComplaintsComponent,
    AdminComplaintsComponent,
    PenaltiesComponent,
    NavbarComponent,
    FooterComponent,
    SidebarComponent,
    HeaderComponent,
    ToastComponent,
    SubscriptionPlansComponent,
    MySubscriptionComponent,
    AdminSubscriptionsComponent,
    SubscriptionButtonComponent,
    ContractsComponent,
    SignatureModalComponent,
    PaymentsComponent,
    MyContractsComponent,
    ContractAnalyticsComponent,
    AdminBadgesComponent,
    LeaderboardComponent,
    MyBadgesComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    KeycloakAngularModule
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
      deps: [KeycloakService]
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    AuthGuard,
    AdminGuard
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
