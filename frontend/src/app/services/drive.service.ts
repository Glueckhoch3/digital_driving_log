import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CreateDriveRequest, DriveDto } from '../models/drives';
import { Page } from '../models/page';
import { PageQuery, toHttpParams } from '../models/page-query';

@Injectable({ providedIn: 'root' })
export class DriveService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  getDriveById(driveId: number): Observable<DriveDto> {
    return this.http.get<DriveDto>(`${this.apiUrl}/drives/${driveId}`);
  }

  getDrivesForCar(carId: number, query?: PageQuery): Observable<Page<DriveDto>> {
    const params: HttpParams = toHttpParams(query);
    return this.http.get<Page<DriveDto>>(`${this.apiUrl}/vehicles/${carId}/drives`, { params });
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
