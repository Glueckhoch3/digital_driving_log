import { Routes } from '@angular/router';
import { DrivingLogComponent } from './pages/driving-log/driving-log.component';
import { DrivingHistoryComponent } from './pages/driving-history/driving-history.component';
import { CostsComponent } from './pages/costs/costs.component';
import { ShareholdersComponent } from './pages/shareholders/shareholders.component';
import { SettlementsComponent } from './pages/settlements/settlements.component';
import { FuelComponent } from './pages/fuel/fuel.component';
import { ReportsComponent } from './pages/reports/reports.component';

export const routes: Routes = [
  { path: '', redirectTo: '/drives', pathMatch: 'full' },
  { path: 'drives', component: DrivingLogComponent },
  { path: 'history', component: DrivingHistoryComponent },
  { path: 'costs', component: CostsComponent },
  { path: 'shareholders', component: ShareholdersComponent },
  { path: 'settlements', component: SettlementsComponent },
  { path: 'fuel', component: FuelComponent },
  { path: 'reports', component: ReportsComponent },
  { path: '**', redirectTo: '/drives' }
];
