import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CostService } from '../../services/cost.service';
import { Cost, CostSummary } from '../../models/costs';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-costs',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TranslateModule],
  templateUrl: './costs.component.html',
  styleUrl: './costs.component.scss'
})
export class CostsComponent implements OnInit {
  private readonly costService = inject(CostService);
  private readonly fb = inject(FormBuilder);

  costs = signal<Cost[]>([]);
  costSummary = signal<CostSummary | null>(null);
  costForm!: FormGroup;
  isLoading = signal(true);
  isSubmitting = signal(false);
  successMessage = signal('');

  ngOnInit(): void {
    this.initializeForm();
    this.loadCosts();
    this.loadCostSummary();
  }

  private initializeForm(): void {
    this.costForm = this.fb.group({
      type: ['fixed', Validators.required],
      price: [0, [Validators.required, Validators.min(0)]],
      amount: [0, [Validators.required, Validators.min(0)]],
      shareholder: ['', [Validators.required, Validators.minLength(2)]],
      date: [new Date().toISOString().split('T')[0], Validators.required],
      description: ['', [Validators.required, Validators.minLength(2)]],
      category: [''],
      affectedPeriod: ['']
    });
  }

  private loadCosts(): void {
    this.isLoading.set(true);
    this.costService.getCostsForCar(1).subscribe({ //Change carId:1 when multiple cars implemented
      next: costs => {
        this.costs.set(costs);
        this.isLoading.set(false);
      }
    });
  }

  private loadCostSummary(): void {
    this.costService.getCostDistribution().subscribe({
      next: summary => {
        this.costSummary.set(summary);
      }
    });
  }

  onSubmit(): void {
    if (this.costForm.valid) {
      this.isSubmitting.set(true);
      const formValue = this.costForm.value;

      this.costService.createCost(1,formValue).subscribe({  //Change carId:1 when multiple cars implemented
        next: () => {
          this.successMessage.set('Cost recorded successfully!');
          this.loadCosts();
          this.loadCostSummary();
          this.costForm.reset({
            type: 'fixed',
            price: 0,
            amount: 0,
            date: new Date().toISOString().split('T')[0],
            description: '',
            category: '',
            affectedPeriod: ''
          });
          this.isSubmitting.set(false);

          setTimeout(() => this.successMessage.set(''), 3000);
        }
      });
    }
  }

  deleteCost(id: string | undefined): void {
    if (id && confirm('Are you sure you want to delete this cost?')) {
      this.costService.deleteCost(id).subscribe({
        next: () => {
          this.loadCosts();
          this.loadCostSummary();
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
