import { Routes } from '@angular/router';
import { CarSelectionComponent } from './pages/car-selection/car-selection.component';
import { CarHubComponent } from './pages/car-hub/car-hub.component';
import { DrivesListComponent } from './pages/drives-list/drives-list.component';
import { DriveFormComponent } from './pages/drive-form/drive-form.component';
import { CostsListComponent } from './pages/costs-list/costs-list.component';
import { CostFormComponent } from './pages/cost-form/cost-form.component';
import { UploadComponent } from './pages/upload/upload.component';
import { AdminOverviewComponent } from './pages/admin-overview/admin-overview.component';
import { CarManagementComponent } from './pages/car-management/car-management.component';
import { UserManagementComponent } from './pages/user-management/user-management.component';
import { CalculationHubComponent } from './pages/calculations/calculation-hub.component';
import { CalculationRunComponent } from './pages/calculations/calculation-run.component';
import { YearlySettlementComponent } from './pages/calculations/yearly-settlement.component';
import { CombinedSettlementComponent } from './pages/calculations/combined-settlement.component';
import { MonthlyDistancesComponent } from './pages/calculations/monthly-distances.component';
import { DistributionFactorsComponent } from './pages/calculations/distribution-factors.component';
import { carResolver } from './resolvers/car.resolver';
import { usersResolver } from './resolvers/users.resolver';

export const routes: Routes = [
  { path: '', component: CarSelectionComponent },
  { path: 'cars/select', redirectTo: '' },
  {
    path: 'cars/:carId',
    resolve: { car: carResolver, users: usersResolver },
    children: [
      { path: '', component: CarHubComponent },
      { path: 'drives', component: DrivesListComponent },
      { path: 'drives/new', component: DriveFormComponent },
      { path: 'costs', component: CostsListComponent },
      { path: 'costs/new', component: CostFormComponent },
      { path: 'upload', component: UploadComponent },
    ],
  },
  { path: 'manage/cars', component: CarManagementComponent },
  { path: 'manage/users', component: UserManagementComponent },
  { path: 'overview', component: AdminOverviewComponent },
  {
    path: 'calculations',
    children: [
      { path: '', component: CalculationHubComponent },
      { path: 'run', component: CalculationRunComponent },
      { path: 'yearly', component: YearlySettlementComponent },
      { path: 'combined', component: CombinedSettlementComponent },
      { path: 'monthly', component: MonthlyDistancesComponent },
      { path: 'factors', component: DistributionFactorsComponent },
    ],
  },
  { path: '**', redirectTo: '' },
];
