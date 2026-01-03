import { Injectable, PLATFORM_ID, inject, signal, computed } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of } from 'rxjs';

export interface User {
  username: string;
  roles: string[];
}

export interface LoginResponse {
  token: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private platformId = inject(PLATFORM_ID);
  private http = inject(HttpClient);
  private router = inject(Router);
  
  private readonly API_URL = 'http://localhost:8080/api/auth';
  private readonly TOKEN_KEY = 'jwt_token';
  private readonly USER_KEY = 'user_data';

  // State
  private _isAuthenticated = signal(false);
  private _currentUser = signal<User | null>(null);
  private _isLoading = signal(false);

  // Public computed values
  isAuthenticated = computed(() => this._isAuthenticated());
  currentUser = computed(() => this._currentUser());
  isLoading = computed(() => this._isLoading());

  constructor() {
    this.checkStoredAuth();
  }

  private checkStoredAuth(): void {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem(this.TOKEN_KEY);
      const userData = localStorage.getItem(this.USER_KEY);
      
      if (token && userData) {
        try {
          const user = JSON.parse(userData);
          this._currentUser.set(user);
          this._isAuthenticated.set(true);
        } catch {
          this.clearAuth();
        }
      }
    }
  }

  login(username: string, password: string): Observable<LoginResponse | null> {
    this._isLoading.set(true);
    
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, { username, password })
      .pipe(
        tap(response => {
          this.setAuth(response.token, username);
          this._isLoading.set(false);
        }),
        catchError(error => {
          this._isLoading.set(false);
          console.error('Login failed:', error);
          return of(null);
        })
      );
  }

  loginWithIris(token: string, username: string): void {
    this.setAuth(token, username);
  }

  // Called by iris login page when iris auth succeeds
  setAuthData(token: string, user: User | { username: string }): void {
    if (isPlatformBrowser(this.platformId)) {
      const userData: User = {
        username: user.username,
        roles: 'roles' in user ? user.roles : ['ROLE_USER']
      };
      
      localStorage.setItem(this.TOKEN_KEY, token);
      localStorage.setItem(this.USER_KEY, JSON.stringify(userData));
      
      this._currentUser.set(userData);
      this._isAuthenticated.set(true);
    }
  }

  setupAdmin(): Observable<any> {
    return this.http.post(`${this.API_URL}/setup`, {});
  }

  private setAuth(token: string, username: string): void {
    if (isPlatformBrowser(this.platformId)) {
      const user: User = { username, roles: ['ROLE_USER'] };
      
      localStorage.setItem(this.TOKEN_KEY, token);
      localStorage.setItem(this.USER_KEY, JSON.stringify(user));
      
      this._currentUser.set(user);
      this._isAuthenticated.set(true);
    }
  }

  logout(): void {
    this.clearAuth();
    this.router.navigate(['/login']);
  }

  private clearAuth(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.USER_KEY);
    }
    this._currentUser.set(null);
    this._isAuthenticated.set(false);
  }

  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem(this.TOKEN_KEY);
    }
    return null;
  }

  getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }
}
