import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CostDto, CreateCostRequest } from '../models/costs';

@Injectable({ providedIn: 'root' })
export class CostService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  getAllCosts(): Observable<CostDto[]> {
    return this.http.get<CostDto[]>(`${this.apiUrl}/costs`);
  }

  getCostsForCar(carId: number): Observable<CostDto[]> {
    return this.http.get<CostDto[]>(`${this.apiUrl}/vehicles/${carId}/costs`);
  }

  createCost(request: CreateCostRequest): Observable<CostDto> {
    return this.http.post<CostDto>(`${this.apiUrl}/costs`, request);
  }

  deleteCost(costId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/costs/${costId}`);
  }
}
