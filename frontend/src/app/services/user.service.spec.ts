import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';
import { UserService } from './user.service';
import { CreateUserRequest, UserDto } from '../models/users';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  const apiUrl = environment.apiUrl;
  const user: UserDto = {
    userId: 1,
    firstname: 'Anna',
    lastname: 'Meyer',
    driverLicense: true,
    birthday: '1998-03-14',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getUsers issues GET /users', () => {
    let result: UserDto[] | undefined;
    service.getUsers().subscribe((users) => (result = users));

    const req = httpMock.expectOne(`${apiUrl}/users`);
    expect(req.request.method).toBe('GET');
    req.flush([user]);

    expect(result).toEqual([user]);
  });

  it('createUser issues POST /users with body', () => {
    const request: CreateUserRequest = {
      firstname: 'Max',
      lastname: 'Neu',
      driverLicense: false,
    };
    service.createUser(request).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/users`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(user);
  });

  it('updateUser issues PUT /users/:id with body', () => {
    const request: CreateUserRequest = {
      firstname: 'Anna',
      lastname: 'Renamed',
      driverLicense: true,
    };
    service.updateUser(1, request).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/users/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush(user);
  });

  it('deleteUser issues DELETE /users/:id', () => {
    service.deleteUser(7).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/users/7`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('propagates HTTP errors to the subscriber', () => {
    let status: number | undefined;
    service.getUsers().subscribe({
      error: (err) => (status = err.status),
    });

    httpMock
      .expectOne(`${apiUrl}/users`)
      .flush('boom', { status: 500, statusText: 'Server Error' });

    expect(status).toBe(500);
  });
});
