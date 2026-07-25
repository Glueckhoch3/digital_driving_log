import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslateModule } from '@ngx-translate/core';
import { CarService } from '../../services/car.service';
import { CalculationService } from '../../services/calculation.service';
import { CarDto } from '../../models/cars';
import { selectableYears } from '../../utils/selectable-years';

type Message = { type: 'ok' | 'error' | 'warn'; key: string } | null;

@Component({
  selector: 'app-calculation-run',
  standalone: true,
  imports: [FormsModule, RouterLink, TranslateModule],
  templateUrl: './calculation-run.component.html',
})
export class CalculationRunComponent implements OnInit {
  private readonly carService = inject(CarService);
  private readonly calculationService = inject(CalculationService);

  readonly cars = signal<CarDto[]>([]);
  readonly years: number[] = selectableYears();
  readonly months = Array.from({ length: 12 }, (_, i) => i + 1);

  // Monthly panel
  monthlyCarId = 0;
  monthlyYear = 0;
  monthlyMonth = 1;
  readonly monthlyExists = signal(false);
  readonly monthlyMessage = signal<Message>(null);
  readonly monthlyBusy = signal(false);

  // Yearly panel
  yearlyCarId = 0;
  yearlyYear = 0;
  readonly yearlyExists = signal(false);
  readonly yearlyMessage = signal<Message>(null);
  readonly yearlyBusy = signal(false);

  // Delete panel
  deleteCarId = 0;
  deletePeriod: 'monthly' | 'yearly' = 'yearly';
  deleteYear = 0;
  deleteMonth = 1;
  readonly deleteMessage = signal<Message>(null);
  readonly deleteBusy = signal(false);

  constructor() {
    const current = new Date().getFullYear();
    this.monthlyYear = current;
    this.yearlyYear = current;
    this.deleteYear = current;
    this.monthlyMonth = new Date().getMonth() + 1;
  }

  ngOnInit(): void {
    this.carService.getCars().subscribe({
      next: (cars) => {
        this.cars.set(cars);
        if (cars.length > 0) {
          const first = cars[0].carId;
          this.monthlyCarId = first;
          this.yearlyCarId = first;
          this.deleteCarId = first;
          this.refreshMonthlyExists();
          this.refreshYearlyExists();
        }
      },
    });
  }

  // --- existence checks ---

  refreshMonthlyExists(): void {
    if (!this.monthlyCarId) return;
    this.calculationService
      .monthlyExists(this.monthlyCarId, this.monthlyYear, this.monthlyMonth)
      .subscribe({ next: (exists) => this.monthlyExists.set(exists) });
  }

  refreshYearlyExists(): void {
    if (!this.yearlyCarId) return;
    this.calculationService
      .yearlyExists(this.yearlyCarId, this.yearlyYear)
      .subscribe({ next: (exists) => this.yearlyExists.set(exists) });
  }

  // --- submit handlers ---

  aggregateMonth(): void {
    this.monthlyMessage.set(null);
    this.monthlyBusy.set(true);
    this.calculationService
      .aggregateMonth({
        carId: this.monthlyCarId,
        year: this.monthlyYear,
        month: this.monthlyMonth,
      })
      .subscribe({
        next: () => {
          this.monthlyBusy.set(false);
          this.monthlyMessage.set({ type: 'ok', key: 'calculations.messages.monthAggregated' });
          this.refreshMonthlyExists();
        },
        error: (err: HttpErrorResponse) => {
          this.monthlyBusy.set(false);
          this.monthlyMessage.set({
            type: 'error',
            key:
              err.status === 409
                ? 'calculations.messages.monthExists'
                : 'calculations.messages.actionFailed',
          });
          this.refreshMonthlyExists();
        },
      });
  }

  calculateYear(): void {
    this.yearlyMessage.set(null);
    this.yearlyBusy.set(true);
    this.calculationService
      .calculateYear({ carId: this.yearlyCarId, year: this.yearlyYear })
      .subscribe({
        next: () => {
          this.yearlyBusy.set(false);
          this.yearlyMessage.set({ type: 'ok', key: 'calculations.messages.yearCalculated' });
          this.refreshYearlyExists();
        },
        error: (err: HttpErrorResponse) => {
          this.yearlyBusy.set(false);
          this.yearlyMessage.set({
            type: 'error',
            key:
              err.status === 409
                ? 'calculations.messages.yearExists'
                : 'calculations.messages.actionFailed',
          });
          this.refreshYearlyExists();
        },
      });
  }

  deleteRun(): void {
    this.deleteMessage.set(null);
    this.deleteBusy.set(true);
    const done = {
      next: () => {
        this.deleteBusy.set(false);
        this.deleteMessage.set({ type: 'ok', key: 'calculations.messages.deleted' });
        this.refreshMonthlyExists();
        this.refreshYearlyExists();
      },
      error: () => {
        this.deleteBusy.set(false);
        this.deleteMessage.set({ type: 'error', key: 'calculations.messages.actionFailed' });
      },
    };
    if (this.deletePeriod === 'monthly') {
      this.calculationService
        .deleteMonth(this.deleteCarId, this.deleteYear, this.deleteMonth)
        .subscribe(done);
    } else {
      this.calculationService.deleteYear(this.deleteCarId, this.deleteYear).subscribe(done);
    }
  }
}
