import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DriveService } from '../../services/drive.service';
import { Drive } from '../../models/drives';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-drives-list',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './drives-list.component.html',
  styleUrl: './drives-list.component.scss'
})
export class DrivesListComponent implements OnInit {
  private readonly driveService = inject(DriveService);

  drives = signal<Drive[]>([]);
  isLoading = signal(true);
  sortBy = signal<'date' | 'driver' | 'distance'>('date');

  ngOnInit(): void {
    this.loadDrives();
  }

  private loadDrives(): void {
    this.isLoading.set(true);
    this.driveService.getDrives().subscribe({
      next: drives => {
        this.drives.set(drives);
        this.isLoading.set(false);
      }
    });
  }

  setSortBy(sortOption: 'date' | 'driver' | 'distance'): void {
    this.sortBy.set(sortOption);
  }

  getSortedDrives(): Drive[] {
    const drives = [...this.drives()];
    const sortOption = this.sortBy();

    return drives.sort((a, b) => {
      switch (sortOption) {
        case 'date':
          return new Date(b.date).getTime() - new Date(a.date).getTime();
        case 'driver':
          return a.driver.localeCompare(b.driver);
        case 'distance':
          return b.distance - a.distance;
        default:
          return 0;
      }
    });
  }

  getTotalDistance(): number {
    return this.drives().reduce((sum, drive) => sum + drive.distance, 0);
  }

  deleteDrive(id: string | undefined): void {
    if (id && confirm('Are you sure you want to delete this drive?')) {
      this.driveService.deleteDrive(id).subscribe({
        next: () => {
          this.loadDrives();
        }
      });
    }
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }
}
