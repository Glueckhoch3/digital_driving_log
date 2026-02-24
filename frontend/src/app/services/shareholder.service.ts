import { Injectable, signal } from '@angular/core';
import { Observable, of, BehaviorSubject } from 'rxjs';
import {
  Shareholder,
  CreateShareholderRequest,
  ShareholderBalance
} from '../models/shareholder.model';

@Injectable({
  providedIn: 'root'
})
export class ShareholderService {
  private shareholders = signal<Shareholder[]>([
    {
      id: '1',
      name: 'John Doe',
      email: 'john@example.com',
      participantType: 'permanent',
      startDate: new Date(2025, 0, 1),
      isActive: true,
      createdAt: new Date(),
      updatedAt: new Date()
    },
    {
      id: '2',
      name: 'Jane Smith',
      email: 'jane@example.com',
      participantType: 'permanent',
      startDate: new Date(2025, 0, 1),
      isActive: true,
      createdAt: new Date(),
      updatedAt: new Date()
    }
  ]);

  private shareholders$ = new BehaviorSubject<Shareholder[]>(this.shareholders());

  getShareholders(): Observable<Shareholder[]> {
    return this.shareholders$;
  }

  getShareholderById(id: string): Observable<Shareholder | undefined> {
    return of(this.shareholders().find(s => s.id === id));
  }

  createShareholder(request: CreateShareholderRequest): Observable<Shareholder> {
    const newShareholder: Shareholder = {
      id: Date.now().toString(),
      ...request,
      isActive: true,
      createdAt: new Date(),
      updatedAt: new Date()
    };

    const currentShareholders = this.shareholders();
    this.shareholders.set([...currentShareholders, newShareholder]);
    this.shareholders$.next(this.shareholders());

    return of(newShareholder);
  }

  updateShareholder(id: string, shareholder: Partial<Shareholder>): Observable<Shareholder> {
    const currentShareholders = this.shareholders();
    const index = currentShareholders.findIndex(s => s.id === id);

    if (index !== -1) {
      const updated = {
        ...currentShareholders[index],
        ...shareholder,
        updatedAt: new Date()
      };
      currentShareholders[index] = updated;
      this.shareholders.set([...currentShareholders]);
      this.shareholders$.next(this.shareholders());
      return of(updated);
    }

    return of(currentShareholders[index]);
  }

  removeShareholder(id: string): Observable<void> {
    const currentShareholders = this.shareholders();
    this.shareholders.set(currentShareholders.filter(s => s.id !== id));
    this.shareholders$.next(this.shareholders());
    return of(void 0);
  }

  getShareholderBalances(): Observable<ShareholderBalance[]> {
    const balances: ShareholderBalance[] = this.shareholders().map(s => ({
      shareholder: s,
      totalBalance: -425,
      owes: 425,
      isOwed: 0
    }));

    return of(balances);
  }
}
