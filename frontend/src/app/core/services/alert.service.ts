import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Alert, AlertStats } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class AlertService {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/alerts`;

  constructor(private readonly http: HttpClient) {}

  getAll(status?: string, severity?: string): Observable<Alert[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    if (severity) params = params.set('severity', severity);
    return this.http.get<Alert[]>(this.baseUrl, { params });
  }

  updateStatus(id: string, status: string): Observable<Alert> {
    return this.http.put<Alert>(`${this.baseUrl}/${id}/status`, null, {
      params: { status },
    });
  }

  getStats(): Observable<AlertStats> {
    return this.http.get<AlertStats>(`${this.baseUrl}/stats`);
  }
}
