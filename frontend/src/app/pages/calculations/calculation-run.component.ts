import { Component, computed, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { CalculationService } from '../../services/calculation.service';
import { AvailabilityStore } from './availability.store';
import { CalcPeriodSelectComponent } from './calc-period-select.component';

type Message = { type: 'ok' | 'error' | 'warn'; key: string } | null;

/**
 * One selection (car + year + month), a status strip reading the availability
 * store, and a grid of the four period actions (issue #32). Redoing a year stays
 * two deliberate steps — delete, then calculate — there is no recalculate action.
 */
@Component({
  selector: 'app-calculation-run',
  standalone: true,
  imports: [RouterLink, TranslateModule, CalcPeriodSelectComponent],
  templateUrl: './calculation-run.component.html',
})
export class CalculationRunComponent {
  private readonly calculationService = inject(CalculationService);
  private readonly translate = inject(TranslateService);
  readonly store = inject(AvailabilityStore);

  readonly carId = signal(0);
  readonly year = signal(new Date().getFullYear());
  readonly month = signal(new Date().getMonth() + 1);

  readonly busy = signal(false);
  readonly message = signal<Message>(null);

  readonly participantCount = signal(0);
  readonly participantAddedCount = signal(0);

  readonly monthAggregated = computed(
    () => this.store.monthState(this.year(), this.month()) === 'stored',
  );
  readonly yearCalculated = computed(() => this.store.yearCalculated(this.year()));

  constructor() {
    effect(() => {
      const carId = this.carId();
      const year = this.year();
      if (carId && year) {
        this.calculationService.getParticipants(carId, year).subscribe({
          next: (set) => {
            const participating = set.rows.filter((r) => r.participating);
            this.participantCount.set(participating.length);
            this.participantAddedCount.set(participating.filter((r) => r.manuallyAdded).length);
          },
        });
      }
    });
  }

  aggregateMonth(): void {
    this.message.set(null);
    this.busy.set(true);
    this.calculationService
      .aggregateMonth({ carId: this.carId(), year: this.year(), month: this.month() })
      .subscribe({
        next: () => {
          this.busy.set(false);
          this.message.set({ type: 'ok', key: 'calculations.messages.monthAggregated' });
          this.store.refresh();
        },
        error: (err: HttpErrorResponse) => {
          this.busy.set(false);
          this.message.set({
            type: 'error',
            key:
              err.status === 409
                ? 'calculations.messages.monthExists'
                : 'calculations.messages.actionFailed',
          });
        },
      });
  }

  deleteMonth(): void {
    if (!window.confirm(this.translate.instant('calculations.run.confirmDeleteMonth'))) return;
    this.message.set(null);
    this.busy.set(true);
    this.calculationService.deleteMonth(this.carId(), this.year(), this.month()).subscribe({
      next: () => {
        this.busy.set(false);
        this.message.set({ type: 'ok', key: 'calculations.messages.deleted' });
        this.store.refresh();
      },
      error: () => {
        this.busy.set(false);
        this.message.set({ type: 'error', key: 'calculations.messages.actionFailed' });
      },
    });
  }

  calculateYear(): void {
    this.message.set(null);
    this.busy.set(true);
    this.calculationService.calculateYear({ carId: this.carId(), year: this.year() }).subscribe({
      next: () => {
        this.busy.set(false);
        this.message.set({ type: 'ok', key: 'calculations.messages.yearCalculated' });
        this.store.refresh();
      },
      error: (err: HttpErrorResponse) => {
        this.busy.set(false);
        this.message.set({
          type: 'error',
          key:
            err.status === 409
              ? 'calculations.messages.yearExists'
              : 'calculations.messages.actionFailed',
        });
      },
    });
  }

  deleteYear(): void {
    if (!window.confirm(this.translate.instant('calculations.run.confirmDeleteYear'))) return;
    this.message.set(null);
    this.busy.set(true);
    this.calculationService.deleteYear(this.carId(), this.year()).subscribe({
      next: () => {
        this.busy.set(false);
        this.message.set({ type: 'ok', key: 'calculations.messages.deleted' });
        this.store.refresh();
      },
      error: () => {
        this.busy.set(false);
        this.message.set({ type: 'error', key: 'calculations.messages.actionFailed' });
      },
    });
  }
}
