import { Component, OnInit, inject } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { ListLoader } from '../../core/services/list-loader';

@Component({
  selector: 'app-positions',
  imports: [FormsModule, AsyncPipe],
  providers: [ListLoader],
  templateUrl: './positions.html'
})
export class PositionsComponent implements OnInit {
  readonly list = inject(ListLoader);
  private readonly api = inject(ApiService);
  showModal = false;
  editing: any = null;
  form: any = { title: '', description: '', active: true };

  ngOnInit(): void {
    this.list.load('/positions');
  }

  openCreate(): void {
    this.editing = null;
    this.form = { title: '', description: '', active: true };
    this.showModal = true;
  }

  openEdit(item: any): void {
    this.editing = item;
    this.form = { ...item };
    this.showModal = true;
  }

  save(): void {
    const req = this.editing
      ? this.api.put(`/positions/${this.editing.id}`, this.form)
      : this.api.post('/positions', this.form);
    req.subscribe({
      next: () => {
        this.showModal = false;
        this.list.load('/positions');
      },
      error: (err) => alert(err?.error?.message || 'Save failed')
    });
  }

  remove(item: any): void {
    if (!confirm(`Deactivate position "${item.title}"?`)) return;
    this.api.patch(`/positions/${item.id}/deactivate`).subscribe({
      next: () => this.list.load('/positions'),
      error: (err) => alert(err?.error?.message || 'Deactivate failed')
    });
  }

  activate(item: any): void {
    this.api.patch(`/positions/${item.id}/activate`).subscribe({
      next: () => this.list.load('/positions'),
      error: (err) => alert(err?.error?.message || 'Activate failed')
    });
  }
}
