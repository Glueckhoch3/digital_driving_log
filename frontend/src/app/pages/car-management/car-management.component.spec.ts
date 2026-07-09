import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { CarManagementComponent } from './car-management.component';
import { CarService } from '../../services/car.service';
import { UserService } from '../../services/user.service';
import { CarDto } from '../../models/cars';
import { UserDto } from '../../models/users';

describe('CarManagementComponent', () => {
  let fixture: ComponentFixture<CarManagementComponent>;
  let component: CarManagementComponent;
  let carService: Mocked<Pick<CarService, 'getCars' | 'createCar' | 'updateCar' | 'deleteCar'>>;
  let userService: Mocked<Pick<UserService, 'getUsers'>>;

  const users: UserDto[] = [
    { userId: 1, firstname: 'Anna', lastname: 'Meyer', driverLicense: true, birthday: null },
  ];
  const cars: CarDto[] = [
    { carId: 10, name: 'City Car', plateNumber: 'M-AB-1001', ownerId: 1, data: null },
  ];

  beforeEach(async () => {
    carService = {
      getCars: vi.fn().mockReturnValue(of(cars)),
      createCar: vi.fn().mockReturnValue(of(cars[0])),
      updateCar: vi.fn().mockReturnValue(of(cars[0])),
      deleteCar: vi.fn().mockReturnValue(of(void 0)),
    };
    userService = {
      getUsers: vi.fn().mockReturnValue(of(users)),
    };

    await TestBed.configureTestingModule({
      imports: [CarManagementComponent],
      providers: [
        provideTranslateService(),
        { provide: CarService, useValue: carService },
        { provide: UserService, useValue: userService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CarManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads users and cars on init and preselects the first owner', () => {
    expect(userService.getUsers).toHaveBeenCalled();
    expect(carService.getCars).toHaveBeenCalled();
    expect(component.cars()).toEqual(cars);
    expect(component.users()).toEqual(users);
    expect(component.carForm.value.ownerId).toBe(1);
    expect(component.loading()).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('City Car');
  });

  it('maps owner ids to display names', () => {
    expect(component.ownerLabelById().get(1)).toBe('Anna Meyer');
  });

  it('shows load error when the car request fails', () => {
    carService.getCars.mockReturnValue(throwError(() => ({ status: 500 })));

    component.loadData();

    expect(component.error()).toBe('carManagement.messages.loadFailed');
    expect(component.loading()).toBe(false);
  });

  it('refuses to submit when no users exist', () => {
    component.users.set([]);

    component.submit();

    expect(component.error()).toBe('carManagement.messages.usersRequiredFirst');
    expect(carService.createCar).not.toHaveBeenCalled();
  });

  it('does not submit an invalid form', () => {
    component.carForm.setValue({ name: '', plateNumber: '', ownerId: 0, data: '' });

    component.submit();

    expect(carService.createCar).not.toHaveBeenCalled();
    expect(component.carForm.touched).toBe(true);
  });

  it('creates a car from a valid form and reloads', () => {
    component.carForm.setValue({
      name: 'New Car',
      plateNumber: 'M-NC-1',
      ownerId: 1,
      data: '',
    });

    component.submit();

    expect(carService.createCar).toHaveBeenCalledWith({
      name: 'New Car',
      plateNumber: 'M-NC-1',
      ownerId: 1,
      data: undefined,
    });
    expect(component.message()).toBe('carManagement.messages.createSuccess');
    expect(carService.getCars).toHaveBeenCalledTimes(2);
  });

  it('updates the car being edited instead of creating', () => {
    component.edit(cars[0]);
    component.carForm.patchValue({ name: 'Renamed' });

    component.submit();

    expect(carService.updateCar).toHaveBeenCalledWith(10, {
      name: 'Renamed',
      plateNumber: 'M-AB-1001',
      ownerId: 1,
      data: undefined,
    });
    expect(carService.createCar).not.toHaveBeenCalled();
    expect(component.editingCarId()).toBeNull();
  });

  it('deletes a car and reloads', () => {
    component.delete(10);

    expect(carService.deleteCar).toHaveBeenCalledWith(10);
    expect(component.message()).toBe('carManagement.messages.deleteSuccess');
  });

  it('maps a 409 delete conflict to the dependency-blocked message', () => {
    carService.deleteCar.mockReturnValue(throwError(() => ({ status: 409 })));

    component.delete(10);

    expect(component.error()).toBe('carManagement.messages.deleteDependencyBlocked');
  });

  it('maps other delete errors to the generic delete message', () => {
    carService.deleteCar.mockReturnValue(throwError(() => ({ status: 500 })));

    component.delete(10);

    expect(component.error()).toBe('carManagement.messages.deleteFailed');
  });
});
