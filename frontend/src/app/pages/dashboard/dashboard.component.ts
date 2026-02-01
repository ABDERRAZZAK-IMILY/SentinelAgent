import { Component, inject, signal, OnInit, OnDestroy, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

interface SystemStats {
  totalAlerts: number;
  activeThreats: number;
  agentsOnline: number;
  systemHealth: number;
}

interface RecentAlert {
  id: string;
  type: 'critical' | 'warning' | 'info';
  message: string;
  time: string;
  source: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);
  private refreshInterval: ReturnType<typeof setInterval> | null = null;

  currentUser = this.authService.currentUser;

  logout(): void {
    this.authService.logout();
  }

}
