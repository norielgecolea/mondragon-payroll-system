import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { AsyncPipe, CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BehaviorSubject, forkJoin } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { ListLoader } from '../../core/services/list-loader';

@Component({
  selector: 'app-payrolls',
  imports: [FormsModule, AsyncPipe, CurrencyPipe, DatePipe, RouterLink],
  providers: [ListLoader],
  templateUrl: './payrolls.html',
  styleUrl: './payrolls.scss'
})
export class PayrollsComponent implements OnInit {
  readonly list = inject(ListLoader);
  private readonly api = inject(ApiService);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly selected$ = new BehaviorSubject<any | null>(null);
  employees: any[] = [];
  advances: any[] = [];
  previewLoading = false;
  showGenerate = false;
  periodStart = '';
  periodEnd = '';
  remarks = '';
  deductions: Record<number, {
    bonusAmount: number | null;
    deductCashAdvance: boolean;
    cashAdvanceAmount: number | null;
    deductSavings: boolean;
    savingsAmount: number | null;
    deductSss: boolean;
    sssAmount: number | null;
    deductPhilhealth: boolean;
    philhealthAmount: number | null;
  }> = {};

  constructor() {
    const now = new Date();
    const start = new Date(now.getFullYear(), now.getMonth(), 1);
    const end = new Date(now.getFullYear(), now.getMonth() + 1, 0);
    this.periodStart = start.toISOString().slice(0, 10);
    this.periodEnd = end.toISOString().slice(0, 10);
  }

  ngOnInit(): void {
    this.list.load('/payrolls');
  }

  openGenerate(): void {
    if (!this.periodStart || !this.periodEnd) {
      alert('Set period start and end first');
      return;
    }
    this.previewLoading = true;
    this.api.refreshUi();

    forkJoin({
      emps: this.api.getList<any>('/employees/active'),
      advances: this.api.getList<any>('/cash-advances'),
      preview: this.api.getList<any>('/payrolls/preview', {
        periodStart: this.periodStart,
        periodEnd: this.periodEnd
      })
    }).subscribe({
      next: ({ emps, advances, preview }) => {
        const previewMap: Record<number, any> = {};
        preview.forEach((row) => {
          previewMap[row.employeeId] = row;
        });

        this.deductions = {};
        this.employees = emps.map((e) => {
          this.deductions[e.id] = {
            bonusAmount: null,
            deductCashAdvance: false,
            cashAdvanceAmount: null,
            deductSavings: false,
            savingsAmount: null,
            deductSss: false,
            sssAmount: null,
            deductPhilhealth: false,
            philhealthAmount: null
          };
          return { ...e, preview: previewMap[e.id] ?? null };
        });
        this.advances = advances.filter((a) => a.status === 'ACTIVE');
        this.previewLoading = false;
        this.showGenerate = true;
        this.cdr.detectChanges();
        this.api.refreshUi();
      },
      error: (err) => {
        this.previewLoading = false;
        this.cdr.detectChanges();
        alert(err?.error?.message || 'Failed to open generate payroll');
        this.api.refreshUi();
      }
    });
  }

  onPeriodChange(): void {
    if (this.showGenerate) {
      this.loadPreview();
    }
  }

  loadPreview(): void {
    if (!this.periodStart || !this.periodEnd) return;
    this.previewLoading = true;
    this.cdr.detectChanges();
    this.api.getList<any>('/payrolls/preview', {
      periodStart: this.periodStart,
      periodEnd: this.periodEnd
    }).subscribe({
      next: (rows) => {
        const previewMap: Record<number, any> = {};
        rows.forEach((row) => {
          previewMap[row.employeeId] = row;
        });
        this.employees = this.employees.map((e) => ({
          ...e,
          preview: previewMap[e.id] ?? null
        }));
        this.previewLoading = false;
        this.cdr.detectChanges();
        this.api.refreshUi();
      },
      error: (err) => {
        this.previewLoading = false;
        this.employees = this.employees.map((e) => ({ ...e, preview: null }));
        this.cdr.detectChanges();
        alert(err?.error?.message || 'Failed to load salary preview');
        this.api.refreshUi();
      }
    });
  }

  advanceFor(empId: number): any {
    return this.advances.find((a) => a.employeeId === empId);
  }

  generate(): void {
    const payload = {
      periodStart: this.periodStart,
      periodEnd: this.periodEnd,
      remarks: this.remarks,
      deductions: Object.entries(this.deductions).map(([employeeId, opt]) => ({
        employeeId: Number(employeeId),
        ...opt
      }))
    };
    this.api.post<any>('/payrolls/generate', payload).subscribe({
      next: (p) => {
        this.showGenerate = false;
        this.list.load('/payrolls');
        this.view(p.id);
      },
      error: (err) => alert(err?.error?.message || 'Generate failed')
    });
  }

  view(id: number): void {
    this.api.get<any>(`/payrolls/${id}`).subscribe({
      next: (p) => {
        this.selected$.next(p);
        this.api.refreshUi();
      },
      error: (err) => alert(err?.error?.message || 'Failed to open payroll')
    });
  }

  finalize(): void {
    const current = this.selected$.value;
    if (!current || !confirm('Finalize payroll? Cash advances and savings deductions will be applied.')) return;
    this.api.post<any>(`/payrolls/${current.id}/finalize`, {}).subscribe({
      next: (p) => {
        this.selected$.next(p);
        this.list.load('/payrolls');
      },
      error: (err) => alert(err?.error?.message || 'Finalize failed')
    });
  }

  archive(): void {
    const current = this.selected$.value;
    if (!current || !confirm('Archive this finalized payroll? It will move to Archives and leave the Payroll list.')) return;
    this.api.post(`/payrolls/${current.id}/archive`, {}).subscribe({
      next: () => {
        this.selected$.next(null);
        this.list.load('/payrolls');
        alert('Payroll archived. Open Archives to view it.');
      },
      error: (err) => alert(err?.error?.message || 'Archive failed')
    });
  }

  print(): void {
    const current = this.selected$.value;
    if (!current?.id) return;
    this.api.download(`/payrolls/${current.id}/payslips`).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `payslips-${current.payrollNumber || current.id}.pdf`;
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

  exportExcel(): void {
    const current = this.selected$.value;
    if (!current?.id) return;
    this.api.download(`/payrolls/${current.id}/export.xlsx`).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `payroll-${current.payrollNumber || current.id}.xlsx`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: async (err) => {
        let msg = 'Failed to export Excel';
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

  remove(p: any): void {
    if (!confirm('Delete this draft payroll?')) return;
    this.api.delete(`/payrolls/${p.id}`).subscribe({
      next: () => {
        if (this.selected$.value?.id === p.id) this.selected$.next(null);
        this.list.load('/payrolls');
      },
      error: (err) => alert(err?.error?.message || 'Delete failed')
    });
  }
}
