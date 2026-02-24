import { Injectable, signal } from '@angular/core';
import { Observable, of, BehaviorSubject } from 'rxjs';
import { Drive, FuelRefill, CreateDriveRequest, CreateFuelRefillRequest } from '../models/drive.model';

@Injectable({
  providedIn: 'root'
})
export class DriveService {
  private drives = signal<Drive[]>([
    {
      id: '1',
      date: new Date(2026, 1, 1),
      driver: 'John Doe',
      distance: 150,
      notes: 'Regular drive',
      createdAt: new Date(),
      updatedAt: new Date()
    },
    {
      id: '2',
      date: new Date(2026, 1, 5),
      driver: 'Jane Smith',
      distance: 200,
      notes: 'Long trip',
      createdAt: new Date(),
      updatedAt: new Date()
    }
  ]);

  private drives$ = new BehaviorSubject<Drive[]>(this.drives());

  getDrives(): Observable<Drive[]> {
    return this.drives$;
  }

  getDriveById(id: string): Observable<Drive | undefined> {
    return of(this.drives().find(d => d.id === id));
  }

  createDrive(request: CreateDriveRequest): Observable<Drive> {
    const newDrive: Drive = {
      id: Date.now().toString(),
      ...request,
      createdAt: new Date(),
      updatedAt: new Date(),
      fuelRefills: []
    };

    const currentDrives = this.drives();
    this.drives.set([...currentDrives, newDrive]);
    this.drives$.next(this.drives());

    return of(newDrive);
  }

  updateDrive(id: string, drive: Partial<Drive>): Observable<Drive> {
    const currentDrives = this.drives();
    const index = currentDrives.findIndex(d => d.id === id);

    if (index !== -1) {
      const updated = {
        ...currentDrives[index],
        ...drive,
        updatedAt: new Date()
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

  addFuelRefill(driveId: string, request: CreateFuelRefillRequest): Observable<FuelRefill> {
    const newRefill: FuelRefill = {
      id: Date.now().toString(),
      ...request,
      costPerLiter: request.cost / request.liters,
      createdAt: new Date(),
      updatedAt: new Date()
    };

    const currentDrives = this.drives();
    const driveIndex = currentDrives.findIndex(d => d.id === driveId);

    if (driveIndex !== -1) {
      if (!currentDrives[driveIndex].fuelRefills) {
        currentDrives[driveIndex].fuelRefills = [];
      }
      currentDrives[driveIndex].fuelRefills?.push(newRefill);
      this.drives.set([...currentDrives]);
      this.drives$.next(this.drives());
    }

    return of(newRefill);
  }

  removeFuelRefill(driveId: string, refillId: string): Observable<void> {
    const currentDrives = this.drives();
    const driveIndex = currentDrives.findIndex(d => d.id === driveId);

    if (driveIndex !== -1 && currentDrives[driveIndex].fuelRefills) {
      currentDrives[driveIndex].fuelRefills = currentDrives[driveIndex].fuelRefills?.filter(
        f => f.id !== refillId
      );
      this.drives.set([...currentDrives]);
      this.drives$.next(this.drives());
    }

    return of(void 0);
  }
}
