import { Routes } from '@angular/router';
import { StartComponent } from './pages/start/start.component';
import { CarSelectionComponent } from './pages/car-selection/car-selection.component';
import { CarHubComponent } from './pages/car-hub/car-hub.component';
import { DrivesListComponent } from './pages/drives-list/drives-list.component';
import { DriveFormComponent } from './pages/drive-form/drive-form.component';
import { CostsListComponent } from './pages/costs-list/costs-list.component';
import { CostFormComponent } from './pages/cost-form/cost-form.component';
import { AdminOverviewComponent } from './pages/admin-overview/admin-overview.component';
import { CarManagementComponent } from './pages/car-management/car-management.component';
import { UserManagementComponent } from './pages/user-management/user-management.component';
import { carResolver } from './resolvers/car.resolver';
import { usersResolver } from './resolvers/users.resolver';

export const routes: Routes = [
  { path: '', component: StartComponent },
  { path: 'cars/select', component: CarSelectionComponent },
  {
    path: 'cars/:carId',
    resolve: { car: carResolver, users: usersResolver },
    children: [
      { path: '', component: CarHubComponent },
      { path: 'drives', component: DrivesListComponent },
      { path: 'drives/new', component: DriveFormComponent },
      { path: 'costs', component: CostsListComponent },
      { path: 'costs/new', component: CostFormComponent },
    ],
  },
  { path: 'manage/cars', component: CarManagementComponent },
  { path: 'manage/users', component: UserManagementComponent },
  { path: 'overview', component: AdminOverviewComponent },
  { path: '**', redirectTo: '' },
];
