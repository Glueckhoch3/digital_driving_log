import { Component, effect, inject, input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { CalculationService } from '../../services/calculation.service';
import { MonthlyDistance } from '../../models/calculations';
import { AvailabilityStore } from './availability.store';

/** Tab body of the results page (issue #32): per-driver monthly distances for a car-year. */
@Component({
  selector: 'app-monthly-distances',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './monthly-distances.component.html',
})
export class MonthlyDistancesComponent {
  private readonly calculationService = inject(CalculationService);
  readonly store = inject(AvailabilityStore);

  readonly carId = input.required<number>();
  readonly year = input.required<number>();

  readonly rows = signal<MonthlyDistance[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');

  constructor() {
    effect(() => {
      const carId = this.carId();
      const year = this.year();
      if (!carId) return;
      this.loading.set(true);
      this.error.set('');
      this.calculationService.getMonthlyDistances(carId, year).subscribe({
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
