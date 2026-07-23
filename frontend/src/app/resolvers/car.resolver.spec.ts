import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, convertToParamMap } from '@angular/router';
import { Observable, of } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { carResolver } from './car.resolver';
import { CarService } from '../services/car.service';
import { CarDto } from '../models/cars';

describe('carResolver', () => {
  it('resolves the car matching the carId route param', () => {
    const car: CarDto = {
      carId: 5,
      name: 'City Car',
      plateNumber: 'M-AB-1',
      ownerId: 1,
      data: null,
    };
    const carService: Mocked<Pick<CarService, 'getCarById'>> = {
      getCarById: vi.fn().mockReturnValue(of(car)),
    };

    TestBed.configureTestingModule({
      providers: [{ provide: CarService, useValue: carService }],
    });

    const route = { paramMap: convertToParamMap({ carId: '5' }) } as ActivatedRouteSnapshot;
    const result = TestBed.runInInjectionContext(() => carResolver(route, {} as never));

    let resolved: CarDto | undefined;
    (result as Observable<CarDto>).subscribe((value) => (resolved = value));

    expect(carService.getCarById).toHaveBeenCalledWith(5);
    expect(resolved).toEqual(car);
  });
});
