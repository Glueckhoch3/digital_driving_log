import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, BehaviorSubject } from 'rxjs';
import { Settlement, SettlementReport } from '../models/settlements';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SettlementService {
  private readonly apiUrl = `${environment.apiUrl}/articles`;

  constructor(private readonly http: HttpClient) {}

  private readonly settlements = signal<Settlement[]>([
    {
      id: '1',
      period: '2025',
      startDate: new Date(2025, 0, 1),
      endDate: new Date(2025, 11, 31),
      finalBalances: {
        '1': -500,
        '2': 500
      },
      totalDrives: 25,
      totalDistance: 5000,
      totalCosts: 1000,
      status: 'completed',
      createdAt: new Date(2024, 11, 31),
      completedAt: new Date(2025, 0, 5)
    }
  ]);

  private readonly settlements$ = new BehaviorSubject<Settlement[]>(this.settlements());

  getSettlements(): Observable<Settlement[]> {
    return this.settlements$;
  }

  getSettlementById(id: string): Observable<Settlement | undefined> {
    return of(this.settlements().find(s => s.id === id));
  }

  triggerSettlement(): Observable<Settlement> {
    const newSettlement: Settlement = {
      id: Date.now().toString(),
      period: new Date().getFullYear().toString(),
      startDate: new Date(new Date().getFullYear(), 0, 1),
      endDate: new Date(new Date().getFullYear(), 11, 31),
      finalBalances: {
        '1': -425,
        '2': 425
      },
      totalDrives: 10,
      totalDistance: 2500,
      totalCosts: 850,
      status: 'pending',
      createdAt: new Date()
    };

    const currentSettlements = this.settlements();
    this.settlements.set([...currentSettlements, newSettlement]);
    this.settlements$.next(this.settlements());

    return of(newSettlement);
  }

  getSettlementReport(id: string): Observable<SettlementReport | undefined> {
    const settlement = this.settlements().find(s => s.id === id);

    if (!settlement) {
      return of(undefined);
    }

    const report: SettlementReport = {
      settlement,
      costBreakdown: {
        fixedCosts: 500,
        variableCosts: 200,
        fuelCosts: 150,
        totalCosts: 850
      },
      shareholderReports: [
        {
          shareholderId: '1',
          shareholderName: 'John Doe',
          distance: 1250,
          fixedCostsShare: 250,
          variableCostsShare: 100,
          fuelCostsShare: 75,
          totalOwed: 425,
          paid: false
        },
        {
          shareholderId: '2',
          shareholderName: 'Jane Smith',
          distance: 1250,
          fixedCostsShare: 250,
          variableCostsShare: 100,
          fuelCostsShare: 75,
          totalOwed: 425,
          paid: false
        }
      ]
    };

    return of(report);
  }
}
