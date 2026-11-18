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

describe('CombinedSettlementComponent — final result vs. details (issue #41)', () => {
  let fixture: ComponentFixture<CombinedSettlementComponent>;

  const rows = [
    {
      userId: 1,
      userName: 'Anna Maier',
      paid: 100,
      differenceVariableCost: 40,
      differenceFixCost: 22.4,
      netBalance: 62.4,
    },
    {
      userId: 2,
      userName: 'Tom Koch',
      paid: 0,
      differenceVariableCost: -30,
      differenceFixCost: -32.4,
      netBalance: -62.4,
    },
  ];

  beforeEach(async () => {
    const carService: Mocked<Pick<CarService, 'getCars'>> = {
      getCars: vi.fn().mockReturnValue(of([])),
    };
    const calculationService: Mocked<Pick<CalculationService, 'getCombined' | 'yearlyExists'>> = {
      getCombined: vi.fn().mockReturnValue(of(rows)),
      yearlyExists: vi.fn().mockReturnValue(of(true)),
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

  it('shows only one card per person with the final amount by default', () => {
    const el: HTMLElement = fixture.nativeElement;
    const people = el.querySelectorAll('.calc__person');
    expect(people.length).toBe(2);
    expect(el.querySelector('table')).toBeNull();

    expect(people[0].querySelector('.calc__amount--pos')).not.toBeNull();
    expect(people[1].querySelector('.calc__amount--neg')).not.toBeNull();
    // amounts are shown without sign, direction comes from the word and arrow
    expect(people[1].textContent).toContain('62.40');
    expect(people[1].textContent).not.toContain('-62.40');
  });

  it('moves the fix/var differences into the details view', () => {
    const el: HTMLElement = fixture.nativeElement;
    (el.querySelectorAll('.calc__tab')[1] as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(el.querySelector('.calc__person')).toBeNull();
    expect(el.querySelectorAll('table.tbl tbody tr').length).toBe(2);
    expect(el.querySelectorAll('table.tbl th').length).toBe(5);
  });

  it('derives initials from the name', () => {
    expect(fixture.componentInstance.initials('Anna Maier')).toBe('AM');
    expect(fixture.componentInstance.initials('T. K.')).toBe('TK');
    expect(fixture.componentInstance.initials('Cher')).toBe('C');
  });
});
