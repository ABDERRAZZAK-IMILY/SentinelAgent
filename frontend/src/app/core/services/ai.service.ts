import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AiStatus, ChatRequest, ChatResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class AiService {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/ai`;

  constructor(private readonly http: HttpClient) {}

  getStatus(): Observable<AiStatus> {
    return this.http.get<AiStatus>(`${this.baseUrl}/status`);
  }

  train(): Observable<{ message: string; jobId: string }> {
    return this.http.post<{ message: string; jobId: string }>(`${this.baseUrl}/train`, {});
  }

  chat(message: string, agentId: string | null = null): Observable<ChatResponse> {
    const payload: ChatRequest = { message, agentId };
    return this.http.post<ChatResponse>(`${this.baseUrl}/chat`, payload);
  }
}
