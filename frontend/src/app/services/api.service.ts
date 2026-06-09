import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.services';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }

  async get<T>(endpoint: string): Promise<T> {
    return firstValueFrom(
      this.http.get<T>(`${this.baseUrl}${endpoint}`, { headers: this.getHeaders() })
    );
  }

  async post<T>(endpoint: string, data: any): Promise<T> {
    return firstValueFrom(
      this.http.post<T>(`${this.baseUrl}${endpoint}`, data, { headers: this.getHeaders() })
    );
  }

  async put<T>(endpoint: string, data: any): Promise<T> {
    return firstValueFrom(
      this.http.put<T>(`${this.baseUrl}${endpoint}`, data, { headers: this.getHeaders() })
    );
  }

   // ✅ AJOUTEZ CETTE MÉTHODE PATCH
  async patch<T>(endpoint: string, data: any): Promise<T> {
    return firstValueFrom(
      this.http.patch<T>(`${this.baseUrl}${endpoint}`, data, { headers: this.getHeaders() })
    );
  }

  async delete<T>(endpoint: string): Promise<T> {
    return firstValueFrom(
      this.http.delete<T>(`${this.baseUrl}${endpoint}`, { headers: this.getHeaders() })
    );
  }
}