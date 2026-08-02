import { Component, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { CalcPeriodSelectComponent } from './calc-period-select.component';
import { YearlySettlementComponent } from './yearly-settlement.component';
import { MonthlyDistancesComponent } from './monthly-distances.component';
import { DistributionFactorsComponent } from './distribution-factors.component';

export type ResultsTab = 'yearly' | 'monthly' | 'factors';

/**
 * Merges the three car-scoped result views behind one selection and a tab bar
 * (issue #32, ask 1.2). All three tab bodies stay mounted (just hidden) so
 * switching tabs back and forth never re-hits the API for the same car/year.
 */
@Component({
  selector: 'app-calculation-results',
  standalone: true,
  imports: [
    RouterLink,
    TranslateModule,
    CalcPeriodSelectComponent,
    YearlySettlementComponent,
    MonthlyDistancesComponent,
    DistributionFactorsComponent,
  ],
  templateUrl: './calculation-results.component.html',
})
export class CalculationResultsComponent {
  readonly carId = signal(0);
  readonly year = signal(new Date().getFullYear());
  readonly tab = signal<ResultsTab>('yearly');

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
  ) {
    const requested = this.route.snapshot.queryParamMap.get('tab');
    if (requested === 'yearly' || requested === 'monthly' || requested === 'factors') {
      this.tab.set(requested);
    }
  }

  selectTab(tab: ResultsTab): void {
    this.tab.set(tab);
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { tab },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }
}
