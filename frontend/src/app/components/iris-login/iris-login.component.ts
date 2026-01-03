import { Component, OnInit, OnDestroy, ElementRef, ViewChild, inject, PLATFORM_ID, signal, computed, Output, EventEmitter } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { IrisSocketService, EyePosition } from '../../services/iris-socket.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-iris-login',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './iris-login.component.html',
    styleUrl: './iris-login.component.css'
})
export class IrisLoginComponent implements OnInit, OnDestroy {
    @ViewChild('video', { static: true }) videoRef!: ElementRef<HTMLVideoElement>;
    @ViewChild('canvas', { static: true }) canvasRef!: ElementRef<HTMLCanvasElement>;
    @ViewChild('overlayCanvas', { static: true }) overlayCanvasRef!: ElementRef<HTMLCanvasElement>;

    @Output() authenticationSuccess = new EventEmitter<{ token: string; username: string }>();
    @Output() authenticationFailed = new EventEmitter<string>();

    private platformId = inject(PLATFORM_ID);
    private irisSocket = inject(IrisSocketService);
    private subscriptions = new Subscription();
    private detectionInterval: ReturnType<typeof setInterval> | null = null;
    private ctx: CanvasRenderingContext2D | null = null;
    private overlayCtx: CanvasRenderingContext2D | null = null;

    // State signals
    isScanning = signal(false);
    isEnrolled = signal(false);
    eyeDetected = signal(false);
    isConnected = signal(false);
    cameraReady = signal(false);
    eyePreviewSrc = signal('data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7');
    similarityScore = signal<number | null>(null);
    showSimilarity = signal(false);
    username = signal('admin');

    statusMessage = signal({ type: 'info', text: 'Initializing camera...' });

    // Computed values
    buttonsDisabled = computed(() => !this.cameraReady() || this.isScanning() || !this.username().trim());
    detectionStatusText = computed(() => this.eyeDetected() ? 'Eye Detected' : 'Searching...');

    ngOnInit(): void {
        if (isPlatformBrowser(this.platformId)) {
            this.initializeSocket();
            this.initCamera();
        }
    }

    ngOnDestroy(): void {
        this.stopEyeDetection();
        this.subscriptions.unsubscribe();
        this.irisSocket.disconnect();

        // Stop camera stream
        const video = this.videoRef?.nativeElement;
        if (video?.srcObject) {
            const stream = video.srcObject as MediaStream;
            stream.getTracks().forEach(track => track.stop());
        }
    }

    private async initializeSocket(): Promise<void> {
        await this.irisSocket.connect();

        this.subscriptions.add(
            this.irisSocket.connected$.subscribe(connected => {
                this.isConnected.set(connected);
                if (connected) {
                    this.startEyeDetection();
                } else {
                    this.stopEyeDetection();
                }
            })
        );

        this.subscriptions.add(
            this.irisSocket.eyePosition$.subscribe(data => this.handleEyePosition(data))
        );

        this.subscriptions.add(
            this.irisSocket.enrollmentStatus$.subscribe(data => {
                this.isEnrolled.set(data.enrolled);
            })
        );

        this.subscriptions.add(
            this.irisSocket.enrollResult$.subscribe(data => {
                this.isScanning.set(false);
                if (data.success) {
                    this.showStatus('success', '✓ ' + data.message);
                    this.isEnrolled.set(true);
                } else {
                    this.showStatus('error', '✗ ' + data.error);
                }
            })
        );

        this.subscriptions.add(
            this.irisSocket.irisResult$.subscribe(data => {
                this.isScanning.set(false);
                if (data.error) {
                    this.showStatus('error', data.error);
                    this.authenticationFailed.emit(data.error);
                } else if (data.authenticated) {
                    this.similarityScore.set((data.similarity ?? 0) * 100);
                    this.showSimilarity.set(true);
                    
                    if (data.token) {
                        // Store the JWT token
                        this.irisSocket.storeToken(data.token);
                        this.showStatus('success', '✓ Authentication successful! Welcome ' + (data.username || this.username()));
                        
                        // Emit success event with token and username
                        this.authenticationSuccess.emit({
                            token: data.token,
                            username: data.username || this.username()
                        });
                    } else {
                        this.showStatus('error', '✗ Iris verified but no token received');
                        this.authenticationFailed.emit('No token received from server');
                    }
                } else {
                    this.showStatus('error', '✗ Authentication failed - Iris mismatch');
                    this.similarityScore.set((data.similarity ?? 0) * 100);
                    this.showSimilarity.set(true);
                    this.authenticationFailed.emit('Iris mismatch');
                }
            })
        );
    }

