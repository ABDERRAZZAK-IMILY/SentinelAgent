import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserInfo } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/users`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<UserInfo[]> {
    return this.http.get<UserInfo[]>(this.baseUrl);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
