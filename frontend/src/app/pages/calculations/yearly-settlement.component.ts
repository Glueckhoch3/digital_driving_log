import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslateModule } from '@ngx-translate/core';
import { CarService } from '../../services/car.service';
import { CalculationService } from '../../services/calculation.service';
import { CarDto } from '../../models/cars';
import { YearlySettlementRow } from '../../models/calculations';

const YEARS_BACK = 6;

@Component({
  selector: 'app-yearly-settlement',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TranslateModule],
  templateUrl: './yearly-settlement.component.html',
})
export class YearlySettlementComponent implements OnInit {
  private readonly carService = inject(CarService);
  private readonly calculationService = inject(CalculationService);

  readonly cars = signal<CarDto[]>([]);
  readonly years: number[] = [];
  carId = 0;
  year = 0;

  readonly rows = signal<YearlySettlementRow[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly loaded = signal(false);

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
    const current = new Date().getFullYear();
    for (let y = current; y > current - YEARS_BACK; y--) {
      this.years.push(y);
    }
    this.year = current;
  }

  ngOnInit(): void {
    this.carService.getCars().subscribe({
      next: (cars) => {
        this.cars.set(cars);
        if (cars.length > 0) {
          this.carId = cars[0].carId;
          this.load();
        }
      },
    });
  }

  load(): void {
    if (!this.carId) return;
    this.loading.set(true);
    this.error.set('');
    this.calculationService.getYearlySettlement(this.carId, this.year).subscribe({
      next: (rows) => {
        this.rows.set(rows);
        this.loaded.set(true);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.rows.set([]);
        this.loaded.set(true);
        this.loading.set(false);
        this.error.set(
          err.status === 404 ? 'calculations.notCalculated' : 'calculations.messages.loadFailed',
        );
      },
    });
  }
}
