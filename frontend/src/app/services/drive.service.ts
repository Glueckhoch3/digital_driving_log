import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CreateDriveRequest, DriveDto } from '../models/drives';

@Injectable({ providedIn: 'root' })
export class DriveService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  getDriveById(driveId: number): Observable<DriveDto> {
    return this.http.get<DriveDto>(`${this.apiUrl}/drives/${driveId}`);
  }

  getDrivesForCar(carId: number): Observable<DriveDto[]> {
    return this.http.get<DriveDto[]>(`${this.apiUrl}/vehicles/${carId}/drives`);
  }

  createDrive(request: CreateDriveRequest): Observable<DriveDto> {
    return this.http.post<DriveDto>(`${this.apiUrl}/drives`, request);
  }

  updateDrive(driveId: number, request: CreateDriveRequest): Observable<DriveDto> {
    return this.http.put<DriveDto>(`${this.apiUrl}/drives/${driveId}`, request);
  }

  deleteDrive(driveId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/drives/${driveId}`);
  }
}
