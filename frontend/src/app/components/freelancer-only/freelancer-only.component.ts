import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-freelancer-only',
  templateUrl: './freelancer-only.component.html',
  styleUrls: ['./freelancer-only.component.css']
})
export class FreelancerOnlyComponent {

  constructor(private router: Router) {}

  goToHome() {
    this.router.navigate(['/']);
  }

  goToProfile() {
    this.router.navigate(['/profile']);
  }
}
