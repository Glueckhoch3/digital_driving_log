import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { DriveService } from '../../services/drive.service';
import { Drive } from '../../models/drive.model';

@Component({
  selector: 'app-drive-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './drive-form.component.html',
  styleUrl: './drive-form.component.scss'
})
export class DriveFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private driveService = inject(DriveService);

  driveForm!: FormGroup;
  isSubmitting = signal(false);
  successMessage = signal('');
  errorMessage = signal('');

  showRefill = signal(false);
  refill = {
    date: [new Date().toISOString().split('T')[0], Validators.required],
    liters: [0, [Validators.required, Validators.min(0)]],
    cost: [0, [Validators.required, Validators.min(0)]],
  };

  toggleRefill(): void {
    this.showRefill.update(v => !v);
  }

  ngOnInit(): void {
    this.initializeForm();
  }

  private initializeForm(): void {
    this.driveForm = this.fb.group({
      date: [new Date().toISOString().split('T')[0], Validators.required],
      driver: ['', [Validators.required, Validators.minLength(2)]],
      distance: [0, [Validators.required, Validators.min(0.1)]],
      notes: ['']
    });
  }

  onSubmit(): void {
    if (this.driveForm.valid) {
      this.isSubmitting.set(true);
      this.errorMessage.set('');
      this.successMessage.set('');

      const formValue = this.driveForm.value;
      const request = {
        date: new Date(formValue.date),
        driver: formValue.driver,
        distance: parseFloat(formValue.distance),
        notes: formValue.notes || undefined
      };

      this.driveService.createDrive(request).subscribe({
        next: () => {
          this.successMessage.set('Drive recorded successfully!');
          this.driveForm.reset({
            date: new Date().toISOString().split('T')[0],
            driver: '',
            distance: 0,
            notes: ''
          });
          this.isSubmitting.set(false);

          setTimeout(() => {
            this.successMessage.set('');
          }, 3000);
        },
        error: () => {
          this.errorMessage.set('Failed to record drive. Please try again.');
          this.isSubmitting.set(false);
        }

        // add costService.createCost(request).subscribe()
      });
    }
  }

  resetForm(): void {
    this.driveForm.reset({
      date: new Date().toISOString().split('T')[0],
      driver: '',
      distance: 0,
      notes: ''
    });
    this.errorMessage.set('');
    this.successMessage.set('');
  }
}
