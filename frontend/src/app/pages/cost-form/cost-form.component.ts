import {
  Component,
  ElementRef,
  Injector,
  OnInit,
  afterNextRender,
  effect,
  inject,
  input,
  computed,
  signal,
  viewChild,
} from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { HttpErrorResponse } from '@angular/common/http';
import { CostService } from '../../services/cost.service';
import { CarDto } from '../../models/cars';
import { UserDto } from '../../models/users';
import { CreateCostRequest } from '../../models/costs';
import { CostCategoryDto } from '../../models/cost-categories';
import { extractApiErrorMessage } from '../../models/api-error';
import { localeDecimalValidator, parseLocaleNumber } from '../../models/locale-number';
import { FieldErrorComponent } from '../../components/field-error/field-error.component';

@Component({
  selector: 'app-cost-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TranslateModule, FieldErrorComponent],
  templateUrl: './cost-form.component.html',
  styleUrl: './cost-form.component.scss',
})
export class CostFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly costService = inject(CostService);
  private readonly injector = inject(Injector);

  readonly car = input.required<CarDto>();
  readonly users = input.required<UserDto[]>();
  readonly costCategories = input.required<CostCategoryDto[]>();

  readonly message = signal('');
  readonly error = signal('');

  readonly formEl = viewChild<ElementRef<HTMLFormElement>>('transactionFormEl');
  readonly statusRegion = viewChild<ElementRef<HTMLElement>>('statusRegion');

  readonly transactionForm = this.fb.group({
    buyerId: [0, [Validators.required, Validators.min(1)]],
    costCategoryId: [0, [Validators.required, Validators.min(1)]],
    price: ['0', [Validators.required, localeDecimalValidator(0)]],
    quantity: ['1', [Validators.required, localeDecimalValidator(0)]],
    dayOfTransaction: [new Date().toISOString().slice(0, 10), Validators.required],
    odometer: [''],
    notes: [''],
  });

  private readonly selectedCategoryId = signal(0);

  /** The category chosen in the form; drives the unit suffix and the odometer requirement. */
  readonly selectedCategory = computed(
    () => this.costCategories().find((c) => c.costCategoryId === this.selectedCategoryId()) ?? null,
  );

  constructor() {
    effect(() => {
      if (this.message()) {
        this.statusRegion()?.nativeElement.focus();
      }
    });
  }

  ngOnInit(): void {
    this.transactionForm.controls.costCategoryId.valueChanges.subscribe((id) => {
      this.selectedCategoryId.set(Number(id));
      this.applyOdometerValidators();
    });

    const firstCategory = this.costCategories()[0];
    if (firstCategory) {
      this.transactionForm.patchValue({ costCategoryId: firstCategory.costCategoryId });
    }

    const firstUser = this.users()[0];
    if (firstUser) {
      this.transactionForm.patchValue({ buyerId: firstUser.userId });
    }
  }

  /** Odometer is mandatory only for categories flagged `requiresOdometer`. */
  private applyOdometerValidators(): void {
    const control = this.transactionForm.controls.odometer;
    const required = this.selectedCategory()?.requiresOdometer ?? false;
    control.setValidators(
      required
        ? [Validators.required, Validators.pattern(/^[1-9]\d*$/)]
        : [Validators.pattern(/^([1-9]\d*)?$/)],
    );
    control.updateValueAndValidity();
  }

  private focusFirstInvalidControl(): void {
    afterNextRender(
      () => {
        this.formEl()?.nativeElement.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus();
      },
      { injector: this.injector },
    );
  }

  saveTransaction(): void {
    this.message.set('');
    this.error.set('');

    const carId = this.car().carId;
    if (this.transactionForm.invalid) {
      this.transactionForm.markAllAsTouched();
      this.error.set('carWorkspace.messages.transactionFormInvalid');
      this.focusFirstInvalidControl();
      return;
    }

    const value = this.transactionForm.getRawValue();
    const request: CreateCostRequest = {
      carId,
      buyerId: Number(value.buyerId),
      costCategoryId: Number(value.costCategoryId),
      price: parseLocaleNumber(value.price) ?? 0,
      quantity: parseLocaleNumber(value.quantity) ?? 0,
      dayOfTransaction: value.dayOfTransaction ?? new Date().toISOString().slice(0, 10),
      odometer: value.odometer ? Number(value.odometer) : undefined,
      notes: value.notes ?? undefined,
    };

    this.costService.createCost(request).subscribe({
      next: () => {
        this.message.set('carWorkspace.messages.transactionSaved');
        this.transactionForm.reset({
          buyerId: this.users()[0]?.userId ?? 0,
          costCategoryId: value.costCategoryId,
          price: '0',
          quantity: '1',
          dayOfTransaction: new Date().toISOString().slice(0, 10),
          odometer: '',
          notes: '',
        });
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(extractApiErrorMessage(err, 'carWorkspace.messages.transactionSaveFailed'));
      },
    });
  }
}
