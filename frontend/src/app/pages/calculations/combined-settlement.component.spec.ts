import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { CombinedSettlementComponent } from './combined-settlement.component';
import { CarService } from '../../services/car.service';
import { CalculationService } from '../../services/calculation.service';
import { CarDto } from '../../models/cars';

describe('CombinedSettlementComponent — calculated-cars markers', () => {
  let fixture: ComponentFixture<CombinedSettlementComponent>;

  const cars: CarDto[] = [
    { carId: 1, name: 'Golf', plateNumber: 'M-AB-1', ownerId: 1, data: null },
    { carId: 2, name: 'Polo', plateNumber: 'M-CD-2', ownerId: 1, data: null },
  ];

  beforeEach(async () => {
    const carService: Mocked<Pick<CarService, 'getCars'>> = {
      getCars: vi.fn().mockReturnValue(of(cars)),
    };
    const calculationService: Mocked<Pick<CalculationService, 'getCombined' | 'yearlyExists'>> = {
      getCombined: vi.fn().mockReturnValue(of([])),
      yearlyExists: vi.fn().mockImplementation((carId: number) => of(carId === 1)),
    };

    await TestBed.configureTestingModule({
      imports: [CombinedSettlementComponent],
      providers: [
        provideTranslateService(),
        provideRouter([]),
        { provide: CarService, useValue: carService },
        { provide: CalculationService, useValue: calculationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CombinedSettlementComponent);
    fixture.detectChanges();
  });

  it('marks the calculated car and leaves the uncalculated one plain', () => {
    const chips: HTMLElement[] = Array.from(fixture.nativeElement.querySelectorAll('.calc__chip'));
    expect(chips.length).toBe(2);

    const golf = chips.find((c) => c.textContent?.includes('Golf'));
    const polo = chips.find((c) => c.textContent?.includes('Polo'));

    expect(golf?.classList.contains('calc__chip--stored')).toBe(true);
    expect(polo?.classList.contains('calc__chip--stored')).toBe(false);
  });
});
