import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './guards/auth.guard';

export const routes: Routes = [
    { 
        path: 'login', 
        loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent),
        canActivate: [guestGuard]
    },
    { 
        path: 'enroll', 
        loadComponent: () => import('./pages/enroll/enroll.component').then(m => m.EnrollComponent),
        canActivate: [guestGuard]
    },
    { 
        path: 'iris-login', 
        loadComponent: () => import('./pages/iris-login-page/iris-login-page.component').then(m => m.IrisLoginPageComponent),
        canActivate: [guestGuard]
    },
    { 
        path: 'dashboard', 
        loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent),
        canActivate: [authGuard]
    },
    { path: '', redirectTo: '/login', pathMatch: 'full' },
    { path: '**', redirectTo: '/login' }
];
