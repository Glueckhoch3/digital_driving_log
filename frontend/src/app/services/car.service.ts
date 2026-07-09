import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CarDto, CreateCarRequest, UpdateCarRequest } from '../models/cars';

@Injectable({ providedIn: 'root' })
export class CarService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  getCars(): Observable<CarDto[]> {
    return this.http.get<CarDto[]>(`${this.apiUrl}/vehicles`);
  }

  getCarById(carId: number): Observable<CarDto> {
    return this.http.get<CarDto>(`${this.apiUrl}/vehicles/${carId}`);
  }

  createCar(request: CreateCarRequest): Observable<CarDto> {
    return this.http.post<CarDto>(`${this.apiUrl}/vehicles`, request);
  }

  updateCar(carId: number, request: UpdateCarRequest): Observable<CarDto> {
    return this.http.put<CarDto>(`${this.apiUrl}/vehicles/${carId}`, request);
  }

  deleteCar(carId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/vehicles/${carId}`);
  }
}
