import { Component, effect, inject, input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { CalculationService } from '../../services/calculation.service';
import { FactorRow } from '../../models/calculations';
import { AvailabilityStore } from './availability.store';

/** Tab body of the results page (issue #32): the run's per-driver variable/fixed factors. */
@Component({
  selector: 'app-distribution-factors',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './distribution-factors.component.html',
})
export class DistributionFactorsComponent {
  private readonly calculationService = inject(CalculationService);
  readonly store = inject(AvailabilityStore);

  readonly carId = input.required<number>();
  readonly year = input.required<number>();

  readonly rows = signal<FactorRow[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');

  constructor() {
    effect(() => {
      const carId = this.carId();
      const year = this.year();
      if (!carId) return;
      this.loading.set(true);
      this.error.set('');
      this.calculationService.getFactors(carId, year).subscribe({
        next: (rows) => {
          this.rows.set(rows);
          this.loading.set(false);
        },
        error: () => {
          this.rows.set([]);
          this.loading.set(false);
          this.error.set('calculations.messages.loadFailed');
        },
      });
    });
  }
}
