import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, BehaviorSubject, catchError } from 'rxjs';
import { Cost, CostResponse, CostSummary, CostRequest } from '../models/costs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CostService {
  private readonly apiUrl = `${environment.apiUrl}/costs`;

  constructor(private readonly http: HttpClient) { }

  private readonly costs = signal<Cost[]>([
    {
      id: '1',
      type: 'fixed',
      price: 500,
      amount: 1,
      shareholder: 'Jane Smith',
      date: new Date(2026, 0, 1),
      description: 'Car Insurance',
      category: 'Insurance'
    }
  ]);

  private readonly costs$ = new BehaviorSubject<Cost[]>(this.costs());

  getCostsForCar(carId: string): Observable<CostResponse[]> {
    return this.http.get<CostResponse[]>(`${this.apiUrl}/costs/${carId}`).pipe(
      catchError(() => {
        // Fallback to mock data if API is not available
        return this.costs$;
      })
    );
  }

  getCostById(id: string): Observable<Cost | undefined> {
    return of(this.costs().find(c => c.id === id));
  }

  createCost(carId: string, costRequest: CostRequest): Observable<CostResponse> {
    return this.http.post<CostResponse>(`${this.apiUrl}/costs/${carId}`, costRequest).pipe(
          catchError(() => {
            // Fallback to mock data if API is not available
            const newCost: Cost = {
              id: Date.now().toString(),
              ...costRequest
            };

            const currentCosts = this.costs();
            this.costs.set([...currentCosts, newCost]);
            this.costs$.next(this.costs());

            return of(newCost);
          })
        );
  }

  updateCost(id: string, cost: Partial<Cost>): Observable<Cost> {
    const currentCosts = this.costs();
    const index = currentCosts.findIndex(c => c.id === id);

    if (index !== -1) {
      const updated = {
        ...currentCosts[index],
        ...cost
      };
      currentCosts[index] = updated;
      this.costs.set([...currentCosts]);
      this.costs$.next(this.costs());
      return of(updated);
    }

    return of(currentCosts[index]);
  }

  deleteCost(id: string): Observable<void> {
    const currentCosts = this.costs();
    this.costs.set(currentCosts.filter(c => c.id !== id));
    this.costs$.next(this.costs());
    return of(void 0);
  }

  getCostDistribution(): Observable<CostSummary> {
    const mockSummary: CostSummary = {
      totalFixed: 500,
      totalVariable: 200,
      totalFuel: 150,
      totalCosts: 850,
      distributions: [
        {
          shareholder: 'John Doe',
          totalOwed: 425,
          fixedCosts: 250,
          variableCosts: 125,
          fuelCosts: 50,
          distance: 350
        },
        {
          shareholder: 'Jane Smith',
          totalOwed: 425,
          fixedCosts: 250,
          variableCosts: 125,
          fuelCosts: 50,
          distance: 350
        }
      ]
    };

    return of(mockSummary);
  }
}
