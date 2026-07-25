import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { DriveFormComponent } from './drive-form.component';
import { DriveService } from '../../services/drive.service';
import { CostService } from '../../services/cost.service';
import { CarDto } from '../../models/cars';
import { UserDto } from '../../models/users';
import { DriveDto } from '../../models/drives';
import { CostDto } from '../../models/costs';

describe('DriveFormComponent', () => {
  let fixture: ComponentFixture<DriveFormComponent>;
  let component: DriveFormComponent;
  let driveService: Mocked<Pick<DriveService, 'createDrive'>>;
  let costService: Mocked<Pick<CostService, 'createCost'>>;

  const car: CarDto = { carId: 5, name: 'City Car', plateNumber: 'M-AB-1', ownerId: 1, data: null };
  const users: UserDto[] = [
    { userId: 1, firstname: 'Anna', lastname: 'Meyer', driverLicense: true, birthday: null },
  ];

  beforeEach(async () => {
    driveService = { createDrive: vi.fn().mockReturnValue(of({} as DriveDto)) };
    costService = { createCost: vi.fn().mockReturnValue(of({} as CostDto)) };

    await TestBed.configureTestingModule({
      imports: [DriveFormComponent],
      providers: [
        provideTranslateService(),
        provideRouter([]),
        { provide: DriveService, useValue: driveService },
        { provide: CostService, useValue: costService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DriveFormComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('car', car);
    fixture.componentRef.setInput('users', users);
    fixture.detectChanges();
  });

  it('rejects odometer = 0 client-side', () => {
    component.driveForm.setValue({
      driveDate: '2025-01-01',
      odometer: 0,
      driverId: 1,
      notes: '',
      includeFuel: false,
      fuelPrice: '0',
      fuelQuantity: '0',
      fuelDate: '2025-01-01',
      fuelBuyerId: 1,
      fuelNotes: '',
    });

    component.saveDrive();
    fixture.detectChanges();

    expect(driveService.createDrive).not.toHaveBeenCalled();
    expect(component.error()).toBe('carWorkspace.messages.driveFormInvalid');
    expect(fixture.nativeElement.textContent).toContain('carWorkspace.messages.driveFormInvalid');
  });

  it('accepts odometer = 1 and saves the drive', () => {
    component.driveForm.setValue({
      driveDate: '2025-01-01',
      odometer: 1,
      driverId: 1,
      notes: '',
      includeFuel: false,
      fuelPrice: '0',
      fuelQuantity: '0',
      fuelDate: '2025-01-01',
      fuelBuyerId: 1,
      fuelNotes: '',
    });

    component.saveDrive();

    expect(driveService.createDrive).toHaveBeenCalledWith(
      expect.objectContaining({ carId: 5, odometer: 1, driverId: 1 }),
    );
    expect(component.message()).toBe('carWorkspace.messages.driveSaved');
  });

  it('requires fuel fields only when includeFuel is checked', () => {
    component.driveForm.setValue({
      driveDate: '2025-01-01',
      odometer: 10,
      driverId: 1,
      notes: '',
      includeFuel: false,
      fuelPrice: '',
      fuelQuantity: '',
      fuelDate: '2025-01-01',
      fuelBuyerId: 1,
      fuelNotes: '',
    });

    expect(component.driveForm.valid).toBe(true);

    component.driveForm.controls.includeFuel.setValue(true);

    expect(component.driveForm.controls.fuelQuantity.invalid).toBe(true);
    expect(component.driveForm.valid).toBe(false);
  });

  it('clears a stale drive-saved message on the next invalid submission', () => {
    component.message.set('carWorkspace.messages.driveSaved');
    component.driveForm.setValue({
      driveDate: '',
      odometer: 0,
      driverId: 0,
      notes: '',
      includeFuel: false,
      fuelPrice: '0',
      fuelQuantity: '0',
      fuelDate: '2025-01-01',
      fuelBuyerId: 1,
      fuelNotes: '',
    });

    component.saveDrive();
    fixture.detectChanges();

    expect(component.message()).toBe('');
    expect(fixture.nativeElement.textContent).not.toContain('carWorkspace.messages.driveSaved');
  });

  it('surfaces the backend detail message when saving a drive fails', () => {
    driveService.createDrive.mockReturnValue(
      throwError(() => ({ status: 400, error: { detail: 'odometer must be greater than 0' } })),
    );
    component.driveForm.setValue({
      driveDate: '2025-01-01',
      odometer: 1,
      driverId: 1,
      notes: '',
      includeFuel: false,
      fuelPrice: '0',
      fuelQuantity: '0',
      fuelDate: '2025-01-01',
      fuelBuyerId: 1,
      fuelNotes: '',
    });

    component.saveDrive();

    expect(component.error()).toBe('odometer must be greater than 0');
  });
});
