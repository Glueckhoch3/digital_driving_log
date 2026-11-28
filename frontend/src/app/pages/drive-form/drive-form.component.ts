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
import { IconComponent } from '../../components/icon/icon.component';
import { HttpErrorResponse } from '@angular/common/http';
import { DriveService } from '../../services/drive.service';
import { CarDto } from '../../models/cars';
import { UserDto } from '../../models/users';
import { CreateDriveRequest } from '../../models/drives';
import { extractApiErrorMessage } from '../../models/api-error';
import { FieldErrorComponent } from '../../components/field-error/field-error.component';

@Component({
  selector: 'app-drive-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TranslateModule, FieldErrorComponent, IconComponent],
  templateUrl: './drive-form.component.html',
  styleUrl: './drive-form.component.scss',
})
export class DriveFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly driveService = inject(DriveService);
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
  });

  constructor() {
    effect(() => {
      if (this.message()) {
        this.statusRegion()?.nativeElement.focus();
      }
    });
  }

  ngOnInit(): void {
    const firstUser = this.users()[0];
    if (firstUser) {
      this.driveForm.patchValue({ driverId: firstUser.userId });
    }
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
    });
  }
}
