import { Component, OnInit, computed, inject, signal } from '@angular/core';
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

/**
 * Upper bound of per-car entries pulled from each paged endpoint. The combined
 * timeline is then paginated client-side below.
 */
const PER_CAR_FETCH_SIZE = 500;

/** Rows shown per page in the merged timeline. */
const PAGE_SIZE = 20;

@Component({
  selector: 'app-admin-overview',
  standalone: true,
  imports: [TranslateModule],
  templateUrl: './admin-overview.component.html',
  styleUrl: './admin-overview.component.scss',
})
export class AdminOverviewComponent implements OnInit {
  private readonly timelineTypeOrder: Record<TimelineEntryType, number> = {
    transaction: 0,
    drive: 1,
  };

  private readonly carService = inject(CarService);
  private readonly driveService = inject(DriveService);
  private readonly costService = inject(CostService);
  private readonly userService = inject(UserService);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly timelineEntries = signal<ManagementTimelineEntry[]>([]);
  readonly page = signal(0);

  readonly totalPages = computed(() =>
    Math.max(1, Math.ceil(this.timelineEntries().length / PAGE_SIZE)),
  );

  readonly pagedEntries = computed<ManagementTimelineEntry[]>(() => {
    const start = this.page() * PAGE_SIZE;
    return this.timelineEntries().slice(start, start + PAGE_SIZE);
  });

  ngOnInit(): void {
    forkJoin({
      cars: this.carService.getCars(),
      users: this.userService.getUsers(),
    }).subscribe({
      next: ({ cars, users }) => {
        if (cars.length === 0) {
          this.timelineEntries.set([]);
          this.loading.set(false);
          return;
        }

        const userNameById = this.createUserMap(users);

        const requests = cars.map((car) =>
          forkJoin({
            drives: this.driveService.getDrivesForCar(car.carId, { size: PER_CAR_FETCH_SIZE }),
            costs: this.costService.getCostsForCar(car.carId, { size: PER_CAR_FETCH_SIZE }),
          }),
        );

        forkJoin(requests).subscribe({
          next: (results) => {
            const entries = results.flatMap((result, index) =>
              this.buildEntriesForCar(
                cars[index],
                result.drives.content,
                result.costs.content,
                userNameById,
              ),
            );

            entries.sort((left, right) => {
              const dateComparison = right.date.localeCompare(left.date);
              if (dateComparison !== 0) {
                return dateComparison;
              }
              return this.timelineTypeOrder[left.type] - this.timelineTypeOrder[right.type];
            });

            this.timelineEntries.set(entries);
            this.page.set(0);
            this.loading.set(false);
          },
          error: () => {
            this.error.set('managementOverview.timelineLoadError');
            this.loading.set(false);
          },
        });
      },
      error: () => {
        this.error.set('managementOverview.loadError');
        this.loading.set(false);
      },
    });
  }

  changePage(delta: number): void {
    const next = this.page() + delta;
    if (next < 0 || next >= this.totalPages()) {
      return;
    }
    this.page.set(next);
  }

  private createUserMap(users: UserDto[]): Map<number, string> {
    const map = new Map<number, string>();

    users.forEach((user) => {
      map.set(user.userId, `${user.firstname} ${user.lastname}`.trim());
    });

    return map;
  }

  private buildEntriesForCar(
    car: CarDto,
    drives: DriveDto[],
    costs: CostDto[],
    userNameById: Map<number, string>,
  ): ManagementTimelineEntry[] {
    const driveEntries: ManagementTimelineEntry[] = drives.map((drive) => ({
      type: 'drive',
      date: drive.driveDate,
      carName: car.name,
      plateNumber: car.plateNumber,
      participantName: userNameById.get(drive.driverId) ?? `Unknown user #${drive.driverId}`,
      details: `${drive.odometer} km`,
      valueLabel: drive.notes ?? '',
    }));

    const transactionEntries: ManagementTimelineEntry[] = costs.map((cost) => ({
      type: 'transaction',
      date: cost.dayOfTransaction,
      carName: car.name,
      plateNumber: car.plateNumber,
      participantName: userNameById.get(cost.buyerId) ?? `Unknown user #${cost.buyerId}`,
      details: `${cost.description} (${cost.costType})`,
      valueLabel: `${Number(cost.price).toFixed(2)} € x ${cost.quantity}`,
    }));

    return [...driveEntries, ...transactionEntries];
  }
}
