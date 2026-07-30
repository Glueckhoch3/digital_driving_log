import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { CalculationRunComponent } from './calculation-run.component';
import { AvailabilityStore } from './availability.store';
import { CarService } from '../../services/car.service';
import { CalculationService } from '../../services/calculation.service';
import { CarDto } from '../../models/cars';
import { CarAvailability } from '../../models/calculations';

describe('CalculationRunComponent', () => {
  let fixture: ComponentFixture<CalculationRunComponent>;
  let component: CalculationRunComponent;

  const car: CarDto = { carId: 5, name: 'City Car', plateNumber: 'M-AB-1', ownerId: 1, data: null };

  const availability: CarAvailability = {
    carId: 5,
    years: [
      {
        year: 2025,
        yearCalculated: true,
        participantsStored: false,
        aggregatedMonths: [1],
        monthsWithDrives: [1, 2],
      },
    ],
  };

  beforeEach(async () => {
    const carService: Mocked<Pick<CarService, 'getCars'>> = {
      getCars: vi.fn().mockReturnValue(of([car])),
    };
    const calculationService: Mocked<
      Pick<
        CalculationService,
        | 'getAvailability'
        | 'getParticipants'
        | 'aggregateMonth'
        | 'deleteMonth'
        | 'calculateYear'
        | 'deleteYear'
      >
    > = {
      getAvailability: vi.fn().mockReturnValue(of(availability)),
      getParticipants: vi
        .fn()
        .mockReturnValue(of({ carId: 5, year: 2025, stored: false, rows: [] })),
      aggregateMonth: vi.fn().mockReturnValue(of(undefined)),
      deleteMonth: vi.fn().mockReturnValue(of(undefined)),
      calculateYear: vi.fn().mockReturnValue(of(undefined)),
      deleteYear: vi.fn().mockReturnValue(of(undefined)),
    };

    await TestBed.configureTestingModule({
      imports: [CalculationRunComponent],
      providers: [
        provideTranslateService(),
        provideRouter([]),
        { provide: CarService, useValue: carService },
        { provide: CalculationService, useValue: calculationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CalculationRunComponent);
    component = fixture.componentInstance;
    component.carId.set(5);
    component.year.set(2025);
    component.month.set(1);
    TestBed.inject(AvailabilityStore).load(5);
    fixture.detectChanges();
  });

  it('enables delete-month and disables aggregate-month for an already aggregated month', () => {
    const buttons: HTMLButtonElement[] = Array.from(
      fixture.nativeElement.querySelectorAll('button'),
    );
    const aggregateBtn = buttons.find((b) =>
      b.textContent?.includes('calculations.run.aggregateBtn'),
    );
    const deleteMonthBtn = buttons.find((b) =>
      b.textContent?.includes('calculations.run.deleteMonthBtn'),
    );

    expect(aggregateBtn?.disabled).toBe(true);
    expect(deleteMonthBtn?.disabled).toBe(false);
  });

  it('enables delete-year and disables calculate-year for an already calculated year', () => {
    const buttons: HTMLButtonElement[] = Array.from(
      fixture.nativeElement.querySelectorAll('button'),
    );
    const calculateBtn = buttons.find((b) =>
      b.textContent?.includes('calculations.run.calculateBtn'),
    );
    const deleteYearBtn = buttons.find((b) =>
      b.textContent?.includes('calculations.run.deleteYearBtn'),
    );

    expect(calculateBtn?.disabled).toBe(true);
    expect(deleteYearBtn?.disabled).toBe(false);
  });

  it('flips availability-derived state when the month changes to one that is not aggregated', () => {
    component.month.set(6);
    fixture.detectChanges();

    expect(component.monthAggregated()).toBe(false);
  });
});
