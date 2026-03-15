import { Routes } from '@angular/router';
import { ShellComponent } from './core/layout/shell.component';
import { authGuard } from './core/guards/auth.guard';

export const appRoutes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/login-page.component').then((m) => m.LoginPageComponent),
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'overview',
      },
      {
        path: 'overview',
        loadComponent: () => import('./features/overview/overview-page.component').then((m) => m.OverviewPageComponent),
      },
      {
        path: 'chat',
        loadComponent: () => import('./features/chat/chat-page.component').then((m) => m.ChatPageComponent),
      },
      {
        path: 'reports',
        loadComponent: () => import('./features/reports/reports-page.component').then((m) => m.ReportsPageComponent),
      },
      {
        path: 'system',
        loadComponent: () => import('./features/system/system-page.component').then((m) => m.SystemPageComponent),
      },
      {
        path: 'squad',
        loadComponent: () => import('./features/squad/squad-page.component').then((m) => m.SquadPageComponent),
      },
      {
        path: 'settings',
        loadComponent: () => import('./features/settings/settings-page.component').then((m) => m.SettingsPageComponent),
      },
      {
        path: 'threats',
        loadComponent: () => import('./features/threats/threats-page.component').then((m) => m.ThreatsPageComponent),
      },
      {
        path: 'telemetry',
        loadComponent: () => import('./features/telemetry/telemetry-page.component').then((m) => m.TelemetryPageComponent),
      },
    ],
  },
  {
    path: '**',
    redirectTo: 'overview',
  },
];
