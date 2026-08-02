import { Component, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ImportService } from '../../services/import.service';
import { CarDto } from '../../models/cars';
import { CsvLocale, ImportResult } from '../../models/imports';
import { extractApiErrorMessage } from '../../models/api-error';

type Kind = 'drives' | 'costs';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [RouterLink, TranslateModule],
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.scss',
})
export class UploadComponent {
  private readonly importService = inject(ImportService);

  readonly car = input.required<CarDto>();

  readonly locale = signal<CsvLocale>('en');

  readonly driveFile = signal<File | null>(null);
  readonly costFile = signal<File | null>(null);

  readonly driveResult = signal<ImportResult | null>(null);
  readonly costResult = signal<ImportResult | null>(null);

  readonly driveError = signal('');
  readonly costError = signal('');

  readonly driveBusy = signal(false);
  readonly costBusy = signal(false);

  setLocale(locale: CsvLocale): void {
    this.locale.set(locale);
  }

  onFileSelected(event: Event, kind: Kind): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    if (kind === 'drives') {
      this.driveFile.set(file);
      this.driveResult.set(null);
      this.driveError.set('');
    } else {
      this.costFile.set(file);
      this.costResult.set(null);
      this.costError.set('');
    }
  }

  importDrives(): void {
    const file = this.driveFile();
    if (!file) {
      return;
    }
    this.driveBusy.set(true);
    this.driveError.set('');
    this.importService.importDrives(this.car().carId, file, this.locale()).subscribe({
      next: (result) => {
        this.driveResult.set(result);
        this.driveBusy.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.driveError.set(extractApiErrorMessage(err, 'carWorkspace.upload.importFailed'));
        this.driveBusy.set(false);
      },
    });
  }

  importCosts(): void {
    const file = this.costFile();
    if (!file) {
      return;
    }
    this.costBusy.set(true);
    this.costError.set('');
    this.importService.importCosts(this.car().carId, file, this.locale()).subscribe({
      next: (result) => {
        this.costResult.set(result);
        this.costBusy.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.costError.set(extractApiErrorMessage(err, 'carWorkspace.upload.importFailed'));
        this.costBusy.set(false);
      },
    });
  }
}
