import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormsModule , Validators } from '@angular/forms';
import { DriveService } from '../../services/drive.service';
import { CostService } from '../../services/cost.service';
import { CreateCostRequest } from '../../models/cost.model';

@Component({
  selector: 'app-drive-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './drive-form.component.html',
  styleUrl: './drive-form.component.scss'
})
export class DriveFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly driveService = inject(DriveService);
  private readonly costService = inject(CostService);

  driveForm!: FormGroup;
  isSubmitting = signal(false);
  successMessage = signal('');
  errorMessage = signal('');

  showRefill = signal(false);

  toggleRefill(): void {
    this.showRefill.update(v => !v);
    if (this.showRefill()) {
      this.driveForm.addControl('liters', this.fb.control(0, [Validators.required, Validators.min(0.01)]));
      this.driveForm.addControl('cost', this.fb.control(0, [Validators.required, Validators.min(0.01)]));
      this.driveForm.addControl('refilldate', this.fb.control(new Date().toISOString().split('T')[0], Validators.required));
    } else {
      this.driveForm.removeControl('liters');
      this.driveForm.removeControl('cost');
      this.driveForm.removeControl('refilldate');
    }
  }

  ngOnInit(): void {
    this.initializeForm();
  }

  private initializeForm(): void {
    this.driveForm = this.fb.group({
      date: [new Date().toISOString().split('T')[0], Validators.required],
      driver: ['', [Validators.required, Validators.minLength(2)]],
      distance: [0, [Validators.required, Validators.min(1)]],
      notes: ['']
    });
  }

  onSubmit(): void {
    if (this.driveForm.valid) {
      this.isSubmitting.set(true);
      this.errorMessage.set('');
      this.successMessage.set('');

      const formValue = this.driveForm.value;
      const driveRequest = {
        date: new Date(formValue.date),
        driver: formValue.driver,
        distance: Number.parseFloat(formValue.distance),
        notes: formValue.notes || undefined
      };

      this.driveService.createDrive(driveRequest).subscribe({
        next: () => {
          if (this.showRefill()) {
            const costRequest: CreateCostRequest = {
              type: 'variable',
              price: formValue.amount,
              amount: formValue.liters,
              shareholder: formValue.refillname,
              date: new Date(formValue.refilldate),
              description: 'Fuel refill',
              category: 'Fuel'
            };
            this.costService.createCost(costRequest).subscribe({
              next: () => {
                this.successMessage.set('Drive and fuel cost recorded successfully!');
                this.resetForm();
                this.isSubmitting.set(false);
                setTimeout(() => {
                  this.successMessage.set('');
                }, 3000);
              },
              error: () => {
                this.errorMessage.set('Drive recorded but failed to record fuel cost.');
                this.resetForm();
                this.isSubmitting.set(false);
              }
            });
          } else {
            this.successMessage.set('Drive recorded successfully!');
            this.resetForm();
            this.isSubmitting.set(false);
            setTimeout(() => {
              this.successMessage.set('');
            }, 3000);
          }
        },
        error: () => {
          this.errorMessage.set('Failed to record drive. Please try again.');
          this.isSubmitting.set(false);
        }
      });
    }
  }

  resetForm(): void {
    this.driveForm.reset({
      distance: 0,
      driver: '',
      date: new Date().toISOString().split('T')[0],
      notes: '',
      refillname: '',
      liters: 0,
      cost: 0,
      refilldate: new Date().toISOString().split('T')[0]
    });
    this.errorMessage.set('');
    this.successMessage.set('');
  }
}
