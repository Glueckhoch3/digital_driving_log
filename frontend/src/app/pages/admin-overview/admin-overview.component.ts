import { Component, OnInit, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { CarService } from '../../services/car.service';
import { DriveService } from '../../services/drive.service';
import { CostService } from '../../services/cost.service';
import { UserService } from '../../services/user.service';
import { CarDto } from '../../models/cars';
import { DriveDto } from '../../models/drives';
import { CostDto } from '../../models/costs';
import { UserDto } from '../../models/users';

type TimelineEntryType = 'drive' | 'transaction';

interface ManagementTimelineEntry {
  type: TimelineEntryType;
  date: string;
  carName: string;
  plateNumber: string;
  participantName: string;
  details: string;
  valueLabel: string;
}

@Component({
  selector: 'app-admin-overview',
  standalone: true,
  imports: [TranslateModule],
  templateUrl: './admin-overview.component.html',
  styleUrl: './admin-overview.component.scss'
})
export class AdminOverviewComponent implements OnInit {
  private readonly carService = inject(CarService);
  private readonly driveService = inject(DriveService);
  private readonly costService = inject(CostService);
  private readonly userService = inject(UserService);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly timelineEntries = signal<ManagementTimelineEntry[]>([]);

  ngOnInit(): void {
    forkJoin({
      cars: this.carService.getCars(),
      users: this.userService.getUsers()
    }).subscribe({
      next: ({ cars, users }) => {
        if (cars.length === 0) {
          this.timelineEntries.set([]);
          this.loading.set(false);
          return;
        }

        const userNameById = this.createUserMap(users);

        const requests = cars.map(car =>
          forkJoin({
            drives: this.driveService.getDrivesForCar(car.carId),
            costs: this.costService.getCostsForCar(car.carId)
          })
        );

        forkJoin(requests).subscribe({
          next: results => {
            const entries = results.flatMap((result, index) =>
              this.buildEntriesForCar(cars[index], result.drives, result.costs, userNameById)
            );

            const timelineTypeOrder: Record<TimelineEntryType, number> = {
              transaction: 0,
              drive: 1
            };

            entries.sort((left, right) => {
              const dateComparison = right.date.localeCompare(left.date);
              if (dateComparison !== 0) {
                return dateComparison;
              }
              return timelineTypeOrder[left.type] - timelineTypeOrder[right.type];
            });

            this.timelineEntries.set(entries);
            this.loading.set(false);
          },
          error: () => {
            this.error.set('managementOverview.timelineLoadError');
            this.loading.set(false);
          }
        });
      },
      error: () => {
        this.error.set('managementOverview.loadError');
        this.loading.set(false);
      }
    });
  }

  private createUserMap(users: UserDto[]): Map<number, string> {
    const map = new Map<number, string>();

    users.forEach(user => {
      map.set(user.userId, `${user.firstname} ${user.lastname}`.trim());
    });

    return map;
  }

  private buildEntriesForCar(
    car: CarDto,
    drives: DriveDto[],
    costs: CostDto[],
    userNameById: Map<number, string>
  ): ManagementTimelineEntry[] {
    const driveEntries: ManagementTimelineEntry[] = drives.map(drive => ({
      type: 'drive',
      date: drive.driveDate,
      carName: car.name,
      plateNumber: car.plateNumber,
      participantName: userNameById.get(drive.driverId) ?? String(drive.driverId),
      details: `${drive.currentMileage} km (${drive.drivenDistance ?? '-'} km)`,
      valueLabel: drive.notes ?? ''
    }));

    const transactionEntries: ManagementTimelineEntry[] = costs.map(cost => ({
      type: 'transaction',
      date: cost.dayOfTransaction,
      carName: car.name,
      plateNumber: car.plateNumber,
      participantName: userNameById.get(cost.buyerId) ?? String(cost.buyerId),
      details: `${cost.transactionObject} (${cost.costType})`,
      valueLabel: `${Number(cost.price).toFixed(2)} € x ${cost.amount}`
    }));

    return [...driveEntries, ...transactionEntries];
  }
}
