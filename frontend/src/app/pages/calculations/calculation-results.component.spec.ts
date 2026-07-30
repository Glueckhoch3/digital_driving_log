import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter, ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { CalculationResultsComponent } from './calculation-results.component';
import { CarService } from '../../services/car.service';
import { CalculationService } from '../../services/calculation.service';
import { CarDto } from '../../models/cars';

describe('CalculationResultsComponent', () => {
  let fixture: ComponentFixture<CalculationResultsComponent>;
  let calculationService: Mocked<
    Pick<
      CalculationService,
      'getAvailability' | 'getYearlySettlement' | 'getMonthlyDistances' | 'getFactors'
    >
  >;

  const car: CarDto = { carId: 5, name: 'City Car', plateNumber: 'M-AB-1', ownerId: 1, data: null };

  async function setup(initialTab: string | null): Promise<void> {
    const carService: Mocked<Pick<CarService, 'getCars'>> = {
      getCars: vi.fn().mockReturnValue(of([car])),
    };
    calculationService = {
      getAvailability: vi.fn().mockReturnValue(of({ carId: 5, years: [] })),
      getYearlySettlement: vi.fn().mockReturnValue(of([])),
      getMonthlyDistances: vi.fn().mockReturnValue(of([])),
      getFactors: vi.fn().mockReturnValue(of([])),
    };

    await TestBed.configureTestingModule({
      imports: [CalculationResultsComponent],
      providers: [
        provideTranslateService(),
        provideRouter([]),
        { provide: CarService, useValue: carService },
        { provide: CalculationService, useValue: calculationService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { queryParamMap: convertToParamMap(initialTab ? { tab: initialTab } : {}) },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CalculationResultsComponent);
    fixture.componentInstance.carId.set(5);
    fixture.componentInstance.year.set(2025);
    fixture.detectChanges();
  }

  it('reads the initial tab from the query param', async () => {
    await setup('factors');
    expect(fixture.componentInstance.tab()).toBe('factors');
  });

  it('defaults to the yearly tab when no query param is present', async () => {
    await setup(null);
    expect(fixture.componentInstance.tab()).toBe('yearly');
  });

  it('does not refetch a tab already loaded when switching back to it', async () => {
    await setup('yearly');

    expect(calculationService.getYearlySettlement).toHaveBeenCalledTimes(1);

    fixture.componentInstance.selectTab('monthly');
    fixture.detectChanges();
    fixture.componentInstance.selectTab('yearly');
    fixture.detectChanges();

    // The yearly tab body stays mounted (hidden, not destroyed) while switching tabs,
    // so its data-loading effect never re-fires for the same car/year.
    expect(calculationService.getYearlySettlement).toHaveBeenCalledTimes(1);
  });
});
