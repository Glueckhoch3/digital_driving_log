import {
  Component,
  ElementRef,
  Injector,
  OnInit,
  afterNextRender,
  effect,
  inject,
  input,
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
import { CreateCostRequest, CostType } from '../../models/costs';
import { extractApiErrorMessage } from '../../models/api-error';
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

  readonly message = signal('');
  readonly error = signal('');

  readonly formEl = viewChild<ElementRef<HTMLFormElement>>('transactionFormEl');
  readonly statusRegion = viewChild<ElementRef<HTMLElement>>('statusRegion');

  readonly transactionForm = this.fb.group({
    buyerId: [0, [Validators.required, Validators.min(1)]],
    description: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(63)]],
    price: [0, [Validators.required, Validators.min(0)]],
    quantity: [1, [Validators.required, Validators.min(1)]],
    dayOfTransaction: [new Date().toISOString().slice(0, 10), Validators.required],
    costType: ['VARIABLE' as CostType, Validators.required],
    notes: [''],
  });

  constructor() {
    effect(() => {
      if (this.message()) {
        this.statusRegion()?.nativeElement.focus();
      }
    });
  }

  ngOnInit(): void {
    const firstUser = this.users()[0];
    if (firstUser) {
      this.transactionForm.patchValue({ buyerId: firstUser.userId });
    }
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
      description: value.description ?? '',
      price: Number(value.price),
      quantity: Number(value.quantity),
      dayOfTransaction: value.dayOfTransaction ?? new Date().toISOString().slice(0, 10),
      costType: (value.costType ?? 'VARIABLE') as CostType,
      notes: value.notes ?? undefined,
    };

    this.costService.createCost(request).subscribe({
      next: () => {
        this.message.set('carWorkspace.messages.transactionSaved');
        this.transactionForm.reset({
          buyerId: this.users()[0]?.userId ?? 0,
          description: '',
          price: 0,
          quantity: 1,
          dayOfTransaction: new Date().toISOString().slice(0, 10),
          costType: 'VARIABLE',
          notes: '',
        });
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(extractApiErrorMessage(err, 'carWorkspace.messages.transactionSaveFailed'));
      },
    });
  }
}
