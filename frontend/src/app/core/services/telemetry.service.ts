import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MetricReport } from '../models/api.models';

export interface MetricReportDetail extends MetricReport {
  processes: ProcessInfo[];
  networkConnections: NetworkConnectionInfo[];
}

export interface ProcessInfo {
  pid: number;
  name: string;
  cpuUsage: number;
  username: string;
}

export interface NetworkConnectionInfo {
  pid: number;
  localAddress: string;
  localPort: number;
  remoteAddress: string;
  remotePort: number;
  status: string;
  processName: string;
}

@Injectable({ providedIn: 'root' })
export class TelemetryService {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/telemetry`;

  constructor(private readonly http: HttpClient) {}

  getHistory(agentId: string, hoursBack: number = 1): Observable<MetricReportDetail[]> {
    const params = new HttpParams().set('hoursBack', hoursBack.toString());
    return this.http.get<MetricReportDetail[]>(`${this.baseUrl}/agents/${agentId}/history`, { params });
  }

  getLatest(agentId: string): Observable<MetricReportDetail> {
    return this.http.get<MetricReportDetail>(`${this.baseUrl}/agents/${agentId}/latest`);
  }
}
