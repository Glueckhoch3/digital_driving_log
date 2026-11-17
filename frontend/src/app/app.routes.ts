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
import { CalculationResultsComponent } from './pages/calculations/calculation-results.component';
import { CombinedSettlementComponent } from './pages/calculations/combined-settlement.component';
import { participantsDirtyGuard } from './pages/calculations/participants-dirty.guard';
import { carResolver } from './resolvers/car.resolver';
import { usersResolver } from './resolvers/users.resolver';
import { costCategoriesResolver } from './resolvers/cost-categories.resolver';
import { CostCategoryManagementComponent } from './pages/cost-category-management/cost-category-management.component';
import { StatisticsComponent } from './pages/statistics/statistics.component';

export const routes: Routes = [
  { path: '', component: CarSelectionComponent },
  { path: 'cars/select', redirectTo: '' },
  {
    path: 'cars/:carId',
    resolve: { car: carResolver, users: usersResolver, costCategories: costCategoriesResolver },
    children: [
      { path: '', component: CarHubComponent },
      { path: 'drives', component: DrivesListComponent },
      { path: 'drives/new', component: DriveFormComponent },
      { path: 'costs', component: CostsListComponent },
      { path: 'costs/new', component: CostFormComponent },
      { path: 'statistics', component: StatisticsComponent },
      { path: 'upload', component: UploadComponent },
    ],
  },
  { path: 'manage/cars', component: CarManagementComponent },
  { path: 'manage/users', component: UserManagementComponent },
  { path: 'manage/cost-categories', component: CostCategoryManagementComponent },
  { path: 'overview', component: AdminOverviewComponent },
  {
    path: 'calculations',
    children: [
      { path: '', component: CalculationHubComponent, canDeactivate: [participantsDirtyGuard] },
      { path: 'run', component: CalculationRunComponent },
      { path: 'results', component: CalculationResultsComponent },
      { path: 'combined', component: CombinedSettlementComponent },
      { path: 'yearly', redirectTo: () => '/calculations/results?tab=yearly' },
      { path: 'monthly', redirectTo: () => '/calculations/results?tab=monthly' },
      { path: 'factors', redirectTo: () => '/calculations/results?tab=factors' },
    ],
  },
  { path: '**', redirectTo: '' },
];
