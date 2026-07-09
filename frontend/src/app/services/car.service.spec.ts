import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';
import { CarService } from './car.service';
import { CarDto, CreateCarRequest } from '../models/cars';

describe('CarService', () => {
  let service: CarService;
  let httpMock: HttpTestingController;

  const apiUrl = environment.apiUrl;
  const car: CarDto = {
    carId: 1,
    name: 'City Car',
    plateNumber: 'M-AB-1001',
    ownerId: 2,
    data: null,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CarService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getCars issues GET /vehicles', () => {
    let result: CarDto[] | undefined;
    service.getCars().subscribe((cars) => (result = cars));

    const req = httpMock.expectOne(`${apiUrl}/vehicles`);
    expect(req.request.method).toBe('GET');
    req.flush([car]);

    expect(result).toEqual([car]);
  });

  it('getCarById issues GET /vehicles/:id', () => {
    service.getCarById(1).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/vehicles/1`);
    expect(req.request.method).toBe('GET');
    req.flush(car);
  });

  it('createCar issues POST /vehicles with body', () => {
    const request: CreateCarRequest = {
      name: 'New Car',
      plateNumber: 'M-NC-1',
      ownerId: 2,
    };
    service.createCar(request).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/vehicles`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(car);
  });

  it('updateCar issues PUT /vehicles/:id with body', () => {
    const request: CreateCarRequest = {
      name: 'Renamed',
      plateNumber: 'M-NC-2',
      ownerId: 2,
    };
    service.updateCar(1, request).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/vehicles/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush(car);
  });

  it('deleteCar issues DELETE /vehicles/:id', () => {
    service.deleteCar(5).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/vehicles/5`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('propagates a 409 conflict to the subscriber', () => {
    let status: number | undefined;
    service.deleteCar(5).subscribe({
      error: (err) => (status = err.status),
    });

    httpMock
      .expectOne(`${apiUrl}/vehicles/5`)
      .flush('conflict', { status: 409, statusText: 'Conflict' });

    expect(status).toBe(409);
  });
});
