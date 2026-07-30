import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { CalculationHubComponent } from './calculation-hub.component';
import { AvailabilityStore } from './availability.store';
import { CarService } from '../../services/car.service';
import { CalculationService } from '../../services/calculation.service';
import { CarDto } from '../../models/cars';
import { CarAvailability, ParticipantSet } from '../../models/calculations';

describe('CalculationHubComponent — participant management', () => {
  let fixture: ComponentFixture<CalculationHubComponent>;
  let component: CalculationHubComponent;
  let calculationService: Mocked<
    Pick<
      CalculationService,
      | 'getParticipants'
      | 'saveParticipants'
      | 'deleteParticipants'
      | 'deleteYear'
      | 'getAvailability'
    >
  >;

  const car: CarDto = { carId: 5, name: 'City Car', plateNumber: 'M-AB-1', ownerId: 1, data: null };

  const participantSet: ParticipantSet = {
    carId: 5,
    year: 2025,
    stored: false,
    rows: [
      {
        userId: 1,
        userName: 'Anna',
        participating: true,
        manuallyAdded: false,
        hasDrives: true,
        distance: 100,
        fixShare: 33.33,
        varShare: 40,
      },
      {
        userId: 2,
        userName: 'Ben',
        participating: true,
        manuallyAdded: false,
        hasDrives: true,
        distance: 100,
        fixShare: 33.33,
        varShare: 40,
      },
      {
        userId: 3,
        userName: 'Carla',
        participating: true,
        manuallyAdded: false,
        hasDrives: true,
        distance: 50,
        fixShare: 33.34,
        varShare: 20,
      },
      {
        userId: 4,
        userName: 'Dan',
        participating: false,
        manuallyAdded: false,
        hasDrives: false,
        distance: 0,
        fixShare: null,
        varShare: null,
      },
    ],
  };

  function availability(yearCalculated: boolean): CarAvailability {
    return {
      carId: 5,
      years: [
        {
          year: 2025,
          yearCalculated,
          participantsStored: false,
          aggregatedMonths: [],
          monthsWithDrives: [],
        },
      ],
    };
  }

  async function setup(yearCalculated: boolean): Promise<void> {
    const carService: Mocked<Pick<CarService, 'getCars'>> = {
      getCars: vi.fn().mockReturnValue(of([car])),
    };
    calculationService = {
      getParticipants: vi.fn().mockReturnValue(of(participantSet)),
      saveParticipants: vi.fn().mockReturnValue(of(undefined)),
      deleteParticipants: vi.fn().mockReturnValue(of(undefined)),
      deleteYear: vi.fn().mockReturnValue(of(undefined)),
      getAvailability: vi.fn().mockReturnValue(of(availability(yearCalculated))),
    };

    await TestBed.configureTestingModule({
      imports: [CalculationHubComponent],
      providers: [
        provideTranslateService(),
        provideRouter([]),
        { provide: CarService, useValue: carService },
        { provide: CalculationService, useValue: calculationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CalculationHubComponent);
    component = fixture.componentInstance;
    component.carId.set(5);
    component.year.set(2025);
    TestBed.inject(AvailabilityStore).load(5);
    fixture.detectChanges();
  }

  it('moves the previewed fixed share from 33.33 to 25.00 when a 4th participant is ticked', async () => {
    await setup(false);

    expect(component.preview().fix.get(1)).toBeCloseTo(33.33);

    component.toggle(participantSet.rows[3]);

    expect(component.preview().fix.get(1)).toBeCloseTo(25);
    expect(component.preview().fix.get(4)).toBeCloseTo(25);
  });

  it("disables a driver's checkbox so it can't be unticked", async () => {
    await setup(false);

    const checkboxes: HTMLInputElement[] = Array.from(
      fixture.nativeElement.querySelectorAll('input[type="checkbox"]'),
    );
    // Anna (row 0) has drives and must be locked; Dan (row 3) has none and stays editable.
    expect(checkboxes[0].disabled).toBe(true);
    expect(checkboxes[3].disabled).toBe(false);
  });

  it('shows the stale-run hint after saving when the year is already calculated', async () => {
    await setup(true);

    component.save();

    expect(calculationService.saveParticipants).toHaveBeenCalled();
    expect(component.message()?.key).toBe('calculations.participants.savedStaleRun');
  });

  it('shows the delete-the-calculation button only for a calculated year', async () => {
    await setup(true);
    expect(fixture.nativeElement.textContent).toContain('calculations.participants.deleteRunBtn');
  });

  it('hides the delete-the-calculation button for a year that is not calculated', async () => {
    await setup(false);
    expect(fixture.nativeElement.textContent).not.toContain(
      'calculations.participants.deleteRunBtn',
    );
  });
});
