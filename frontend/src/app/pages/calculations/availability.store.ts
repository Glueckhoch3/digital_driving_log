import { Injectable, inject, signal } from '@angular/core';
import { CalculationService } from '../../services/calculation.service';
import { CarAvailability } from '../../models/calculations';

export type MonthState = 'stored' | 'data' | 'none';

/**
 * Holds one car's availability picture (issue #32) so the run, results and hub
 * screens colour their car/year/month selectors from a single source of truth
 * instead of each calling `/monthly/exists` and `/yearly/exists` themselves.
 */
@Injectable({ providedIn: 'root' })
export class AvailabilityStore {
  private readonly calculationService = inject(CalculationService);

  readonly availability = signal<CarAvailability | null>(null);
  readonly loading = signal(false);

  private loadedCarId: number | null = null;

  load(carId: number): void {
    if (!carId) return;
    this.loadedCarId = carId;
    this.loading.set(true);
    this.calculationService.getAvailability(carId).subscribe({
      next: (a) => {
        if (this.loadedCarId === carId) {
          this.availability.set(a);
          this.loading.set(false);
        }
      },
      error: () => {
        if (this.loadedCarId === carId) {
          this.availability.set(null);
          this.loading.set(false);
        }
      },
    });
  }

  refresh(): void {
    if (this.loadedCarId != null) {
      this.load(this.loadedCarId);
    }
  }

  monthState(year: number, month: number): MonthState {
    const y = this.availability()?.years.find((yr) => yr.year === year);
    if (!y) return 'none';
    if (y.aggregatedMonths.includes(month)) return 'stored';
    if (y.monthsWithDrives.includes(month)) return 'data';
    return 'none';
  }

  yearCalculated(year: number): boolean {
    return !!this.availability()?.years.find((yr) => yr.year === year)?.yearCalculated;
  }

  participantsStored(year: number): boolean {
    return !!this.availability()?.years.find((yr) => yr.year === year)?.participantsStored;
  }

  aggregatedMonths(year: number): number[] {
    return this.availability()?.years.find((yr) => yr.year === year)?.aggregatedMonths ?? [];
  }

  storedYears(): number[] {
    return (
      this.availability()
        ?.years.filter((yr) => yr.participantsStored)
        .map((yr) => yr.year) ?? []
    );
  }

  calculatedYears(): number[] {
    return (
      this.availability()
        ?.years.filter((yr) => yr.yearCalculated)
        .map((yr) => yr.year) ?? []
    );
  }
}
