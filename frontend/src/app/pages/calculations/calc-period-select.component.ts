import { Component, OnInit, effect, inject, input, model, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { CarService } from '../../services/car.service';
import { CarDto } from '../../models/cars';
import { selectableYears } from '../../utils/selectable-years';
import { AvailabilityStore } from './availability.store';

/**
 * Shared car / year / (optional) month selector for the calculation screens (issue
 * #32). Renders the orange/dot markers from {@link AvailabilityStore} so all three
 * screens colour their selections the same way from a single API call per car.
 */
@Component({
  selector: 'app-calc-period-select',
  standalone: true,
  imports: [FormsModule, TranslateModule],
  templateUrl: './calc-period-select.component.html',
})
export class CalcPeriodSelectComponent implements OnInit {
  private readonly carService = inject(CarService);
  readonly store = inject(AvailabilityStore);

  readonly showMonth = input(false);

  readonly carId = model.required<number>();
  readonly year = model.required<number>();
  readonly month = model<number>(1);

  readonly cars = signal<CarDto[]>([]);
  readonly years: number[] = selectableYears();
  readonly months = Array.from({ length: 12 }, (_, i) => i + 1);

  constructor() {
    effect(() => {
      const carId = this.carId();
      if (carId) {
        this.store.load(carId);
      }
    });
  }

  ngOnInit(): void {
    this.carService.getCars().subscribe({
      next: (cars) => {
        this.cars.set(cars);
        if (cars.length > 0 && !this.carId()) {
          this.carId.set(cars[0].carId);
        }
      },
    });
  }
}
