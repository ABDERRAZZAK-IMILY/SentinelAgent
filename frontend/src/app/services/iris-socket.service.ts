import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Observable, Subject, BehaviorSubject } from 'rxjs';

export interface EyePosition {
  detected: boolean;
  eyeImage?: string;
  face?: { x: number; y: number; width: number; height: number };
  eye?: { x: number; y: number; width: number; height: number };
}

export interface EnrollmentStatus {
  enrolled: boolean;
}

export interface EnrollResult {
  success: boolean;
  message?: string;
  error?: string;
}

export interface IrisResult {
  authenticated?: boolean;
  similarity?: number;
  error?: string;
  token?: string;
  username?: string;
}

@Injectable({
  providedIn: 'root'
})
export class IrisSocketService {
  private socket: any = null;
  private platformId = inject(PLATFORM_ID);
  private username: string = 'admin';

  private eyePositionSubject = new Subject<EyePosition>();
  private enrollmentStatusSubject = new BehaviorSubject<EnrollmentStatus>({ enrolled: false });
  private enrollResultSubject = new Subject<EnrollResult>();
  private irisResultSubject = new Subject<IrisResult>();
  private connectedSubject = new BehaviorSubject<boolean>(false);

  eyePosition$ = this.eyePositionSubject.asObservable();
  enrollmentStatus$ = this.enrollmentStatusSubject.asObservable();
  enrollResult$ = this.enrollResultSubject.asObservable();
  irisResult$ = this.irisResultSubject.asObservable();
  connected$ = this.connectedSubject.asObservable();

  setUsername(username: string): void {
    this.username = username;
  }

  getUsername(): string {
    return this.username;
  }

  /**
   * Store JWT token in localStorage for subsequent API calls
   */
  storeToken(token: string): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('jwt_token', token);
    }
  }

  /**
   * Get stored JWT token
   */
  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('jwt_token');
    }
    return null;
  }

  /**
   * Remove stored JWT token (logout)
   */
  clearToken(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('jwt_token');
    }
  }

  /**
   * Check if user is authenticated (has valid token)
   */
  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  async connect(serverUrl: string = 'http://localhost:5000'): Promise<void> {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    // Dynamically import socket.io-client only in browser
    const { io } = await import('socket.io-client');
    
    this.socket = io(serverUrl);

    this.socket.on('connect', () => {
      console.log('Connected to iris server');
      this.connectedSubject.next(true);
      this.checkEnrollment();
    });

    this.socket.on('disconnect', () => {
      console.log('Disconnected from iris server');
      this.connectedSubject.next(false);
    });

    this.socket.on('eye_position', (data: EyePosition) => {
      this.eyePositionSubject.next(data);
    });

    this.socket.on('enrollment_status', (data: EnrollmentStatus) => {
      this.enrollmentStatusSubject.next(data);
    });

    this.socket.on('enroll_result', (data: EnrollResult) => {
      this.enrollResultSubject.next(data);
    });

    this.socket.on('iris_result', (data: IrisResult) => {
      this.irisResultSubject.next(data);
    });

    this.socket.on('connect_error', () => {
      console.error('Failed to connect to iris server');
      this.connectedSubject.next(false);
    });
  }

  disconnect(): void {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
    }
  }

  checkEnrollment(): void {
    if (this.socket?.connected) {
      this.socket.emit('check_enrollment');
    }
  }

  detectEye(imageData: string): void {
    if (this.socket?.connected) {
      this.socket.emit('detect_eye', imageData);
    }
  }

  authenticate(imageData: string): void {
    if (this.socket?.connected) {
      // Send both image and username for backend authentication
      this.socket.emit('iris_frame', {
        image: imageData,
        username: this.username
      });
    }
  }

  enroll(imageData: string): void {
    if (this.socket?.connected) {
      this.socket.emit('iris_enroll', imageData);
    }
  }

  isConnected(): boolean {
    return this.socket?.connected ?? false;
  }
}
