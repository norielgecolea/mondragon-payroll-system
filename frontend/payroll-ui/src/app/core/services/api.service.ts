import { Injectable, inject, ApplicationRef, NgZone } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly appRef = inject(ApplicationRef);
  private readonly zone = inject(NgZone);

  get<T>(path: string, params?: Record<string, string>): Observable<T> {
    let httpParams = new HttpParams();
    if (params) {
      Object.entries(params).forEach(([k, v]) => {
        if (v != null && v !== '') httpParams = httpParams.set(k, v);
      });
    }
    return this.http.get<T>(`/api${path}`, { params: httpParams });
  }

  /** Download a binary response (e.g. PDF) with auth via interceptor. */
  download(path: string): Observable<Blob> {
    return this.http.get(`/api${path}`, { responseType: 'blob' });
  }

  getList<T>(path: string, params?: Record<string, string>): Observable<T[]> {
    return this.get<T[] | null>(path, params).pipe(map((d) => (Array.isArray(d) ? d : [])));
  }

  post<T>(path: string, body: unknown): Observable<T> {
    return this.http.post<T>(`/api${path}`, body);
  }

  put<T>(path: string, body: unknown): Observable<T> {
    return this.http.put<T>(`/api${path}`, body);
  }

  patch<T>(path: string, body?: unknown): Observable<T> {
    return this.http.patch<T>(`/api${path}`, body ?? {});
  }

  delete<T>(path: string): Observable<T> {
    return this.http.delete<T>(`/api${path}`);
  }

  /** Force a full UI refresh after async work (sidebar nav + HTTP). */
  refreshUi(): void {
    this.zone.run(() => {
      setTimeout(() => this.appRef.tick(), 0);
    });
  }
}
