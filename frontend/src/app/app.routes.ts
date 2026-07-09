import { Routes } from '@angular/router';
import { StartComponent } from './pages/start/start.component';
import { CarSelectionComponent } from './pages/car-selection/car-selection.component';
import { CarWorkspaceComponent } from './pages/car-workspace/car-workspace.component';
import { AdminOverviewComponent } from './pages/admin-overview/admin-overview.component';
import { CarManagementComponent } from './pages/car-management/car-management.component';
import { UserManagementComponent } from './pages/user-management/user-management.component';

export const routes: Routes = [
  { path: '', component: StartComponent },
  { path: 'cars/select', component: CarSelectionComponent },
  { path: 'cars/:carId', component: CarWorkspaceComponent },
  { path: 'manage/cars', component: CarManagementComponent },
  { path: 'manage/users', component: UserManagementComponent },
  { path: 'overview', component: AdminOverviewComponent },
  { path: '**', redirectTo: '' }
];
