import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Report, GenerateReportRequest } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/reports`;

  constructor(private readonly http: HttpClient) {}

  getAll(agentId?: string, reportType?: string): Observable<Report[]> {
    let params = new HttpParams();
    if (agentId) params = params.set('agentId', agentId);
    if (reportType) params = params.set('reportType', reportType);
    return this.http.get<Report[]>(this.baseUrl, { params });
  }

  generate(request: GenerateReportRequest): Observable<Report> {
    return this.http.post<Report>(`${this.baseUrl}/generate`, request);
  }

  download(id: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/download/${id}`, { responseType: 'blob' });
  }
}
