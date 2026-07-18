import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { ShellComponent } from './layout/shell';
import { LoginComponent } from './pages/login/login';
import { DashboardComponent } from './pages/dashboard/dashboard';
import { EmployeesComponent } from './pages/employees/employees';
import { PositionsComponent } from './pages/positions/positions';
import { SalaryRatesComponent } from './pages/salary-rates/salary-rates';
import { SchedulesComponent } from './pages/schedules/schedules';
import { DtrComponent } from './pages/dtr/dtr';
import { OvertimeComponent } from './pages/overtime/overtime';
import { CashAdvancesComponent } from './pages/cash-advances/cash-advances';
import { SavingsComponent } from './pages/savings/savings';
import { PayrollsComponent } from './pages/payrolls/payrolls';
import { ArchivesComponent } from './pages/archives/archives';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'employees', component: EmployeesComponent },
      { path: 'positions', component: PositionsComponent },
      { path: 'salary-rates', component: SalaryRatesComponent },
      { path: 'schedules', component: SchedulesComponent },
      { path: 'dtr', component: DtrComponent },
      { path: 'overtime', component: OvertimeComponent },
      { path: 'cash-advances', component: CashAdvancesComponent },
      { path: 'savings', component: SavingsComponent },
      { path: 'payrolls', component: PayrollsComponent },
      { path: 'archives', component: ArchivesComponent }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
