import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive, RouterOutlet, NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from '../core/services/auth.service';
import { ApiService } from '../core/services/api.service';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './shell.html',
  styleUrl: './shell.scss'
})
export class ShellComponent {
  private readonly router = inject(Router);
  private readonly api = inject(ApiService);
  readonly auth = inject(AuthService);

  navOpen = false;
  accountOpen = false;
  accountUsername = '';
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  accountMessage = '';
  accountError = '';
  accountSaving = false;

  readonly links = [
    { path: '/dashboard', label: 'Dashboard', icon: '▣' },
    { path: '/employees', label: 'Employees', icon: '☺' },
    { path: '/positions', label: 'Positions', icon: '◈' },
    { path: '/salary-rates', label: 'Salary Rates', icon: '₱' },
    { path: '/schedules', label: 'Schedule Classes', icon: '◷' },
    { path: '/dtr', label: 'DTR', icon: '☑' },
    { path: '/overtime', label: 'Overtime', icon: '⚡' },
    { path: '/cash-advances', label: 'Cash Advances', icon: '⇄' },
    { path: '/savings', label: 'Savings', icon: '◈' },
    { path: '/payrolls', label: 'Payroll', icon: '☰' },
    { path: '/archives', label: 'Archives', icon: '▤' }
  ];

  constructor() {
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => {
      this.navOpen = false;
      this.api.refreshUi();
      setTimeout(() => this.api.refreshUi(), 50);
      setTimeout(() => this.api.refreshUi(), 200);
    });
  }

  openAccount(): void {
    this.accountOpen = true;
    this.accountMessage = '';
    this.accountError = '';
    this.accountUsername = this.auth.username() || '';
    this.currentPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.navOpen = false;
    this.api.get<{ username: string }>('/auth/me').subscribe({
      next: (a) => {
        this.accountUsername = a.username;
        this.api.refreshUi();
      }
    });
    this.api.refreshUi();
  }

  closeAccount(): void {
    this.accountOpen = false;
    this.accountMessage = '';
    this.accountError = '';
    this.api.refreshUi();
  }

  saveAccount(): void {
    this.accountMessage = '';
    this.accountError = '';
    if (!this.currentPassword) {
      this.accountError = 'Current password is required';
      return;
    }
    if (this.newPassword && this.newPassword !== this.confirmPassword) {
      this.accountError = 'New password confirmation does not match';
      return;
    }
    if (this.newPassword && this.newPassword.length < 6) {
      this.accountError = 'New password must be at least 6 characters';
      return;
    }

    this.accountSaving = true;
    const body: Record<string, string> = {
      currentPassword: this.currentPassword,
      username: this.accountUsername
    };
    if (this.newPassword) {
      body['newPassword'] = this.newPassword;
    }

    this.api.put<{ token: string; username: string }>('/auth/account', body).subscribe({
      next: (res) => {
        this.auth.applySession(res.token, res.username);
        this.currentPassword = '';
        this.newPassword = '';
        this.confirmPassword = '';
        this.accountMessage = 'Account updated';
        this.accountSaving = false;
        this.api.refreshUi();
      },
      error: (err) => {
        this.accountError = err?.error?.message || 'Update failed';
        this.accountSaving = false;
        this.api.refreshUi();
      }
    });
  }

  logout(): void {
    this.auth.logout();
  }
}
