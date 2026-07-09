import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { CarService } from '../../services/car.service';
import { CarDto } from '../../models/cars';

@Component({
  selector: 'app-car-selection',
  standalone: true,
  imports: [RouterLink, TranslateModule],
  templateUrl: './car-selection.component.html',
  styleUrl: './car-selection.component.scss'
})
export class CarSelectionComponent implements OnInit {
  private readonly carService = inject(CarService);

  readonly cars = signal<CarDto[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    this.carService.getCars().subscribe({
      next: cars => {
        this.cars.set(cars);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('carSelection.loadError');
        this.loading.set(false);
      }
    });
  }
}
