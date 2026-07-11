import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CostDto, CreateCostRequest } from '../models/costs';
import { Page } from '../models/page';
import { PageQuery, toHttpParams } from '../models/page-query';

@Injectable({ providedIn: 'root' })
export class CostService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  getAllCosts(query?: PageQuery): Observable<Page<CostDto>> {
    const params: HttpParams = toHttpParams(query);
    return this.http.get<Page<CostDto>>(`${this.apiUrl}/costs`, { params });
  }

  getCostsForCar(carId: number, query?: PageQuery): Observable<Page<CostDto>> {
    const params: HttpParams = toHttpParams(query);
    return this.http.get<Page<CostDto>>(`${this.apiUrl}/vehicles/${carId}/costs`, { params });
  }

  createCost(request: CreateCostRequest): Observable<CostDto> {
    return this.http.post<CostDto>(`${this.apiUrl}/costs`, request);
  }

  deleteCost(costId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/costs/${costId}`);
  }
}