    private async initCamera(): Promise<void> {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({
                video: {
                    width: { ideal: 640 },
                    height: { ideal: 480 },
                    facingMode: 'user'
                }
            });

            const video = this.videoRef.nativeElement;
            video.srcObject = stream;

            video.onloadedmetadata = () => {
                const canvas = this.canvasRef.nativeElement;
                const overlayCanvas = this.overlayCanvasRef.nativeElement;

                canvas.width = video.videoWidth;
                canvas.height = video.videoHeight;
                overlayCanvas.width = video.videoWidth;
                overlayCanvas.height = video.videoHeight;

                this.ctx = canvas.getContext('2d');
                this.overlayCtx = overlayCanvas.getContext('2d');

                this.cameraReady.set(true);
                this.showStatus('info', 'Camera ready. Position your eye and click Enroll or Authenticate.');
            };
        } catch (err) {
            console.error('Camera error:', err);
            this.showStatus('error', 'Camera access denied. Please grant permissions.');
        }
    }

    private startEyeDetection(): void {
        if (this.detectionInterval) return;

        this.detectionInterval = setInterval(() => {
            if (!this.isScanning() && this.irisSocket.isConnected()) {
                this.captureAndDetect();
            }
        }, 200);
    }

    private stopEyeDetection(): void {
        if (this.detectionInterval) {
            clearInterval(this.detectionInterval);
            this.detectionInterval = null;
        }
    }

    private captureAndDetect(): void {
        if (!this.ctx) return;

        const video = this.videoRef.nativeElement;
        const canvas = this.canvasRef.nativeElement;

        this.ctx.drawImage(video, 0, 0);
        canvas.toBlob((blob) => {
            if (!blob) return;
            const reader = new FileReader();
            reader.onload = () => {
                const base64Data = (reader.result as string).split(',')[1];
                this.irisSocket.detectEye(base64Data);
            };
            reader.readAsDataURL(blob);
        }, 'image/jpeg', 0.7);
    }

    private handleEyePosition(data: EyePosition): void {
        if (data.detected) {
            this.eyeDetected.set(true);
            if (data.eyeImage) {
                this.eyePreviewSrc.set('data:image/jpeg;base64,' + data.eyeImage);
            }
            if (data.face && data.eye) {
                this.drawDetectionOverlay(data.face, data.eye);
            }
        } else {
            this.eyeDetected.set(false);
            this.clearOverlay();
        }
    }

    private drawDetectionOverlay(
        face: { x: number; y: number; width: number; height: number },
        eye: { x: number; y: number; width: number; height: number }
    ): void {
        if (!this.overlayCtx) return;

        const video = this.videoRef.nativeElement;
        const overlayCanvas = this.overlayCanvasRef.nativeElement;
        const scaleX = overlayCanvas.width / video.videoWidth;
        const scaleY = overlayCanvas.height / video.videoHeight;

        this.overlayCtx.clearRect(0, 0, overlayCanvas.width, overlayCanvas.height);

        // Draw face rectangle
        this.overlayCtx.strokeStyle = 'rgba(102, 126, 234, 0.6)';
        this.overlayCtx.lineWidth = 2;
        this.overlayCtx.strokeRect(
            face.x * scaleX,
            face.y * scaleY,
            face.width * scaleX,
            face.height * scaleY
        );

        // Draw eye rectangle with glow
        this.overlayCtx.strokeStyle = '#4ade80';
        this.overlayCtx.lineWidth = 3;
        this.overlayCtx.shadowColor = '#4ade80';
        this.overlayCtx.shadowBlur = 10;
        this.overlayCtx.strokeRect(
            eye.x * scaleX,
            eye.y * scaleY,
            eye.width * scaleX,
            eye.height * scaleY
        );
        this.overlayCtx.shadowBlur = 0;

        // Draw crosshair on eye center
        const eyeCenterX = (eye.x + eye.width / 2) * scaleX;
        const eyeCenterY = (eye.y + eye.height / 2) * scaleY;
        const crossSize = 15;

        this.overlayCtx.beginPath();
        this.overlayCtx.moveTo(eyeCenterX - crossSize, eyeCenterY);
        this.overlayCtx.lineTo(eyeCenterX + crossSize, eyeCenterY);
        this.overlayCtx.moveTo(eyeCenterX, eyeCenterY - crossSize);
        this.overlayCtx.lineTo(eyeCenterX, eyeCenterY + crossSize);
        this.overlayCtx.stroke();
    }

    private clearOverlay(): void {
        if (this.overlayCtx) {
            const overlayCanvas = this.overlayCanvasRef.nativeElement;
            this.overlayCtx.clearRect(0, 0, overlayCanvas.width, overlayCanvas.height);
        }
    }

    private showStatus(type: 'info' | 'success' | 'error' | 'scanning', text: string): void {
        this.statusMessage.set({ type, text });
    }

    authenticate(): void {
        if (this.isScanning() || !this.eyeDetected()) {
            if (!this.eyeDetected()) {
                this.showStatus('error', 'No eye detected. Please position your eye correctly.');
            }
            return;
        }

        if (!this.username().trim()) {
            this.showStatus('error', 'Please enter a username.');
            return;
        }

        // Set username in service before authentication
        this.irisSocket.setUsername(this.username());

        this.isScanning.set(true);
        this.showSimilarity.set(false);
        this.showStatus('scanning', 'Analyzing iris pattern...');

        if (!this.ctx) return;

        const video = this.videoRef.nativeElement;
        const canvas = this.canvasRef.nativeElement;

        this.ctx.drawImage(video, 0, 0);
        canvas.toBlob((blob) => {
            if (!blob) return;
            const reader = new FileReader();
            reader.onload = () => {
                const base64Data = (reader.result as string).split(',')[1];
                this.irisSocket.authenticate(base64Data);
            };
            reader.readAsDataURL(blob);
        }, 'image/jpeg', 0.9);
    }

    enroll(): void {
        if (this.isScanning() || !this.eyeDetected()) {
            if (!this.eyeDetected()) {
                this.showStatus('error', 'No eye detected. Please position your eye correctly.');
            }
            return;
        }

        this.isScanning.set(true);
        this.showSimilarity.set(false);
        this.showStatus('scanning', 'Capturing iris pattern...');

        if (!this.ctx) return;

        const video = this.videoRef.nativeElement;
        const canvas = this.canvasRef.nativeElement;

        this.ctx.drawImage(video, 0, 0);
        canvas.toBlob((blob) => {
            if (!blob) return;
            const reader = new FileReader();
            reader.onload = () => {
                const base64Data = (reader.result as string).split(',')[1];
                this.irisSocket.enroll(base64Data);
            };
            reader.readAsDataURL(blob);
        }, 'image/jpeg', 0.9);
    }

    onUsernameChange(event: Event): void {
        const input = event.target as HTMLInputElement;
        this.username.set(input.value);
        this.irisSocket.setUsername(input.value);
    }
}
