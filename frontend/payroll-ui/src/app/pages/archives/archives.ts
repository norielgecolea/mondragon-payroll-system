import { Component, OnInit, inject } from '@angular/core';
import { AsyncPipe, CurrencyPipe, DatePipe } from '@angular/common';
import { BehaviorSubject } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { ListLoader } from '../../core/services/list-loader';

@Component({
  selector: 'app-archives',
  imports: [AsyncPipe, CurrencyPipe, DatePipe],
  providers: [ListLoader],
  templateUrl: './archives.html'
})
export class ArchivesComponent implements OnInit {
  readonly list = inject(ListLoader);
  private readonly api = inject(ApiService);
  readonly snapshot$ = new BehaviorSubject<any | null>(null);
  private selectedArchive: any | null = null;

  ngOnInit(): void {
    this.list.load('/payrolls/archives');
  }

  open(item: any): void {
    this.selectedArchive = item;
    this.api.get<any>(`/payrolls/archives/${item.id}`).subscribe({
      next: (a) => {
        try {
          this.snapshot$.next(JSON.parse(a.snapshotJson));
        } catch {
          this.snapshot$.next(null);
          alert('Invalid archive snapshot');
        }
        this.api.refreshUi();
      },
      error: (err) => alert(err?.error?.message || 'Failed to open archive')
    });
  }

  print(): void {
    const snap = this.snapshot$.value;
    const payrollId = this.selectedArchive?.payrollId ?? snap?.id;
    if (!payrollId) {
      alert('No payroll linked for payslip download');
      return;
    }
    this.api.download(`/payrolls/${payrollId}/payslips`).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `payslips-${snap?.payrollNumber || payrollId}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: async (err) => {
        let msg = 'Failed to download payslips';
        try {
          if (err?.error instanceof Blob) {
            const text = await err.error.text();
            const parsed = JSON.parse(text);
            msg = parsed.message || msg;
          }
        } catch { /* ignore */ }
        alert(msg);
      }
    });
  }
}
