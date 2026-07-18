import { Component, OnInit, inject } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { ListLoader } from '../../core/services/list-loader';

@Component({
  selector: 'app-dtr',
  imports: [FormsModule, AsyncPipe],
  providers: [ListLoader],
  templateUrl: './dtr.html'
})
export class DtrComponent implements OnInit {
  readonly list = inject(ListLoader);
  private readonly api = inject(ApiService);
  employees: any[] = [];
  showModal = false;
  editing: any = null;
  form: any = {
    employeeId: null,
    workDate: new Date().toISOString().slice(0, 10),
    timeIn: '08:00',
    timeOut: '17:00',
    remarks: ''
  };

  ngOnInit(): void {
    this.list.load('/dtr');
    this.list.fetch('/employees/active').subscribe((d) => {
      this.employees = d;
      this.api.refreshUi();
    });
  }

  openCreate(): void {
    this.editing = null;
    this.form = {
      employeeId: this.employees[0]?.id ?? null,
      workDate: new Date().toISOString().slice(0, 10),
      timeIn: '08:00',
      timeOut: '17:00',
      remarks: ''
    };
    this.showModal = true;
  }

  openEdit(item: any): void {
    this.editing = item;
    this.form = {
      ...item,
      timeIn: String(item.timeIn).slice(0, 5),
      timeOut: String(item.timeOut).slice(0, 5)
    };
    this.showModal = true;
  }

  save(): void {
    const payload = {
      ...this.form,
      timeIn: this.form.timeIn.length === 5 ? this.form.timeIn + ':00' : this.form.timeIn,
      timeOut: this.form.timeOut.length === 5 ? this.form.timeOut + ':00' : this.form.timeOut
    };
    const req = this.editing
      ? this.api.put(`/dtr/${this.editing.id}`, payload)
      : this.api.post('/dtr', payload);
    req.subscribe({
      next: () => {
        this.showModal = false;
        this.list.load('/dtr');
      },
      error: (err) => alert(err?.error?.message || 'Save failed')
    });
  }

  remove(item: any): void {
    if (!confirm('Delete this DTR record?')) return;
    this.api.delete(`/dtr/${item.id}`).subscribe({
      next: () => this.list.load('/dtr'),
      error: (err) => alert(err?.error?.message || 'Delete failed')
    });
  }
}
