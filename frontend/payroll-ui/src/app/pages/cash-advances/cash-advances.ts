import { Component, OnInit, inject } from '@angular/core';
import { AsyncPipe, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { ListLoader } from '../../core/services/list-loader';

@Component({
  selector: 'app-cash-advances',
  imports: [FormsModule, AsyncPipe, CurrencyPipe],
  providers: [ListLoader],
  templateUrl: './cash-advances.html'
})
export class CashAdvancesComponent implements OnInit {
  readonly list = inject(ListLoader);
  private readonly api = inject(ApiService);
  employees: any[] = [];
  showModal = false;
  form: any = {
    employeeId: null,
    amount: 0,
    advanceDate: new Date().toISOString().slice(0, 10),
    remarks: ''
  };

  ngOnInit(): void {
    this.list.load('/cash-advances');
    this.list.fetch('/employees/active').subscribe((d) => {
      this.employees = d;
      this.api.refreshUi();
    });
  }

  openCreate(): void {
    this.form = {
      employeeId: this.employees[0]?.id ?? null,
      amount: 0,
      advanceDate: new Date().toISOString().slice(0, 10),
      remarks: ''
    };
    this.showModal = true;
  }

  save(): void {
    this.api.post('/cash-advances', this.form).subscribe({
      next: () => {
        this.showModal = false;
        this.list.load('/cash-advances');
      },
      error: (err) => alert(err?.error?.message || 'Save failed')
    });
  }

  cancel(item: any): void {
    if (!confirm('Cancel this cash advance?')) return;
    this.api.patch(`/cash-advances/${item.id}/cancel`).subscribe({
      next: () => this.list.load('/cash-advances'),
      error: (err) => alert(err?.error?.message || 'Cancel failed')
    });
  }
}
