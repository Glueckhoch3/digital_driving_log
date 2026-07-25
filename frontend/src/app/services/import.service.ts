import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CsvLocale, ImportResult } from '../models/imports';

/**
 * Uploads semicolon-separated CSV files to the car-scoped import endpoints.
 * `locale` decides how `amount`/`price` decimals are read (en: `.`, de: `,`).
 */
@Injectable({ providedIn: 'root' })
export class ImportService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  importDrives(carId: number, file: File, locale: CsvLocale): Observable<ImportResult> {
    return this.http.post<ImportResult>(
      `${this.apiUrl}/vehicles/${carId}/drives/import`,
      this.body(file, locale),
    );
  }

  importCosts(carId: number, file: File, locale: CsvLocale): Observable<ImportResult> {
    return this.http.post<ImportResult>(
      `${this.apiUrl}/vehicles/${carId}/costs/import`,
      this.body(file, locale),
    );
  }

  private body(file: File, locale: CsvLocale): FormData {
    const form = new FormData();
    form.append('file', file);
    form.append('locale', locale);
    return form;
  }
}
