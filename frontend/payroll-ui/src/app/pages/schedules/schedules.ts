import { Component, OnInit, inject } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { ListLoader } from '../../core/services/list-loader';

@Component({
  selector: 'app-schedules',
  imports: [FormsModule, AsyncPipe],
  providers: [ListLoader],
  templateUrl: './schedules.html',
  styleUrl: './schedules.scss'
})
export class SchedulesComponent implements OnInit {
  readonly list = inject(ListLoader);
  private readonly api = inject(ApiService);

  showModal = false;
  editing: any = null;

  readonly weekDays = [
    { id: 1, name: 'Monday' },
    { id: 2, name: 'Tuesday' },
    { id: 3, name: 'Wednesday' },
    { id: 4, name: 'Thursday' },
    { id: 5, name: 'Friday' },
    { id: 6, name: 'Saturday' },
    { id: 7, name: 'Sunday' }
  ];

  form: any = this.blank();

  ngOnInit(): void {
    this.list.load('/schedule-classes');
  }

  blank() {
    return {
      name: '',
      description: '',
      active: true,
      days: this.weekDays.map((d) => ({
        dayOfWeek: d.id,
        enabled: d.id <= 5,
        timeIn: '08:00',
        timeOut: '17:00'
      }))
    };
  }

  dayLabel(dayOfWeek: number): string {
    return this.weekDays.find((d) => d.id === dayOfWeek)?.name || String(dayOfWeek);
  }

  openCreate(): void {
    this.editing = null;
    this.form = this.blank();
    this.showModal = true;
    this.api.refreshUi();
  }

  openEdit(item: any): void {
    this.editing = item;
    this.form = {
      name: item.name,
      description: item.description || '',
      active: item.active,
      days: this.weekDays.map((d) => {
        const existing = (item.days || []).find((x: any) => x.dayOfWeek === d.id);
        return {
          dayOfWeek: d.id,
          enabled: !!existing,
          timeIn: existing ? String(existing.timeIn).slice(0, 5) : '08:00',
          timeOut: existing ? String(existing.timeOut).slice(0, 5) : '17:00'
        };
      })
    };
    this.showModal = true;
    this.api.refreshUi();
  }

  private payload() {
    const days = this.form.days
      .filter((d: any) => d.enabled)
      .map((d: any) => ({
        dayOfWeek: d.dayOfWeek,
        timeIn: d.timeIn.length === 5 ? d.timeIn + ':00' : d.timeIn,
        timeOut: d.timeOut.length === 5 ? d.timeOut + ':00' : d.timeOut
      }));
    return {
      name: this.form.name,
      description: this.form.description,
      active: this.form.active,
      days
    };
  }

  save(): void {
    const body = this.payload();
    if (!body.days.length) {
      alert('Enable at least one work day');
      return;
    }
    const req = this.editing
      ? this.api.put(`/schedule-classes/${this.editing.id}`, body)
      : this.api.post('/schedule-classes', body);
    req.subscribe({
      next: () => {
        this.showModal = false;
        this.list.load('/schedule-classes');
      },
      error: (err) => alert(err?.error?.message || 'Save failed')
    });
  }

  deactivate(item: any): void {
    if (!confirm(`Deactivate schedule class "${item.name}"?`)) return;
    this.api.patch(`/schedule-classes/${item.id}/deactivate`).subscribe({
      next: () => this.list.load('/schedule-classes'),
      error: (err) => alert(err?.error?.message || 'Deactivate failed')
    });
  }

  activate(item: any): void {
    this.api.patch(`/schedule-classes/${item.id}/activate`).subscribe({
      next: () => this.list.load('/schedule-classes'),
      error: (err) => alert(err?.error?.message || 'Activate failed')
    });
  }
}
