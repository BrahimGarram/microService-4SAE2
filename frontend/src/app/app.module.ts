import { NgModule, APP_INITIALIZER } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClientModule, HttpClient, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { environment } from '../environments/environment';

export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}
import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';
import { FreelancerGuard } from './guards/freelancer.guard';
import { TestPassDeactivateGuard } from './guards/test-pass-deactivate.guard';
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
import { ProjectDashboardComponent } from './components/project-dashboard/project-dashboard.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { FooterComponent } from './components/footer/footer.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { HeaderComponent } from './components/header/header.component';
import { ToastComponent } from './components/toast/toast.component';
import { ChatboxComponent } from './components/chatbox/chatbox.component';
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
import { CollaborationComponent } from './components/collaboration/collaboration.component';
import { AdminCollaborationsComponent } from './components/admin-collaborations/admin-collaborations.component';

/**
 * Initialize Keycloak
 * This function is called before the app starts to initialize Keycloak.
 * If Keycloak is unreachable, the app still loads so the front page is visible.
 */
function initializeKeycloak(keycloak: KeycloakService) {
  return () => {
    return keycloak.init({
      config: {
        url: environment.keycloakUrl,
        realm: environment.keycloakRealm,
        clientId: environment.keycloakClientId
      },
      initOptions: {
        onLoad: 'check-sso',
        checkLoginIframe: false
      },
      loadUserProfileAtStartUp: false
    }).catch((err) => {
      console.warn('Keycloak init failed (app will still load):', err?.message || err);
      return Promise.resolve();
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
    ProjectDashboardComponent,
    NavbarComponent,
    FooterComponent,
    SidebarComponent,
    HeaderComponent,
    ToastComponent,
    ChatboxComponent,
    FeedComponent,
    FeedAbonnementsComponent,
    TestComponent,
    AddQuestionComponent,
    TestIntroComponent,
    TestPassComponent,
    AffectationComponent,
    FreelancerOnlyComponent,
    ComplaintsComponent,
    AdminComplaintsComponent,
    PenaltiesComponent,
    CollaborationComponent,
    AdminCollaborationsComponent
  ],
  imports: [
    BrowserModule,
    CommonModule,
    RouterModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    DragDropModule,
    ReactiveFormsModule,
    TranslateModule.forRoot({
      defaultLanguage: 'en',
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    }),
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
    AdminGuard,
    FreelancerGuard,
    TestPassDeactivateGuard
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
