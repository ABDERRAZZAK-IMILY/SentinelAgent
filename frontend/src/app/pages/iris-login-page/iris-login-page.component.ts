import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { IrisLoginComponent } from '../../components/iris-login/iris-login.component';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-iris-login-page',
  standalone: true,
  imports: [CommonModule, RouterLink, IrisLoginComponent],
  templateUrl: './iris-login-page.component.html',
  styleUrl: './iris-login-page.component.css'
})
export class IrisLoginPageComponent {
  private router = inject(Router);
  private authService = inject(AuthService);

  onAuthSuccess(event: { token: string; username: string }): void {
    console.log('Iris login successful:', event.username);
    this.authService.setAuthData(event.token, { username: event.username, roles: ['ROLE_USER'] });
    this.router.navigate(['/dashboard']);
  }
}
