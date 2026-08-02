import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { DrivesListComponent } from './drives-list.component';
import { DriveService } from '../../services/drive.service';
import { CarDto } from '../../models/cars';
import { UserDto } from '../../models/users';
import { Page } from '../../models/page';
import { DriveDto } from '../../models/drives';

describe('DrivesListComponent', () => {
  let fixture: ComponentFixture<DrivesListComponent>;
  let component: DrivesListComponent;
  let driveService: Mocked<Pick<DriveService, 'getDrivesForCar'>>;

  const car: CarDto = { carId: 5, name: 'City Car', plateNumber: 'M-AB-1', ownerId: 1, data: null };
  const users: UserDto[] = [
    { userId: 1, firstname: 'Anna', lastname: 'Meyer', driverLicense: true, birthday: null },
  ];

  const drivePage = (drives: DriveDto[], totalPages: number): Page<DriveDto> => ({
    content: drives,
    totalElements: drives.length,
    totalPages,
    number: 0,
    size: 10,
  });

  beforeEach(async () => {
    driveService = { getDrivesForCar: vi.fn().mockReturnValue(of(drivePage([], 0))) };

    await TestBed.configureTestingModule({
      imports: [DrivesListComponent],
      providers: [
        provideTranslateService(),
        provideRouter([]),
        { provide: DriveService, useValue: driveService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DrivesListComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('car', car);
    fixture.componentRef.setInput('users', users);
  });

  it('shows the empty state when there are no drives', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(driveService.getDrivesForCar).toHaveBeenCalledWith(5, {
      page: 0,
      size: 10,
      sort: ['odometer,desc', 'driveDate,desc'],
    });
    expect(fixture.nativeElement.textContent).toContain('carWorkspace.drives.empty');
  });

  it('computes driven distance from consecutive odometer readings', async () => {
    const drives: DriveDto[] = [
      { driveId: 2, carId: 5, driveDate: '2025-01-02', odometer: 150, driverId: 1, notes: null },
      { driveId: 1, carId: 5, driveDate: '2025-01-01', odometer: 100, driverId: 1, notes: null },
    ];
    driveService.getDrivesForCar.mockReturnValue(of(drivePage(drives, 1)));

    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(component.driveRows()).toEqual([
      { drive: drives[0], distance: 50 },
      { drive: drives[1], distance: null },
    ]);
  });

  it('requests the next page when changePage(1) is called', async () => {
    driveService.getDrivesForCar.mockReturnValue(of(drivePage([], 2)));
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    component.changePage(1);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(driveService.getDrivesForCar).toHaveBeenCalledWith(5, {
      page: 1,
      size: 10,
      sort: ['odometer,desc', 'driveDate,desc'],
    });
  });
});
