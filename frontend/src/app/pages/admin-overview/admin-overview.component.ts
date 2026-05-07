import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { CarService } from '../../services/car.service';
import { DriveService } from '../../services/drive.service';
import { CostService } from '../../services/cost.service';
import { CarDto } from '../../models/cars';
import { DriveDto } from '../../models/drives';
import { CostDto } from '../../models/costs';

interface AdminCarOverview {
  car: CarDto;
  drives: DriveDto[];
  costs: CostDto[];
}

@Component({
  selector: 'app-admin-overview',
  standalone: true,
  imports: [TranslateModule, DecimalPipe],
  templateUrl: './admin-overview.component.html',
  styleUrl: './admin-overview.component.scss'
})
export class AdminOverviewComponent implements OnInit {
  private readonly carService = inject(CarService);
  private readonly driveService = inject(DriveService);
  private readonly costService = inject(CostService);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly overviews = signal<AdminCarOverview[]>([]);

  ngOnInit(): void {
    this.carService.getCars().subscribe({
      next: cars => {
        if (cars.length === 0) {
          this.overviews.set([]);
          this.loading.set(false);
          return;
        }

        const requests = cars.map(car =>
          forkJoin({
            drives: this.driveService.getDrivesForCar(car.carId),
            costs: this.costService.getCostsForCar(car.carId)
          })
        );

        forkJoin(requests).subscribe({
          next: results => {
            this.overviews.set(results.map((result, index) => ({
              car: cars[index],
              drives: result.drives,
              costs: result.costs
            })));
            this.loading.set(false);
          },
          error: () => {
            this.error.set('adminOverview.loadError');
            this.loading.set(false);
          }
        });
      },
      error: () => {
        this.error.set('adminOverview.loadError');
        this.loading.set(false);
      }
    });
  }

  totalCost(costs: CostDto[]): number {
    return costs.reduce((sum, cost) => sum + Number(cost.price), 0);
  }
}
