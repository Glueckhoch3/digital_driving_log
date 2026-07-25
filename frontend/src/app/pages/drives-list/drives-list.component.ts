import { Component, computed, inject, input, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { DriveService } from '../../services/drive.service';
import { CarDto } from '../../models/cars';
import { UserDto } from '../../models/users';
import { DriveDto } from '../../models/drives';
import { extractApiErrorMessage } from '../../models/api-error';

/** Number of rows shown per page in the drives table. */
const PAGE_SIZE = 10;

/** A drive row enriched with the distance derived from consecutive odometer readings. */
interface DriveRow {
  drive: DriveDto;
  /** Distance since the previous (lower-odometer) drive on this page, or null when unknown. */
  distance: number | null;
}

@Component({
  selector: 'app-drives-list',
  standalone: true,
  imports: [RouterLink, TranslateModule],
  templateUrl: './drives-list.component.html',
  styleUrl: './drives-list.component.scss',
})
export class DrivesListComponent {
  private readonly driveService = inject(DriveService);

  readonly car = input.required<CarDto>();
  readonly users = input.required<UserDto[]>();

  /** Zero-based page index. */
  readonly page = signal(0);

  private readonly drivesResource = rxResource({
    params: () => ({ carId: this.car().carId, page: this.page() }),
    stream: ({ params }) =>
      this.driveService.getDrivesForCar(params.carId, {
        page: params.page,
        size: PAGE_SIZE,
        sort: ['odometer,desc', 'driveDate,desc'],
      }),
  });

  readonly loading = this.drivesResource.isLoading;
  readonly totalPages = computed(() => this.drivesResource.value()?.totalPages ?? 0);

  readonly errorMessage = computed(() => {
    const err = this.drivesResource.error();
    return err ? extractApiErrorMessage(err as HttpErrorResponse, 'carWorkspace.loadError') : '';
  });

  readonly userLabelById = computed(() => {
    const map = new Map<number, string>();
    this.users().forEach((user) => {
      map.set(user.userId, `${user.firstname} ${user.lastname}`.trim());
    });
    return map;
  });

  /**
   * Enriches each drive with a driven distance. Drives arrive sorted by odometer
   * descending (date descending as tie-breaker), so a row's distance is its
   * odometer minus the next row's. The last row on the page has no in-page
   * predecessor, so its distance is null.
   */
  readonly driveRows = computed<DriveRow[]>(() => {
    const drives = this.drivesResource.value()?.content ?? [];
    return drives.map((drive, index) => {
      const previous = drives[index + 1];
      const distance = previous ? drive.odometer - previous.odometer : null;
      return { drive, distance };
    });
  });

  changePage(delta: number): void {
    const next = this.page() + delta;
    if (next < 0 || next >= this.totalPages()) {
      return;
    }
    this.page.set(next);
  }
}
