import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { UploadComponent } from './upload.component';
import { ImportService } from '../../services/import.service';
import { CarDto } from '../../models/cars';
import { ImportResult } from '../../models/imports';

describe('UploadComponent', () => {
  let fixture: ComponentFixture<UploadComponent>;
  let component: UploadComponent;
  let importService: Mocked<Pick<ImportService, 'importDrives' | 'importCosts'>>;

  const car: CarDto = { carId: 7, name: 'Wagon', plateNumber: 'M-XY-9', ownerId: 1, data: null };

  function fileEvent(): Event {
    const file = new File(['146739;Stefan;Meyer;27.12.2020'], 'drives.csv', { type: 'text/csv' });
    return { target: { files: [file] } } as unknown as Event;
  }

  beforeEach(async () => {
    importService = {
      importDrives: vi.fn().mockReturnValue(of({ imported: 1, errors: [] } as ImportResult)),
      importCosts: vi.fn().mockReturnValue(of({ imported: 0, errors: [] } as ImportResult)),
    };

    await TestBed.configureTestingModule({
      imports: [UploadComponent],
      providers: [
        provideTranslateService(),
        provideRouter([]),
        { provide: ImportService, useValue: importService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UploadComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('car', car);
    fixture.detectChanges();
  });

  it('defaults to the English number format', () => {
    expect(component.locale()).toBe('en');
  });

  it('does not import until a file is chosen', () => {
    component.importDrives();
    expect(importService.importDrives).not.toHaveBeenCalled();
  });

  it('imports the chosen drives file with the selected locale', () => {
    component.setLocale('de');
    component.onFileSelected(fileEvent(), 'drives');

    component.importDrives();

    expect(importService.importDrives).toHaveBeenCalledWith(7, expect.any(File), 'de');
    expect(component.driveResult()?.imported).toBe(1);
  });

  it('surfaces per-row errors when the file is rejected', () => {
    importService.importDrives.mockReturnValue(
      of({ imported: 0, errors: [{ line: 2, message: 'No user named Max Bauer exists' }] }),
    );
    component.onFileSelected(fileEvent(), 'drives');

    component.importDrives();

    expect(component.driveResult()?.errors).toHaveLength(1);
    expect(component.driveResult()?.errors[0].line).toBe(2);
  });
});
