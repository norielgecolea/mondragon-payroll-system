import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, tap, catchError, of, finalize } from 'rxjs';
import { ApiService } from './api.service';

/**
 * Holds list data in a BehaviorSubject so templates can use the async pipe.
 * Async pipe always marks the view dirty when data arrives — including after
 * sidebar navigation where a plain assignment/signal set may not re-render.
 */
@Injectable()
export class ListLoader {
  private readonly api = inject(ApiService);

  readonly items$ = new BehaviorSubject<any[]>([]);
  readonly loading$ = new BehaviorSubject<boolean>(false);
  readonly error$ = new BehaviorSubject<string>('');

  load(path: string): void {
    this.loading$.next(true);
    this.error$.next('');
    this.api
      .getList<any>(path)
      .pipe(
        tap((rows) => this.items$.next(rows)),
        catchError((err) => {
          this.items$.next([]);
          this.error$.next(err?.error?.message || 'Failed to load data');
          return of([]);
        }),
        finalize(() => {
          this.loading$.next(false);
          this.api.refreshUi();
        })
      )
      .subscribe();
  }

  /** For dropdowns / secondary lists. */
  fetch(path: string): Observable<any[]> {
    return this.api.getList<any>(path).pipe(
      catchError(() => of([])),
      tap(() => this.api.refreshUi())
    );
  }
}
