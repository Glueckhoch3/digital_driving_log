import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import {
  CarAvailability,
  CombinedSettlementRow,
  FactorRow,
  MonthlyCalculationRequest,
  MonthlyDistance,
  ParticipantSet,
  ParticipantUpdateRequest,
  YearlyCalculationRequest,
  YearlySettlementRow,
} from '../models/calculations';

@Injectable({ providedIn: 'root' })
export class CalculationService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/calculations`;

  // --- actions ---

  aggregateMonth(request: MonthlyCalculationRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/monthly`, request);
  }

  calculateYear(request: YearlyCalculationRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/yearly`, request);
  }

  deleteMonth(carId: number, year: number, month: number): Observable<void> {
    const params = new HttpParams().set('carId', carId).set('year', year).set('month', month);
    return this.http.delete<void>(`${this.apiUrl}/monthly`, { params });
  }

  deleteYear(carId: number, year: number): Observable<void> {
    const params = new HttpParams().set('carId', carId).set('year', year);
    return this.http.delete<void>(`${this.apiUrl}/yearly`, { params });
  }

  // --- existence checks ---

  monthlyExists(carId: number, year: number, month: number): Observable<boolean> {
    const params = new HttpParams().set('carId', carId).set('year', year).set('month', month);
    return this.http
      .get<{ exists: boolean }>(`${this.apiUrl}/monthly/exists`, { params })
      .pipe(map((r) => r.exists));
  }

  yearlyExists(carId: number, year: number): Observable<boolean> {
    const params = new HttpParams().set('carId', carId).set('year', year);
    return this.http
      .get<{ exists: boolean }>(`${this.apiUrl}/yearly/exists`, { params })
      .pipe(map((r) => r.exists));
  }

  // --- views ---

  getMonthlyDistances(carId: number, year: number): Observable<MonthlyDistance[]> {
    const params = new HttpParams().set('carId', carId).set('year', year);
    return this.http.get<MonthlyDistance[]>(`${this.apiUrl}/monthly`, { params });
  }

  getYearlySettlement(carId: number, year: number): Observable<YearlySettlementRow[]> {
    const params = new HttpParams().set('carId', carId).set('year', year);
    return this.http.get<YearlySettlementRow[]>(`${this.apiUrl}/yearly`, { params });
  }

  getFactors(carId: number, year: number): Observable<FactorRow[]> {
    const params = new HttpParams().set('carId', carId).set('year', year);
    return this.http.get<FactorRow[]>(`${this.apiUrl}/factors`, { params });
  }

  getCombined(year: number): Observable<CombinedSettlementRow[]> {
    const params = new HttpParams().set('year', year);
    return this.http.get<CombinedSettlementRow[]>(`${this.apiUrl}/combined`, { params });
  }

  // --- participants ---

  getParticipants(carId: number, year: number): Observable<ParticipantSet> {
    const params = new HttpParams().set('carId', carId).set('year', year);
    return this.http.get<ParticipantSet>(`${this.apiUrl}/participants`, { params });
  }

  saveParticipants(request: ParticipantUpdateRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/participants`, request);
  }

  deleteParticipants(carId: number, year: number): Observable<void> {
    const params = new HttpParams().set('carId', carId).set('year', year);
    return this.http.delete<void>(`${this.apiUrl}/participants`, { params });
  }

  // --- availability ---

  getAvailability(carId: number): Observable<CarAvailability> {
    const params = new HttpParams().set('carId', carId);
    return this.http.get<CarAvailability>(`${this.apiUrl}/availability`, { params });
  }
}
