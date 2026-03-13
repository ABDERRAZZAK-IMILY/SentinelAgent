import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserInfo } from '../models/api.models';

export interface CreateUserRequest {
  username: string;
  password: string;
  role?: string;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/users`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<UserInfo[]> {
    return this.http.get<UserInfo[]>(this.baseUrl);
  }

  create(request: CreateUserRequest): Observable<UserInfo> {
    return this.http.post<UserInfo>(this.baseUrl, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
