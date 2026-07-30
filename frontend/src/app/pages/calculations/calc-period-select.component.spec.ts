import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { CalcPeriodSelectComponent } from './calc-period-select.component';
import { AvailabilityStore } from './availability.store';
import { CarService } from '../../services/car.service';
import { CalculationService } from '../../services/calculation.service';
import { CarDto } from '../../models/cars';
import { CarAvailability } from '../../models/calculations';

describe('CalcPeriodSelectComponent', () => {
  let fixture: ComponentFixture<CalcPeriodSelectComponent>;

  const car: CarDto = { carId: 5, name: 'City Car', plateNumber: 'M-AB-1', ownerId: 1, data: null };
  const availability: CarAvailability = {
    carId: 5,
    years: [
      {
        year: 2025,
        yearCalculated: true,
        participantsStored: false,
        aggregatedMonths: [1, 2],
        monthsWithDrives: [1, 2, 3],
      },
    ],
  };

  beforeEach(async () => {
    const carService: Mocked<Pick<CarService, 'getCars'>> = {
      getCars: vi.fn().mockReturnValue(of([car])),
    };
    const calculationService: Mocked<Pick<CalculationService, 'getAvailability'>> = {
      getAvailability: vi.fn().mockReturnValue(of(availability)),
    };

    await TestBed.configureTestingModule({
      imports: [CalcPeriodSelectComponent],
      providers: [
        provideTranslateService(),
        { provide: CarService, useValue: carService },
        { provide: CalculationService, useValue: calculationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CalcPeriodSelectComponent);
    fixture.componentRef.setInput('carId', 5);
    fixture.componentRef.setInput('year', 2025);
    fixture.componentRef.setInput('showMonth', true);
    TestBed.inject(AvailabilityStore).load(5);
    fixture.detectChanges();
  });

  it('marks a calculated year with the orange dot in the year select', () => {
    const options: HTMLOptionElement[] = Array.from(
      fixture.nativeElement.querySelectorAll('select')[1].querySelectorAll('option'),
    );
    const yearOption = options.find((o) => o.textContent?.includes('2025'));
    expect(yearOption?.textContent?.trim()).toBe('● 2025');
  });

  it('renders a month chip strip with the stored/data markers', () => {
    const chips: HTMLElement[] = Array.from(fixture.nativeElement.querySelectorAll('.calc__chip'));
    expect(chips.length).toBe(12);
    expect(chips[0].classList.contains('calc__chip--stored')).toBe(true);
    expect(chips[2].classList.contains('calc__chip--data')).toBe(true);
    expect(chips[3].classList.contains('calc__chip--stored')).toBe(false);
    expect(chips[3].classList.contains('calc__chip--data')).toBe(false);
  });
});
