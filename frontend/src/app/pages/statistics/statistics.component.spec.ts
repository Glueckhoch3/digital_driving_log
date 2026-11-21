import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { StatisticsComponent } from './statistics.component';
import { ConsumptionService } from '../../services/consumption.service';
import { ConsumptionDto } from '../../models/consumption';
import { CarDto } from '../../models/cars';

describe('StatisticsComponent', () => {
  let fixture: ComponentFixture<StatisticsComponent>;
  let component: StatisticsComponent;
  let service: Mocked<Pick<ConsumptionService, 'getConsumption'>>;

  const car: CarDto = { carId: 5, name: 'City Car', plateNumber: 'M-AB-1', ownerId: 1, data: null };
  const data: ConsumptionDto = {
    carId: 5,
    year: null,
    intervals: [
      {
        costId: 1,
        date: '2025-01-10',
        odometer: 10500,
        distanceKm: 500,
        liters: 30,
        price: 45,
        litersPer100Km: 6,
      },
      {
        costId: 2,
        date: '2025-02-10',
        odometer: 11000,
        distanceKm: 500,
        liters: 50,
        price: 75,
        litersPer100Km: 10,
      },
    ],
    averageLitersPer100Km: 8,
    totalDistanceKm: 1000,
    totalLiters: 80,
  };

  beforeEach(async () => {
    service = { getConsumption: vi.fn().mockReturnValue(of(data)) };

    await TestBed.configureTestingModule({
      imports: [StatisticsComponent],
      providers: [
        provideTranslateService(),
        provideRouter([]),
        { provide: ConsumptionService, useValue: service },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(StatisticsComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('car', car);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('loads consumption for all years by default', () => {
    expect(service.getConsumption).toHaveBeenCalledWith(5, null);
    expect(component.intervals()).toHaveLength(2);
  });

  it('renders the table rows and one chart point per interval', () => {
    expect(fixture.nativeElement.querySelectorAll('tbody tr')).toHaveLength(2);
    expect(fixture.nativeElement.querySelectorAll('circle')).toHaveLength(2);
    expect(fixture.nativeElement.querySelector('svg.statistics__chart').getAttribute('role')).toBe(
      'img',
    );
  });

  it('reloads when a year is selected', async () => {
    component.year.set(2025);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(service.getConsumption).toHaveBeenLastCalledWith(5, 2025);
  });

  it('scales the y axis to the largest consumption', () => {
    expect(component.yMax()).toBe(10);
  });
});
