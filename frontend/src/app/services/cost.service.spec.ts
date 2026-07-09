import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';
import { CostService } from './cost.service';
import { CostDto, CreateCostRequest } from '../models/costs';

describe('CostService', () => {
  let service: CostService;
  let httpMock: HttpTestingController;

  const apiUrl = environment.apiUrl;
  const cost: CostDto = {
    costId: 1,
    carId: 2,
    buyerId: 3,
    transactionObject: 'Fuel',
    price: 54.9,
    amount: 40,
    dayOfTransaction: '2025-04-02',
    costType: 'VARIABLE',
    notes: null
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(CostService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getAllCosts issues GET /costs', () => {
    let result: CostDto[] | undefined;
    service.getAllCosts().subscribe(costs => (result = costs));

    const req = httpMock.expectOne(`${apiUrl}/costs`);
    expect(req.request.method).toBe('GET');
    req.flush([cost]);

    expect(result).toEqual([cost]);
  });

  it('getCostsForCar issues GET /vehicles/:carId/costs', () => {
    service.getCostsForCar(2).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/vehicles/2/costs`);
    expect(req.request.method).toBe('GET');
    req.flush([cost]);
  });

  it('createCost issues POST /costs with body', () => {
    const request: CreateCostRequest = {
      carId: 2,
      buyerId: 3,
      transactionObject: 'Oil',
      price: 19.99,
      amount: 1,
      dayOfTransaction: '2025-05-05',
      costType: 'variable'
    };
    service.createCost(request).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/costs`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(cost);
  });

  it('deleteCost issues DELETE /costs/:id', () => {
    service.deleteCost(4).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/costs/4`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('propagates HTTP errors to the subscriber', () => {
    let status: number | undefined;
    service.getAllCosts().subscribe({
      error: err => (status = err.status)
    });

    httpMock
      .expectOne(`${apiUrl}/costs`)
      .flush('boom', { status: 500, statusText: 'Server Error' });

    expect(status).toBe(500);
  });
});
