import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ConsumptionDto } from '../models/consumption';

@Injectable({ providedIn: 'root' })
export class ConsumptionService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  getConsumption(carId: number, year?: number | null): Observable<ConsumptionDto> {
    let params = new HttpParams();
    if (year != null) {
      params = params.set('year', year);
    }
    return this.http.get<ConsumptionDto>(`${this.apiUrl}/vehicles/${carId}/consumption`, {
      params,
    });
  }
}
