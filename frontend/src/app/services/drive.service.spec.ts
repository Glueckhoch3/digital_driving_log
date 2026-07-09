import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';
import { DriveService } from './drive.service';
import { CreateDriveRequest, DriveDto } from '../models/drives';

describe('DriveService', () => {
  let service: DriveService;
  let httpMock: HttpTestingController;

  const apiUrl = environment.apiUrl;
  const drive: DriveDto = {
    driveId: 1,
    carId: 2,
    driveDate: '2025-03-01',
    currentMileage: 14500,
    drivenDistance: 25,
    driverId: 3,
    notes: null,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(DriveService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getDriveById issues GET /drives/:id', () => {
    let result: DriveDto | undefined;
    service.getDriveById(1).subscribe((d) => (result = d));

    const req = httpMock.expectOne(`${apiUrl}/drives/1`);
    expect(req.request.method).toBe('GET');
    req.flush(drive);

    expect(result).toEqual(drive);
  });

  it('getDrivesForCar issues GET /vehicles/:carId/drives', () => {
    service.getDrivesForCar(2).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/vehicles/2/drives`);
    expect(req.request.method).toBe('GET');
    req.flush([drive]);
  });

  it('createDrive issues POST /drives with body', () => {
    const request: CreateDriveRequest = {
      carId: 2,
      currentMileage: 14525,
      driverId: 3,
      driveDate: '2025-03-01',
    };
    service.createDrive(request).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/drives`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(drive);
  });

  it('updateDrive issues PUT /drives/:id with body', () => {
    const request: CreateDriveRequest = {
      carId: 2,
      currentMileage: 14600,
      driverId: 3,
      driveDate: '2025-03-02',
    };
    service.updateDrive(1, request).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/drives/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush(drive);
  });

  it('deleteDrive issues DELETE /drives/:id', () => {
    service.deleteDrive(9).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/drives/9`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('propagates HTTP errors to the subscriber', () => {
    let status: number | undefined;
    service.getDriveById(1).subscribe({
      error: (err) => (status = err.status),
    });

    httpMock
      .expectOne(`${apiUrl}/drives/1`)
      .flush('missing', { status: 404, statusText: 'Not Found' });

    expect(status).toBe(404);
  });
});
