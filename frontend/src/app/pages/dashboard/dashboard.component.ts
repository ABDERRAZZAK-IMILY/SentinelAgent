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
export class DashboardComponent implements OnInit, OnDestroy {
  private authService = inject(AuthService);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);
  private refreshInterval: ReturnType<typeof setInterval> | null = null;

  currentUser = this.authService.currentUser;
  currentTime = signal(new Date());
  
  stats = signal<SystemStats>({
    totalAlerts: 0,
    activeThreats: 0,
    agentsOnline: 0,
    systemHealth: 0
  });

  recentAlerts = signal<RecentAlert[]>([]);
  isLoading = signal(true);

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.loadDashboardData();
      this.startRefreshTimer();
    }
  }

  ngOnDestroy(): void {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  }

  private startRefreshTimer(): void {
    this.refreshInterval = setInterval(() => {
      this.currentTime.set(new Date());
    }, 1000);
  }

  private loadDashboardData(): void {
    // Simulate loading dashboard data
    setTimeout(() => {
      this.stats.set({
        totalAlerts: Math.floor(Math.random() * 50) + 10,
        activeThreats: Math.floor(Math.random() * 5),
        agentsOnline: Math.floor(Math.random() * 10) + 5,
        systemHealth: Math.floor(Math.random() * 20) + 80
      });

      this.recentAlerts.set([
        {
          id: '1',
          type: 'critical',
          message: 'Unusual network activity detected from endpoint',
          time: '2 min ago',
          source: 'Agent-001'
        },
        {
          id: '2',
          type: 'warning',
          message: 'Multiple failed login attempts detected',
          time: '15 min ago',
          source: 'Auth-Monitor'
        },
        {
          id: '3',
          type: 'info',
          message: 'System scan completed successfully',
          time: '1 hour ago',
          source: 'Scanner'
        },
        {
          id: '4',
          type: 'warning',
          message: 'High CPU usage detected on server node',
          time: '2 hours ago',
          source: 'Agent-003'
        },
        {
          id: '5',
          type: 'info',
          message: 'Security patches applied successfully',
          time: '3 hours ago',
          source: 'System'
        }
      ]);

      this.isLoading.set(false);
    }, 1000);
  }

  logout(): void {
    this.authService.logout();
  }

  refreshData(): void {
    this.isLoading.set(true);
    this.loadDashboardData();
  }

  getAlertIcon(type: string): string {
    switch (type) {
      case 'critical': return 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z';
      case 'warning': return 'M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-8h2v8z';
      default: return 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z';
    }
  }
}
