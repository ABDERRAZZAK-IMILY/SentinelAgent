import { Component, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AsyncPipe } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NavItem } from '../models/navigation.model';
import { AuthService } from '../services/auth.service';
import { Store } from '@ngrx/store';
import { AlertActions } from '../../store/alerts/alerts.actions';
import {
  selectAlertsError,
  selectAlertsLoading,
  selectCriticalAlertsCount,
  selectRecentAlerts,
} from '../../store/alerts/alerts.selectors';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, DatePipe, AsyncPipe],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.css',
})
export class ShellComponent {
  private readonly store = inject(Store);

  protected profileOpen = false;
  protected notificationsOpen = false;
  protected readonly loadingNotifications$ = this.store.select(selectAlertsLoading);
  protected readonly notificationError$ = this.store.select(selectAlertsError);
  protected readonly recentAlerts$ = this.store.select(selectRecentAlerts);
  protected readonly criticalAlertsCount$ = this.store.select(selectCriticalAlertsCount);

  protected readonly navItems: readonly NavItem[] = [
    { label: 'Overview', route: '/overview', icon: 'dashboard' },
    { label: 'Brain-Bot Chat', route: '/chat', icon: 'psychology' },
    { label: 'AI Reports', route: '/reports', icon: 'description' },
    { label: 'System Overview', route: '/system', icon: 'monitoring' },
    { label: 'Sentinel Squad', route: '/squad', icon: 'smart_toy' },
    { label: 'Settings', route: '/settings', icon: 'settings' },
    { label: 'Threat Chronicles', route: '/threats', icon: 'warning' },
    { label: 'Agent Telemetry', route: '/telemetry', icon: 'monitoring' },
  ];

  constructor(private readonly authService: AuthService) {}

  protected toggleNotifications(): void {
    this.notificationsOpen = !this.notificationsOpen;
    this.profileOpen = false;
    if (this.notificationsOpen) {
      this.store.dispatch(AlertActions.loadAlerts({}));
    }
  }

  protected closeNotifications(): void {
    this.notificationsOpen = false;
  }

  protected toggleProfile(): void {
    this.profileOpen = !this.profileOpen;
    this.notificationsOpen = false;
  }

  protected logout(): void {
    this.profileOpen = false;
    this.notificationsOpen = false;
    this.authService.logout();
  }
}
