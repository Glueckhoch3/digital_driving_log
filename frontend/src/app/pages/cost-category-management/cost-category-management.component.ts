import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { CostCategoryService } from '../../services/cost-category.service';
import { CostCategoryDto, CreateCostCategoryRequest } from '../../models/cost-categories';
import { CostType } from '../../models/costs';
import { extractApiErrorMessage } from '../../models/api-error';
import { FieldErrorComponent } from '../../components/field-error/field-error.component';

@Component({
  selector: 'app-cost-category-management',
  standalone: true,
  imports: [ReactiveFormsModule, TranslateModule, FieldErrorComponent],
  templateUrl: './cost-category-management.component.html',
  styleUrl: './cost-category-management.component.scss',
})
export class CostCategoryManagementComponent implements OnInit {
  private readonly service = inject(CostCategoryService);
  private readonly fb = inject(FormBuilder);

  readonly categories = signal<CostCategoryDto[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly message = signal('');
  readonly editingId = signal<number | null>(null);

  readonly costTypes: CostType[] = ['VARIABLE', 'FIXED'];

  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(63)]],
    costType: ['FIXED' as CostType, Validators.required],
    unit: ['', [Validators.required, Validators.maxLength(20)]],
    requiresOdometer: [false],
    fuel: [false],
  });

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading.set(true);
    this.service.getCostCategories().subscribe({
      next: (categories) => {
        this.categories.set(categories);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(extractApiErrorMessage(err, 'costCategoryManagement.messages.loadFailed'));
        this.loading.set(false);
      },
    });
  }

  submit(): void {
    this.message.set('');
    this.error.set('');

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('costCategoryManagement.messages.formInvalid');
      return;
    }

    const value = this.form.getRawValue();
    const payload: CreateCostCategoryRequest = {
      name: (value.name ?? '').trim(),
      costType: value.costType ?? 'FIXED',
      unit: (value.unit ?? '').trim(),
      requiresOdometer: !!value.requiresOdometer,
      fuel: !!value.fuel,
    };

    const editingId = this.editingId();
    const request$ =
      editingId == null
        ? this.service.createCostCategory(payload)
        : this.service.updateCostCategory(editingId, payload);
    const prefix = editingId == null ? 'create' : 'update';

    request$.subscribe({
      next: () => {
        this.message.set(`costCategoryManagement.messages.${prefix}Success`);
        this.resetForm();
        this.loadData();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err?.status === 409
            ? 'costCategoryManagement.messages.nameInUse'
            : extractApiErrorMessage(err, `costCategoryManagement.messages.${prefix}Failed`),
        );
      },
    });
  }

  edit(category: CostCategoryDto): void {
    this.editingId.set(category.costCategoryId);
    this.form.patchValue({
      name: category.name,
      costType: category.costType,
      unit: category.unit,
      requiresOdometer: category.requiresOdometer,
      fuel: category.fuel,
    });
  }

  cancelEdit(): void {
    this.resetForm();
  }

  delete(id: number): void {
    this.message.set('');
    this.error.set('');
    this.service.deleteCostCategory(id).subscribe({
      next: () => {
        this.message.set('costCategoryManagement.messages.deleteSuccess');
        if (this.editingId() === id) {
          this.resetForm();
        }
        this.loadData();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err?.status === 409
            ? 'costCategoryManagement.messages.deleteDependencyBlocked'
            : extractApiErrorMessage(err, 'costCategoryManagement.messages.deleteFailed'),
        );
      },
    });
  }

  private resetForm(): void {
    this.editingId.set(null);
    this.form.reset({
      name: '',
      costType: 'FIXED',
      unit: '',
      requiresOdometer: false,
      fuel: false,
    });
  }
}
