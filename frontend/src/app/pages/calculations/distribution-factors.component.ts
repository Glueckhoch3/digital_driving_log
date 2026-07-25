import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { CarService } from '../../services/car.service';
import { CalculationService } from '../../services/calculation.service';
import { CarDto } from '../../models/cars';
import { FactorRow } from '../../models/calculations';
import { selectableYears } from '../../utils/selectable-years';

@Component({
  selector: 'app-distribution-factors',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TranslateModule],
  templateUrl: './distribution-factors.component.html',
})
export class DistributionFactorsComponent implements OnInit {
  private readonly carService = inject(CarService);
  private readonly calculationService = inject(CalculationService);

  readonly cars = signal<CarDto[]>([]);
  readonly years: number[] = selectableYears();
  carId = 0;
  year = new Date().getFullYear();

  readonly rows = signal<FactorRow[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');

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
    this.calculationService.getFactors(this.carId, this.year).subscribe({
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
  }
}
