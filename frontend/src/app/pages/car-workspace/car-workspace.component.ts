import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { switchMap } from 'rxjs';
import { CarService } from '../../services/car.service';
import { UserService } from '../../services/user.service';
import { DriveService } from '../../services/drive.service';
import { CostService } from '../../services/cost.service';
import { CarDto } from '../../models/cars';
import { UserDto } from '../../models/users';
import { DriveDto, CreateDriveRequest } from '../../models/drives';
import { CostDto, CreateCostRequest, CostType } from '../../models/costs';
import { extractApiErrorMessage } from '../../models/api-error';
import { HttpErrorResponse } from '@angular/common/http';
import { FieldErrorComponent } from '../../components/field-error/field-error.component';

/** Number of rows shown per page in the drives / transactions tables. */
const PAGE_SIZE = 10;

/** A drive row enriched with the distance derived from consecutive odometer readings. */
interface DriveRow {
  drive: DriveDto;
  /** Distance since the previous (older) drive on this page, or null when unknown. */
  distance: number | null;
}

@Component({
  selector: 'app-car-workspace',
  standalone: true,
  imports: [ReactiveFormsModule, TranslateModule, DecimalPipe, FieldErrorComponent],
  templateUrl: './car-workspace.component.html',
  styleUrl: './car-workspace.component.scss',
})
export class CarWorkspaceComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly carService = inject(CarService);
  private readonly userService = inject(UserService);
  private readonly driveService = inject(DriveService);
  private readonly costService = inject(CostService);

  readonly car = signal<CarDto | null>(null);
  readonly users = signal<UserDto[]>([]);
  readonly drives = signal<DriveDto[]>([]);
  readonly costs = signal<CostDto[]>([]);
  readonly loading = signal(true);
  readonly message = signal('');
  readonly error = signal('');

  // Zero-based page indices and total page counts, tracked per table.
  readonly drivesPage = signal(0);
  readonly drivesTotalPages = signal(0);
  readonly costsPage = signal(0);
  readonly costsTotalPages = signal(0);

  readonly driveForm = this.fb.group({
    driveDate: [new Date().toISOString().slice(0, 10), Validators.required],
    odometer: [0, [Validators.required, Validators.min(1)]],
    driverId: [0, [Validators.required, Validators.min(1)]],
    notes: [''],
    includeFuel: [false],
    fuelPrice: [0],
    fuelQuantity: [0],
    fuelDate: [new Date().toISOString().slice(0, 10)],
    fuelBuyerId: [0],
    fuelNotes: [''],
  });

  readonly transactionForm = this.fb.group({
    buyerId: [0, [Validators.required, Validators.min(1)]],
    description: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(63)]],
    price: [0, [Validators.required, Validators.min(0)]],
    quantity: [1, [Validators.required, Validators.min(1)]],
    dayOfTransaction: [new Date().toISOString().slice(0, 10), Validators.required],
    costType: ['VARIABLE' as CostType, Validators.required],
    notes: [''],
  });

  readonly userLabelById = computed(() => {
    const map = new Map<number, string>();
    this.users().forEach((user) => {
      map.set(user.userId, `${user.firstname} ${user.lastname}`.trim());
    });
    return map;
  });

  /**
   * Enriches each drive with a driven distance. Drives arrive sorted by date
   * descending, so a row's distance is its odometer minus the next (older) row's.
   * The oldest row on the page has no in-page predecessor, so its distance is null.
   */
  readonly driveRows = computed<DriveRow[]>(() => {
    const drives = this.drives();
    return drives.map((drive, index) => {
      const previous = drives[index + 1];
      const distance = previous ? drive.odometer - previous.odometer : null;
      return { drive, distance };
    });
  });

  ngOnInit(): void {
    this.driveForm.controls.includeFuel.valueChanges.subscribe((includeFuel) => {
      this.setFuelValidatorsEnabled(!!includeFuel);
    });

    this.route.paramMap
      .pipe(
        switchMap((params) => {
          const carId = Number(params.get('carId'));
          return this.carService.getCarById(carId);
        }),
      )
      .subscribe({
        next: (car) => {
          this.car.set(car);
          this.loadUsersAndEntries(car.carId);
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(extractApiErrorMessage(err, 'carWorkspace.loadError'));
          this.loading.set(false);
        },
      });
  }

  /** The fuel sub-fields mirror CreateCostRequest's own constraints, but only when fuel is actually being logged. */
  private setFuelValidatorsEnabled(enabled: boolean): void {
    const { fuelBuyerId, fuelPrice, fuelQuantity, fuelDate } = this.driveForm.controls;
    fuelBuyerId.setValidators(enabled ? [Validators.required, Validators.min(1)] : []);
    fuelPrice.setValidators(enabled ? [Validators.required, Validators.min(0)] : []);
    fuelQuantity.setValidators(enabled ? [Validators.required, Validators.min(1)] : []);
    fuelDate.setValidators(enabled ? [Validators.required] : []);
    fuelBuyerId.updateValueAndValidity();
    fuelPrice.updateValueAndValidity();
    fuelQuantity.updateValueAndValidity();
    fuelDate.updateValueAndValidity();
  }

  private loadUsersAndEntries(carId: number): void {
    this.userService.getUsers().subscribe({
      next: (users) => {
        this.users.set(users);
        if (users.length > 0) {
          this.driveForm.patchValue({
            driverId: users[0].userId,
            fuelBuyerId: users[0].userId,
          });
          this.transactionForm.patchValue({ buyerId: users[0].userId });
        }

        this.reloadEntries(carId);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(extractApiErrorMessage(err, 'carWorkspace.loadError'));
        this.loading.set(false);
      },
    });
  }

  reloadEntries(carId: number): void {
    this.loading.set(true);

    this.driveService
      .getDrivesForCar(carId, { page: this.drivesPage(), size: PAGE_SIZE, sort: 'driveDate,desc' })
      .subscribe({
        next: (drivePage) => {
          this.drives.set(drivePage.content);
          this.drivesTotalPages.set(drivePage.totalPages);

          this.costService
            .getCostsForCar(carId, {
              page: this.costsPage(),
              size: PAGE_SIZE,
              sort: 'dayOfTransaction,desc',
            })
            .subscribe({
              next: (costPage) => {
                this.costs.set(costPage.content);
                this.costsTotalPages.set(costPage.totalPages);
                this.loading.set(false);
              },
              error: (err: HttpErrorResponse) => {
                this.error.set(extractApiErrorMessage(err, 'carWorkspace.loadError'));
                this.loading.set(false);
              },
            });
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(extractApiErrorMessage(err, 'carWorkspace.loadError'));
          this.loading.set(false);
        },
      });
  }

  changeDrivesPage(delta: number): void {
    const carId = this.car()?.carId;
    if (!carId) {
      return;
    }
    const next = this.drivesPage() + delta;
    if (next < 0 || next >= this.drivesTotalPages()) {
      return;
    }
    this.drivesPage.set(next);
    this.reloadEntries(carId);
  }

  changeCostsPage(delta: number): void {
    const carId = this.car()?.carId;
    if (!carId) {
      return;
    }
    const next = this.costsPage() + delta;
    if (next < 0 || next >= this.costsTotalPages()) {
      return;
    }
    this.costsPage.set(next);
    this.reloadEntries(carId);
  }

  saveDrive(): void {
    this.message.set('');
    this.error.set('');

    const carId = this.car()?.carId;
    if (!carId || this.driveForm.invalid) {
      this.driveForm.markAllAsTouched();
      if (this.driveForm.invalid) {
        this.error.set('carWorkspace.messages.driveFormInvalid');
      }
      return;
    }

    const value = this.driveForm.getRawValue();
    const request: CreateDriveRequest = {
      carId,
      odometer: Number(value.odometer),
      driverId: Number(value.driverId),
      driveDate: value.driveDate ?? new Date().toISOString().slice(0, 10),
      notes: value.notes ?? undefined,
    };

    this.driveService.createDrive(request).subscribe({
      next: () => {
        if (value.includeFuel) {
          const fuelRequest: CreateCostRequest = {
            carId,
            buyerId: Number(value.fuelBuyerId),
            description: 'Fuel',
            price: Number(value.fuelPrice),
            quantity: Number(value.fuelQuantity),
            dayOfTransaction: value.fuelDate ?? new Date().toISOString().slice(0, 10),
            costType: 'VARIABLE',
            notes: value.fuelNotes ?? undefined,
          };

          this.costService.createCost(fuelRequest).subscribe({
            next: () => {
              this.message.set('carWorkspace.messages.driveAndFuelSaved');
              this.resetDriveForm();
              this.resetToFirstPage();
              this.reloadEntries(carId);
            },
            error: (err: HttpErrorResponse) => {
              this.error.set(extractApiErrorMessage(err, 'carWorkspace.messages.fuelSaveFailed'));
            },
          });
          return;
        }

        this.message.set('carWorkspace.messages.driveSaved');
        this.resetDriveForm();
        this.resetToFirstPage();
        this.reloadEntries(carId);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(extractApiErrorMessage(err, 'carWorkspace.messages.driveSaveFailed'));
      },
    });
  }

  saveTransaction(): void {
    this.message.set('');
    this.error.set('');

    const carId = this.car()?.carId;
    if (!carId || this.transactionForm.invalid) {
      this.transactionForm.markAllAsTouched();
      if (this.transactionForm.invalid) {
        this.error.set('carWorkspace.messages.transactionFormInvalid');
      }
      return;
    }

    const value = this.transactionForm.getRawValue();
    const request: CreateCostRequest = {
      carId,
      buyerId: Number(value.buyerId),
      description: value.description ?? '',
      price: Number(value.price),
      quantity: Number(value.quantity),
      dayOfTransaction: value.dayOfTransaction ?? new Date().toISOString().slice(0, 10),
      costType: (value.costType ?? 'VARIABLE') as CostType,
      notes: value.notes ?? undefined,
    };

    this.costService.createCost(request).subscribe({
      next: () => {
        this.message.set('carWorkspace.messages.transactionSaved');
        this.transactionForm.reset({
          buyerId: this.users()[0]?.userId ?? 0,
          description: '',
          price: 0,
          quantity: 1,
          dayOfTransaction: new Date().toISOString().slice(0, 10),
          costType: 'VARIABLE',
          notes: '',
        });
        this.resetToFirstPage();
        this.reloadEntries(carId);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(extractApiErrorMessage(err, 'carWorkspace.messages.transactionSaveFailed'));
      },
    });
  }

  private resetToFirstPage(): void {
    this.drivesPage.set(0);
    this.costsPage.set(0);
  }

  private resetDriveForm(): void {
    this.driveForm.reset({
      driveDate: new Date().toISOString().slice(0, 10),
      odometer: 0,
      driverId: this.users()[0]?.userId ?? 0,
      notes: '',
      includeFuel: false,
      fuelPrice: 0,
      fuelQuantity: 0,
      fuelDate: new Date().toISOString().slice(0, 10),
      fuelBuyerId: this.users()[0]?.userId ?? 0,
      fuelNotes: '',
    });
  }
}
