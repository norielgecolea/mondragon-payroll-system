import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';

export interface LoginResponse {
  token: string;
  username: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'payroll_token';
  private readonly userKey = 'payroll_user';
  readonly username = signal<string | null>(this.readUser());

  constructor(private http: HttpClient, private router: Router) {}

  applySession(token: string, username: string): void {
    localStorage.setItem(this.tokenKey, token);
    localStorage.setItem(this.userKey, username);
    this.username.set(username);
  }

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', { username, password }).pipe(
      tap((res) => this.applySession(res.token, res.username))
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.username.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  private readUser(): string | null {
    return localStorage.getItem(this.userKey);
  }
}
