import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CostCategoryDto,
  CreateCostCategoryRequest,
  UpdateCostCategoryRequest,
} from '../models/cost-categories';

@Injectable({ providedIn: 'root' })
export class CostCategoryService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/cost-categories`;

  getCostCategories(): Observable<CostCategoryDto[]> {
    return this.http.get<CostCategoryDto[]>(this.apiUrl);
  }

  createCostCategory(request: CreateCostCategoryRequest): Observable<CostCategoryDto> {
    return this.http.post<CostCategoryDto>(this.apiUrl, request);
  }

  updateCostCategory(id: number, request: UpdateCostCategoryRequest): Observable<CostCategoryDto> {
    return this.http.put<CostCategoryDto>(`${this.apiUrl}/${id}`, request);
  }

  deleteCostCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
