import { Component, OnInit, inject } from '@angular/core';
import { AsyncPipe, CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ApiService } from '../../core/services/api.service';

interface Dashboard {
  totalEmployees: number;
  activeEmployees: number;
  totalPositions: number;
  pendingOvertime: number;
  activeCashAdvances: number;
  totalCashAdvanceBalance: number;
  totalSavingsBalance: number;
  draftPayrolls: number;
  finalizedPayrolls: number;
  archivedPayrolls: number;
  latestPayrollNet: number;
  latestPayrollNumber: string | null;
}

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, AsyncPipe, CurrencyPipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class DashboardComponent implements OnInit {
  private readonly api = inject(ApiService);
  readonly data$ = new BehaviorSubject<Dashboard | null>(null);
  readonly error$ = new BehaviorSubject<string>('');

  ngOnInit(): void {
    this.api.get<Dashboard>('/dashboard').subscribe({
      next: (d) => {
        this.data$.next(d);
        this.api.refreshUi();
      },
      error: (err) => {
        this.error$.next(err?.error?.message || 'Failed to load dashboard');
        this.api.refreshUi();
      }
    });
  }
}
