import { TestBed, ComponentFixture } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { CarWorkspaceComponent } from './car-workspace.component';
import { CarService } from '../../services/car.service';
import { UserService } from '../../services/user.service';
import { DriveService } from '../../services/drive.service';
import { CostService } from '../../services/cost.service';
import { CarDto } from '../../models/cars';
import { UserDto } from '../../models/users';
import { Page } from '../../models/page';
import { DriveDto } from '../../models/drives';
import { CostDto } from '../../models/costs';

describe('CarWorkspaceComponent', () => {
  let fixture: ComponentFixture<CarWorkspaceComponent>;
  let component: CarWorkspaceComponent;
  let carService: Mocked<Pick<CarService, 'getCarById'>>;
  let userService: Mocked<Pick<UserService, 'getUsers'>>;
  let driveService: Mocked<Pick<DriveService, 'getDrivesForCar' | 'createDrive'>>;
  let costService: Mocked<Pick<CostService, 'getCostsForCar' | 'createCost'>>;

  const car: CarDto = { carId: 5, name: 'City Car', plateNumber: 'M-AB-1', ownerId: 1, data: null };
  const users: UserDto[] = [
    { userId: 1, firstname: 'Anna', lastname: 'Meyer', driverLicense: true, birthday: null },
  ];
  const emptyDrivePage: Page<DriveDto> = {
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 10,
  };
  const emptyCostPage: Page<CostDto> = {
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 10,
  };

  beforeEach(async () => {
    carService = { getCarById: vi.fn().mockReturnValue(of(car)) };
    userService = { getUsers: vi.fn().mockReturnValue(of(users)) };
    driveService = {
      getDrivesForCar: vi.fn().mockReturnValue(of(emptyDrivePage)),
      createDrive: vi.fn().mockReturnValue(of({} as DriveDto)),
    };
    costService = {
      getCostsForCar: vi.fn().mockReturnValue(of(emptyCostPage)),
      createCost: vi.fn().mockReturnValue(of({} as CostDto)),
    };

    await TestBed.configureTestingModule({
      imports: [CarWorkspaceComponent],
      providers: [
        provideTranslateService(),
        { provide: CarService, useValue: carService },
        { provide: UserService, useValue: userService },
        { provide: DriveService, useValue: driveService },
        { provide: CostService, useValue: costService },
        {
          provide: ActivatedRoute,
          useValue: { paramMap: of(convertToParamMap({ carId: '5' })) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CarWorkspaceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads the car, users and entries on init', () => {
    expect(carService.getCarById).toHaveBeenCalledWith(5);
    expect(component.car()).toEqual(car);
    expect(component.users()).toEqual(users);
    expect(component.loading()).toBe(false);
  });

  it('rejects odometer = 0 client-side', () => {
    component.driveForm.setValue({
      driveDate: '2025-01-01',
      odometer: 0,
      driverId: 1,
      notes: '',
      includeFuel: false,
      fuelPrice: 0,
      fuelQuantity: 0,
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
      fuelPrice: 0,
      fuelQuantity: 0,
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
      fuelPrice: 0,
      fuelQuantity: 0,
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
      fuelPrice: 0,
      fuelQuantity: 0,
      fuelDate: '2025-01-01',
      fuelBuyerId: 1,
      fuelNotes: '',
    });

    component.saveDrive();
    fixture.detectChanges();

    expect(component.message()).toBe('');
    expect(fixture.nativeElement.textContent).not.toContain('carWorkspace.messages.driveSaved');
  });

  it('does not submit an invalid transaction form', () => {
    component.transactionForm.setValue({
      buyerId: 0,
      description: '',
      price: 0,
      quantity: 1,
      dayOfTransaction: '2025-01-01',
      costType: 'VARIABLE',
      notes: '',
    });

    component.saveTransaction();
    fixture.detectChanges();

    expect(costService.createCost).not.toHaveBeenCalled();
    expect(component.error()).toBe('carWorkspace.messages.transactionFormInvalid');
    expect(fixture.nativeElement.textContent).toContain(
      'carWorkspace.messages.transactionFormInvalid',
    );
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
      fuelPrice: 0,
      fuelQuantity: 0,
      fuelDate: '2025-01-01',
      fuelBuyerId: 1,
      fuelNotes: '',
    });

    component.saveDrive();

    expect(component.error()).toBe('odometer must be greater than 0');
  });
});
