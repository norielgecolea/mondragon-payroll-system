import { Component, OnInit, inject } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { ListLoader } from '../../core/services/list-loader';

@Component({
  selector: 'app-overtime',
  imports: [FormsModule, AsyncPipe],
  providers: [ListLoader],
  templateUrl: './overtime.html'
})
export class OvertimeComponent implements OnInit {
  readonly list = inject(ListLoader);
  private readonly api = inject(ApiService);
  employees: any[] = [];
  showModal = false;
  editing: any = null;
  form: any = {
    employeeId: null,
    otDate: new Date().toISOString().slice(0, 10),
    hours: 1,
    reason: '',
    approved: false
  };

  ngOnInit(): void {
    this.list.load('/overtime');
    this.list.fetch('/employees/active').subscribe((d) => {
      this.employees = d;
      this.api.refreshUi();
    });
  }

  openCreate(): void {
    this.editing = null;
    this.form = {
      employeeId: this.employees[0]?.id ?? null,
      otDate: new Date().toISOString().slice(0, 10),
      hours: 1,
      reason: '',
      approved: false
    };
    this.showModal = true;
  }

  openEdit(item: any): void {
    this.editing = item;
    this.form = { ...item };
    this.showModal = true;
  }

  save(): void {
    const req = this.editing
      ? this.api.put(`/overtime/${this.editing.id}`, this.form)
      : this.api.post('/overtime', this.form);
    req.subscribe({
      next: () => {
        this.showModal = false;
        this.list.load('/overtime');
      },
      error: (err) => alert(err?.error?.message || 'Save failed')
    });
  }

  approve(item: any): void {
    this.api.patch(`/overtime/${item.id}/approve`).subscribe({
      next: () => this.list.load('/overtime'),
      error: (err) => alert(err?.error?.message || 'Approve failed')
    });
  }

  remove(item: any): void {
    if (!confirm('Delete this overtime record?')) return;
    this.api.delete(`/overtime/${item.id}`).subscribe({
      next: () => this.list.load('/overtime'),
      error: (err) => alert(err?.error?.message || 'Delete failed')
    });
  }
}
