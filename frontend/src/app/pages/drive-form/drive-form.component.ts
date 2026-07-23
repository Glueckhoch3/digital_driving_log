import {
  Component,
  ElementRef,
  Injector,
  OnInit,
  afterNextRender,
  effect,
  inject,
  input,
  signal,
  viewChild,
} from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { HttpErrorResponse } from '@angular/common/http';
import { DriveService } from '../../services/drive.service';
import { CostService } from '../../services/cost.service';
import { CarDto } from '../../models/cars';
import { UserDto } from '../../models/users';
import { CreateDriveRequest } from '../../models/drives';
import { CreateCostRequest } from '../../models/costs';
import { extractApiErrorMessage } from '../../models/api-error';
import { FieldErrorComponent } from '../../components/field-error/field-error.component';

@Component({
  selector: 'app-drive-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TranslateModule, FieldErrorComponent],
  templateUrl: './drive-form.component.html',
  styleUrl: './drive-form.component.scss',
})
export class DriveFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly driveService = inject(DriveService);
  private readonly costService = inject(CostService);
  private readonly injector = inject(Injector);

  readonly car = input.required<CarDto>();
  readonly users = input.required<UserDto[]>();

  readonly message = signal('');
  readonly error = signal('');

  readonly formEl = viewChild<ElementRef<HTMLFormElement>>('driveFormEl');
  readonly statusRegion = viewChild<ElementRef<HTMLElement>>('statusRegion');

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

  constructor() {
    effect(() => {
      if (this.message()) {
        this.statusRegion()?.nativeElement.focus();
      }
    });
  }

  ngOnInit(): void {
    this.driveForm.controls.includeFuel.valueChanges.subscribe((includeFuel) => {
      this.setFuelValidatorsEnabled(!!includeFuel);
    });

    const firstUser = this.users()[0];
    if (firstUser) {
      this.driveForm.patchValue({ driverId: firstUser.userId, fuelBuyerId: firstUser.userId });
    }
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

  private focusFirstInvalidControl(): void {
    afterNextRender(
      () => {
        this.formEl()?.nativeElement.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus();
      },
      { injector: this.injector },
    );
  }

  saveDrive(): void {
    this.message.set('');
    this.error.set('');

    const carId = this.car().carId;
    if (this.driveForm.invalid) {
      this.driveForm.markAllAsTouched();
      this.error.set('carWorkspace.messages.driveFormInvalid');
      this.focusFirstInvalidControl();
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
            },
            error: (err: HttpErrorResponse) => {
              this.error.set(extractApiErrorMessage(err, 'carWorkspace.messages.fuelSaveFailed'));
            },
          });
          return;
        }

        this.message.set('carWorkspace.messages.driveSaved');
        this.resetDriveForm();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(extractApiErrorMessage(err, 'carWorkspace.messages.driveSaveFailed'));
      },
    });
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
