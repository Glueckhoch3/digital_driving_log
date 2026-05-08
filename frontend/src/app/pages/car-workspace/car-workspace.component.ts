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

@Component({
  selector: 'app-car-workspace',
  standalone: true,
  imports: [ReactiveFormsModule, TranslateModule, DecimalPipe],
  templateUrl: './car-workspace.component.html',
  styleUrl: './car-workspace.component.scss'
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

  readonly driveForm = this.fb.group({
    driveDate: [new Date().toISOString().slice(0, 10), Validators.required],
    currentMileage: [0, [Validators.required, Validators.min(0)]],
    driverId: [0, [Validators.required, Validators.min(1)]],
    notes: [''],
    includeFuel: [false],
    fuelPrice: [0],
    fuelAmount: [0],
    fuelDate: [new Date().toISOString().slice(0, 10)],
    fuelBuyerId: [0],
    fuelNotes: ['']
  });

  readonly transactionForm = this.fb.group({
    buyerId: [0, [Validators.required, Validators.min(1)]],
    transactionObject: ['', [Validators.required, Validators.minLength(2)]],
    price: [0, [Validators.required, Validators.min(0)]],
    amount: [1, [Validators.required, Validators.min(1)]],
    dayOfTransaction: [new Date().toISOString().slice(0, 10), Validators.required],
    costType: ['variable', Validators.required],
    notes: ['']
  });

  readonly userLabelById = computed(() => {
    const map = new Map<number, string>();
    this.users().forEach(user => {
      map.set(user.userId, `${user.firstname} ${user.lastname}`.trim());
    });
    return map;
  });

  ngOnInit(): void {
    this.route.paramMap.pipe(
      switchMap(params => {
        const carId = Number(params.get('carId'));
        return this.carService.getCarById(carId);
      })
    ).subscribe({
      next: car => {
        this.car.set(car);
        this.loadUsersAndEntries(car.carId);
      },
      error: () => {
        this.error.set('carWorkspace.loadError');
        this.loading.set(false);
      }
    });
  }

  private loadUsersAndEntries(carId: number): void {
    this.userService.getUsers().subscribe({
      next: users => {
        this.users.set(users);
        if (users.length > 0) {
          this.driveForm.patchValue({
            driverId: users[0].userId,
            fuelBuyerId: users[0].userId
          });
          this.transactionForm.patchValue({ buyerId: users[0].userId });
        }

        this.reloadEntries(carId);
      },
      error: () => {
        this.error.set('carWorkspace.loadError');
        this.loading.set(false);
      }
    });
  }

  reloadEntries(carId: number): void {
    this.loading.set(true);

    this.driveService.getDrivesForCar(carId).subscribe({
      next: drives => {
        this.drives.set(drives);

        this.costService.getCostsForCar(carId).subscribe({
          next: costs => {
            this.costs.set(costs);
            this.loading.set(false);
          },
          error: () => {
            this.error.set('carWorkspace.loadError');
            this.loading.set(false);
          }
        });
      },
      error: () => {
        this.error.set('carWorkspace.loadError');
        this.loading.set(false);
      }
    });
  }

  saveDrive(): void {
    const carId = this.car()?.carId;
    if (!carId || this.driveForm.invalid) {
      this.driveForm.markAllAsTouched();
      return;
    }

    const value = this.driveForm.getRawValue();
    const request: CreateDriveRequest = {
      carId,
      currentMileage: Number(value.currentMileage),
      driverId: Number(value.driverId),
      driveDate: value.driveDate ?? new Date().toISOString().slice(0, 10),
      notes: value.notes ?? undefined
    };

    this.driveService.createDrive(request).subscribe({
      next: () => {
        if (value.includeFuel) {
          const fuelRequest: CreateCostRequest = {
            carId,
            buyerId: Number(value.fuelBuyerId),
            transactionObject: 'Fuel',
            price: Number(value.fuelPrice),
            amount: Number(value.fuelAmount),
            dayOfTransaction: value.fuelDate ?? new Date().toISOString().slice(0, 10),
            costType: 'variable',
            notes: value.fuelNotes ?? undefined
          };

          this.costService.createCost(fuelRequest).subscribe({
            next: () => {
              this.message.set('carWorkspace.messages.driveAndFuelSaved');
              this.resetDriveForm();
              this.reloadEntries(carId);
            },
            error: () => {
              this.error.set('carWorkspace.messages.fuelSaveFailed');
            }
          });
          return;
        }

        this.message.set('carWorkspace.messages.driveSaved');
        this.resetDriveForm();
        this.reloadEntries(carId);
      },
      error: () => {
        this.error.set('carWorkspace.messages.driveSaveFailed');
      }
    });
  }

  saveTransaction(): void {
    const carId = this.car()?.carId;
    if (!carId || this.transactionForm.invalid) {
      this.transactionForm.markAllAsTouched();
      return;
    }

    const value = this.transactionForm.getRawValue();
    const request: CreateCostRequest = {
      carId,
      buyerId: Number(value.buyerId),
      transactionObject: value.transactionObject ?? '',
      price: Number(value.price),
      amount: Number(value.amount),
      dayOfTransaction: value.dayOfTransaction ?? new Date().toISOString().slice(0, 10),
      costType: (value.costType ?? 'variable') as CostType,
      notes: value.notes ?? undefined
    };

    this.costService.createCost(request).subscribe({
      next: () => {
        this.message.set('carWorkspace.messages.transactionSaved');
        this.transactionForm.reset({
          buyerId: this.users()[0]?.userId ?? 0,
          transactionObject: '',
          price: 0,
          amount: 1,
          dayOfTransaction: new Date().toISOString().slice(0, 10),
          costType: 'variable',
          notes: ''
        });
        this.reloadEntries(carId);
      },
      error: () => {
        this.error.set('carWorkspace.messages.transactionSaveFailed');
      }
    });
  }

  private resetDriveForm(): void {
    this.driveForm.reset({
      driveDate: new Date().toISOString().slice(0, 10),
      currentMileage: 0,
      driverId: this.users()[0]?.userId ?? 0,
      notes: '',
      includeFuel: false,
      fuelPrice: 0,
      fuelAmount: 0,
      fuelDate: new Date().toISOString().slice(0, 10),
      fuelBuyerId: this.users()[0]?.userId ?? 0,
      fuelNotes: ''
    });
  }
}
