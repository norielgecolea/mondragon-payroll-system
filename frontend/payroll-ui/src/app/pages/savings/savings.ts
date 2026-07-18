import { Component, OnInit, inject } from '@angular/core';
import { AsyncPipe, CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { ListLoader } from '../../core/services/list-loader';
import { BehaviorSubject } from 'rxjs';

@Component({
  selector: 'app-savings',
  imports: [FormsModule, AsyncPipe, CurrencyPipe, DatePipe],
  providers: [ListLoader],
  templateUrl: './savings.html'
})
export class SavingsComponent implements OnInit {
  readonly list = inject(ListLoader);
  private readonly api = inject(ApiService);
  readonly selected$ = new BehaviorSubject<any | null>(null);
  showModal = false;
  mode: 'deposit' | 'withdraw' = 'deposit';
  amount = 0;
  remarks = '';

  ngOnInit(): void {
    this.list.load('/savings');
  }

  open(account: any): void {
    this.api.get<any>(`/savings/employee/${account.employeeId}`).subscribe({
      next: (d) => {
        this.selected$.next(d);
        this.api.refreshUi();
      },
      error: (err) => alert(err?.error?.message || 'Failed to load account')
    });
  }

  openTx(mode: 'deposit' | 'withdraw'): void {
    this.mode = mode;
    this.amount = 0;
    this.remarks = '';
    this.showModal = true;
  }

  saveTx(): void {
    const current = this.selected$.value;
    if (!current) return;
    const path = `/savings/employee/${current.employeeId}/${this.mode}`;
    this.api.post(path, { amount: this.amount, remarks: this.remarks }).subscribe({
      next: () => {
        this.showModal = false;
        this.open(current);
        this.list.load('/savings');
      },
      error: (err) => alert(err?.error?.message || 'Transaction failed')
    });
  }
}
