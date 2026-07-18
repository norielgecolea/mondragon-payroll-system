import { Component, OnInit, inject } from '@angular/core';
import { AsyncPipe, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { ListLoader } from '../../core/services/list-loader';

@Component({
  selector: 'app-salary-rates',
  imports: [FormsModule, AsyncPipe, CurrencyPipe],
  providers: [ListLoader],
  templateUrl: './salary-rates.html'
})
export class SalaryRatesComponent implements OnInit {
  readonly list = inject(ListLoader);
  private readonly api = inject(ApiService);
  showModal = false;
  editing: any = null;
  form: any = { name: '', dailyRate: 0, hourlyRate: 0, overtimeRate: 0, description: '', active: true };

  ngOnInit(): void {
    this.list.load('/salary-rates');
  }

  openCreate(): void {
    this.editing = null;
    this.form = { name: '', dailyRate: 0, hourlyRate: 0, overtimeRate: 0, description: '', active: true };
    this.showModal = true;
  }

  openEdit(item: any): void {
    this.editing = item;
    this.form = { ...item };
    this.showModal = true;
  }

  save(): void {
    const req = this.editing
      ? this.api.put(`/salary-rates/${this.editing.id}`, this.form)
      : this.api.post('/salary-rates', this.form);
    req.subscribe({
      next: () => {
        this.showModal = false;
        this.list.load('/salary-rates');
      },
      error: (err) => alert(err?.error?.message || 'Save failed')
    });
  }

  remove(item: any): void {
    if (!confirm(`Deactivate salary rate "${item.name}"?`)) return;
    this.api.patch(`/salary-rates/${item.id}/deactivate`).subscribe({
      next: () => this.list.load('/salary-rates'),
      error: (err) => alert(err?.error?.message || 'Deactivate failed')
    });
  }

  activate(item: any): void {
    this.api.patch(`/salary-rates/${item.id}/activate`).subscribe({
      next: () => this.list.load('/salary-rates'),
      error: (err) => alert(err?.error?.message || 'Activate failed')
    });
  }
}
