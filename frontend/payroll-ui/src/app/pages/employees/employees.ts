import { Component, OnInit, inject } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { ListLoader } from '../../core/services/list-loader';

@Component({
  selector: 'app-employees',
  imports: [FormsModule, AsyncPipe],
  providers: [ListLoader],
  templateUrl: './employees.html'
})
export class EmployeesComponent implements OnInit {
  readonly list = inject(ListLoader);
  private readonly api = inject(ApiService);

  positions: any[] = [];
  rates: any[] = [];
  scheduleClasses: any[] = [];
  showModal = false;
  editing: any = null;
  form: any = this.blank();

  ngOnInit(): void {
    this.list.load('/employees');
    this.list.fetch('/positions/active').subscribe((d) => {
      this.positions = d;
      this.api.refreshUi();
    });
    this.list.fetch('/salary-rates/active').subscribe((d) => {
      this.rates = d;
      this.api.refreshUi();
    });
    this.list.fetch('/schedule-classes/active').subscribe((d) => {
      this.scheduleClasses = d;
      this.api.refreshUi();
    });
  }

  blank() {
    return {
      employeeCode: '',
      firstName: '',
      lastName: '',
      middleName: '',
      gender: 'Male',
      phone: '',
      email: '',
      address: '',
      hireDate: new Date().toISOString().slice(0, 10),
      positionId: null as number | null,
      salaryRateId: null as number | null,
      scheduleClassId: null as number | null,
      active: true
    };
  }

  openCreate(): void {
    this.editing = null;
    this.form = this.blank();
    if (this.positions[0]) this.form.positionId = this.positions[0].id;
    if (this.rates[0]) this.form.salaryRateId = this.rates[0].id;
    if (this.scheduleClasses[0]) this.form.scheduleClassId = this.scheduleClasses[0].id;
    this.showModal = true;
  }

  openEdit(item: any): void {
    this.editing = item;
    this.form = { ...item };
    this.showModal = true;
  }

  save(): void {
    const req = this.editing
      ? this.api.put(`/employees/${this.editing.id}`, this.form)
      : this.api.post('/employees', this.form);
    req.subscribe({
      next: () => {
        this.showModal = false;
        this.list.load('/employees');
      },
      error: (err) => alert(err?.error?.message || 'Save failed')
    });
  }

  remove(item: any): void {
    if (!confirm(`Deactivate ${item.fullName}?`)) return;
    this.api.patch(`/employees/${item.id}/deactivate`).subscribe({
      next: () => this.list.load('/employees'),
      error: (err) => alert(err?.error?.message || 'Deactivate failed')
    });
  }

  activate(item: any): void {
    this.api.patch(`/employees/${item.id}/activate`).subscribe({
      next: () => this.list.load('/employees'),
      error: (err) => alert(err?.error?.message || 'Activate failed')
    });
  }
}
