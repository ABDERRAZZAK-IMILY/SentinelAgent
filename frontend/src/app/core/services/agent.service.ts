import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AgentDetails, AgentStats, AgentCommand } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class AgentService {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/agents`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<AgentDetails[]> {
    return this.http.get<AgentDetails[]>(this.baseUrl);
  }

  getById(id: string): Observable<AgentDetails> {
    return this.http.get<AgentDetails>(`${this.baseUrl}/${id}`);
  }

  getByStatus(status: string): Observable<AgentDetails[]> {
    return this.http.get<AgentDetails[]>(`${this.baseUrl}/status/${status}`);
  }

  getStats(): Observable<AgentStats> {
    return this.http.get<AgentStats>(`${this.baseUrl}/stats`);
  }

  sendCommand(agentId: string, command: string, params: object = {}): Observable<AgentCommand> {
    return this.http.post<AgentCommand>(`${this.baseUrl}/${agentId}/commands`, {
      command,
      parameters: JSON.stringify(params),
    });
  }

  getCommandHistory(agentId: string): Observable<AgentCommand[]> {
    return this.http.get<AgentCommand[]>(`${this.baseUrl}/${agentId}/commands/history`);
  }
}
