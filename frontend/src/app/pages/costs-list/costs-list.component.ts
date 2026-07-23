import { Component, computed, inject, input, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { CostService } from '../../services/cost.service';
import { CarDto } from '../../models/cars';
import { UserDto } from '../../models/users';
import { extractApiErrorMessage } from '../../models/api-error';

/** Number of rows shown per page in the transactions table. */
const PAGE_SIZE = 10;

@Component({
  selector: 'app-costs-list',
  standalone: true,
  imports: [RouterLink, TranslateModule, DecimalPipe],
  templateUrl: './costs-list.component.html',
  styleUrl: './costs-list.component.scss',
})
export class CostsListComponent {
  private readonly costService = inject(CostService);

  readonly car = input.required<CarDto>();
  readonly users = input.required<UserDto[]>();

  /** Zero-based page index. */
  readonly page = signal(0);

  private readonly costsResource = rxResource({
    params: () => ({ carId: this.car().carId, page: this.page() }),
    stream: ({ params }) =>
      this.costService.getCostsForCar(params.carId, {
        page: params.page,
        size: PAGE_SIZE,
        sort: 'dayOfTransaction,desc',
      }),
  });

  readonly loading = this.costsResource.isLoading;
  readonly totalPages = computed(() => this.costsResource.value()?.totalPages ?? 0);
  readonly costs = computed(() => this.costsResource.value()?.content ?? []);

  readonly errorMessage = computed(() => {
    const err = this.costsResource.error();
    return err ? extractApiErrorMessage(err as HttpErrorResponse, 'carWorkspace.loadError') : '';
  });

  readonly userLabelById = computed(() => {
    const map = new Map<number, string>();
    this.users().forEach((user) => {
      map.set(user.userId, `${user.firstname} ${user.lastname}`.trim());
    });
    return map;
  });

  changePage(delta: number): void {
    const next = this.page() + delta;
    if (next < 0 || next >= this.totalPages()) {
      return;
    }
    this.page.set(next);
  }
}
