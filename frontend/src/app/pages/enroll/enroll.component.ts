import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { IrisLoginComponent } from '../../components/iris-login/iris-login.component';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-enroll',
  standalone: true,
  imports: [CommonModule, RouterLink, IrisLoginComponent],
  templateUrl: './enroll.component.html',
  styleUrl: './enroll.component.css'
})
export class EnrollComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  enrollmentComplete = signal(false);
  setupComplete = signal(false);
  setupError = signal('');
  isSettingUp = signal(false);

  setupAdminAccount(): void {
    this.isSettingUp.set(true);
    this.setupError.set('');
    
    this.authService.setupAdmin().subscribe({
      next: (response) => {
        this.isSettingUp.set(false);
        this.setupComplete.set(true);
      },
      error: (err) => {
        this.isSettingUp.set(false);
        if (err.status === 400) {
          this.setupError.set('Admin account already exists');
          this.setupComplete.set(true);
        } else {
          this.setupError.set('Failed to create admin account');
        }
      }
    });
  }

  onAuthSuccess(event: { token: string; username: string }): void {
    this.enrollmentComplete.set(true);
    this.authService.loginWithIris(event.token, event.username);
    
    setTimeout(() => {
      this.router.navigate(['/dashboard']);
    }, 2000);
  }

  onAuthFailed(error: string): void {
    console.log('Enrollment auth failed:', error);
  }
}
