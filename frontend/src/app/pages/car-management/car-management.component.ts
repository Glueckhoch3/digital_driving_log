import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { CarService } from '../../services/car.service';
import { UserService } from '../../services/user.service';
import { CarDto, CreateCarRequest, UpdateCarRequest } from '../../models/cars';
import { UserDto } from '../../models/users';

@Component({
  selector: 'app-car-management',
  standalone: true,
  imports: [ReactiveFormsModule, TranslateModule],
  templateUrl: './car-management.component.html',
  styleUrl: './car-management.component.scss',
})
export class CarManagementComponent implements OnInit {
  private readonly carService = inject(CarService);
  private readonly userService = inject(UserService);
  private readonly fb = inject(FormBuilder);

  readonly cars = signal<CarDto[]>([]);
  readonly users = signal<UserDto[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly message = signal('');
  readonly editingCarId = signal<number | null>(null);

  readonly ownerLabelById = computed(() => {
    const map = new Map<number, string>();
    this.users().forEach((user) => {
      map.set(user.userId, `${user.firstname} ${user.lastname}`.trim());
    });
    return map;
  });

  readonly carForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(50)]],
    plateNumber: ['', [Validators.required, Validators.maxLength(15)]],
    ownerId: [0, [Validators.required, Validators.min(1)]],
    data: [''],
  });

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading.set(true);
    this.error.set('');

    this.userService.getUsers().subscribe({
      next: (users) => {
        this.users.set(users);

        const ownerId = Number(this.carForm.value.ownerId ?? 0);
        if (users.length > 0 && ownerId < 1) {
          this.carForm.patchValue({ ownerId: users[0].userId });
        }

        this.carService.getCars().subscribe({
          next: (cars) => {
            this.cars.set(cars);
            this.loading.set(false);
          },
          error: () => {
            this.error.set('carManagement.messages.loadFailed');
            this.loading.set(false);
          },
        });
      },
      error: () => {
        this.error.set('carManagement.messages.loadFailed');
        this.loading.set(false);
      },
    });
  }

  submit(): void {
    if (this.users().length === 0) {
      this.error.set('carManagement.messages.usersRequiredFirst');
      return;
    }

    if (this.carForm.invalid) {
      this.carForm.markAllAsTouched();
      return;
    }

    const form = this.carForm.getRawValue();
    const payload: CreateCarRequest = {
      name: form.name ?? '',
      plateNumber: form.plateNumber ?? '',
      ownerId: Number(form.ownerId),
      data: form.data || undefined,
    };

    const editingId = this.editingCarId();
    if (editingId == null) {
      this.carService.createCar(payload).subscribe({
        next: () => {
          this.message.set('carManagement.messages.createSuccess');
          this.error.set('');
          this.resetForm();
          this.loadData();
        },
        error: () => {
          this.error.set('carManagement.messages.createFailed');
        },
      });
      return;
    }

    const updatePayload: UpdateCarRequest = payload;
    this.carService.updateCar(editingId, updatePayload).subscribe({
      next: () => {
        this.message.set('carManagement.messages.updateSuccess');
        this.error.set('');
        this.resetForm();
        this.loadData();
      },
      error: () => {
        this.error.set('carManagement.messages.updateFailed');
      },
    });
  }

  edit(car: CarDto): void {
    this.editingCarId.set(car.carId);
    this.carForm.patchValue({
      name: car.name,
      plateNumber: car.plateNumber,
      ownerId: car.ownerId ?? 0,
      data: car.data ?? '',
    });
  }

  cancelEdit(): void {
    this.resetForm();
  }

  delete(carId: number): void {
    this.carService.deleteCar(carId).subscribe({
      next: () => {
        this.message.set('carManagement.messages.deleteSuccess');
        this.error.set('');
        this.loadData();
      },
      error: (err) => {
        this.error.set(
          err?.status === 409
            ? 'carManagement.messages.deleteDependencyBlocked'
            : 'carManagement.messages.deleteFailed',
        );
      },
    });
  }

  private resetForm(): void {
    this.editingCarId.set(null);
    this.carForm.reset({
      name: '',
      plateNumber: '',
      ownerId: this.users()[0]?.userId ?? 0,
      data: '',
    });
  }
}
