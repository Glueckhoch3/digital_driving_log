import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslateModule } from '@ngx-translate/core';
import { CalculationService } from '../../services/calculation.service';
import { YearlySettlementRow } from '../../models/calculations';

/** Tab body of the results page (issue #32): one car-year's owed split per driver. */
@Component({
  selector: 'app-yearly-settlement',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './yearly-settlement.component.html',
})
export class YearlySettlementComponent {
  private readonly calculationService = inject(CalculationService);

  readonly carId = input.required<number>();
  readonly year = input.required<number>();

  readonly rows = signal<YearlySettlementRow[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');

  readonly totals = computed(() => {
    const rows = this.rows();
    return {
      distance: rows.reduce((sum, r) => sum + r.distance, 0),
      variableOwed: rows.reduce((sum, r) => sum + r.variableOwed, 0),
      fixedOwed: rows.reduce((sum, r) => sum + r.fixedOwed, 0),
      totalOwed: rows.reduce((sum, r) => sum + r.totalOwed, 0),
    };
  });

  constructor() {
    effect(() => {
      const carId = this.carId();
      const year = this.year();
      if (!carId) return;
      this.loading.set(true);
      this.error.set('');
      this.calculationService.getYearlySettlement(carId, year).subscribe({
        next: (rows) => {
          this.rows.set(rows);
          this.loading.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.rows.set([]);
          this.loading.set(false);
          this.error.set(
            err.status === 404 ? 'calculations.notCalculated' : 'calculations.messages.loadFailed',
          );
        },
      });
    });
  }
}
