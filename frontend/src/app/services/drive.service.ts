import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, BehaviorSubject, catchError } from 'rxjs';
import { FuelRefill, Drive } from '../models/drives';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DriveService {
  private readonly apiUrl = `${environment.apiUrl}/drives`;

  constructor(private readonly http: HttpClient) {}

  private readonly drives = signal<Drive[]>([
    {
      id: '1',
      date: new Date(2026, 1, 1),
      driver: 'John Doe',
      distance: 150,
      notes: 'Regular drive'
    }
  ]);

  private readonly drives$ = new BehaviorSubject<Drive[]>(this.drives());

  getDrivesForCar(carID: string): Observable<Drive[]> {
    return this.http.get<Drive[]>(`${this.apiUrl}/drives/${carID}`).pipe(
      catchError(() => {
        // Fallback to mock data if API is not available
        return this.drives$;
      })
    );
  }

  getDriveById(id: string): Observable<Drive | undefined> {
    return of(this.drives().find(d => d.id === id));
  }

  createDrive(driveRequest: Drive): Observable<Drive> {
    return this.http.post<Drive>(`${this.apiUrl}/drives`, driveRequest).pipe(
      catchError(() => {
        // Fallback to mock data if API is not available
        const newDrive: Drive = {
          id: Date.now().toString(),
          ...driveRequest
        };

        const currentDrives = this.drives();
        this.drives.set([...currentDrives, newDrive]);
        this.drives$.next(this.drives());

        return of(newDrive);
      })
    );
  }

  updateDrive(id: string, drive: Partial<Drive>): Observable<Drive> {
    const currentDrives = this.drives();
    const index = currentDrives.findIndex(d => d.id === id);

    if (index !== -1) {
      const updated = {
        ...currentDrives[index],
        ...drive
      };
      currentDrives[index] = updated;
      this.drives.set([...currentDrives]);
      this.drives$.next(this.drives());
      return of(updated);
    }

    return of(currentDrives[index]);
  }

  deleteDrive(id: string): Observable<void> {
    const currentDrives = this.drives();
    this.drives.set(currentDrives.filter(d => d.id !== id));
    this.drives$.next(this.drives());
    return of(void 0);
  }

  addFuelRefill(driveId: string, request: FuelRefill): Observable<FuelRefill> {
    const newRefill: FuelRefill = {
      id: Date.now().toString(),
      ...request
    };

    return of(newRefill);
  }
}
