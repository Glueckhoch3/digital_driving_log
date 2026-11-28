import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { CostFormComponent } from './cost-form.component';
import { CostService } from '../../services/cost.service';
import { CarDto } from '../../models/cars';
import { UserDto } from '../../models/users';
import { CostDto } from '../../models/costs';
import { CostCategoryDto } from '../../models/cost-categories';

describe('CostFormComponent', () => {
  let fixture: ComponentFixture<CostFormComponent>;
  let component: CostFormComponent;
  let costService: Mocked<Pick<CostService, 'createCost'>>;

  const car: CarDto = { carId: 5, name: 'City Car', plateNumber: 'M-AB-1', ownerId: 1, data: null };
  const users: UserDto[] = [
    { userId: 1, firstname: 'Anna', lastname: 'Meyer', driverLicense: true, birthday: null },
  ];

  const categories: CostCategoryDto[] = [
    {
      costCategoryId: 10,
      name: 'Fuel',
      costType: 'VARIABLE',
      unit: 'Liter',
      requiresOdometer: true,
      fuel: true,
    },
    {
      costCategoryId: 11,
      name: 'Insurance',
      costType: 'FIXED',
      unit: 'pcs',
      requiresOdometer: false,
      fuel: false,
    },
  ];

  beforeEach(async () => {
    costService = { createCost: vi.fn().mockReturnValue(of({} as CostDto)) };

    await TestBed.configureTestingModule({
      imports: [CostFormComponent],
      providers: [
        provideTranslateService(),
        provideRouter([]),
        { provide: CostService, useValue: costService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CostFormComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('car', car);
    fixture.componentRef.setInput('users', users);
    fixture.componentRef.setInput('costCategories', categories);
    fixture.detectChanges();
  });

  it('does not submit an invalid transaction form', () => {
    component.transactionForm.setValue({
      buyerId: 0,
      costCategoryId: 0,
      price: '0',
      quantity: '1',
      dayOfTransaction: '2025-01-01',
      odometer: '',
      notes: '',
    });

    component.saveTransaction();
    fixture.detectChanges();

    expect(costService.createCost).not.toHaveBeenCalled();
    expect(component.error()).toBe('carWorkspace.messages.transactionFormInvalid');
    expect(fixture.nativeElement.textContent).toContain(
      'carWorkspace.messages.transactionFormInvalid',
    );
  });

  it('preselects the first category and shows its unit', () => {
    expect(component.transactionForm.controls.costCategoryId.value).toBe(10);
    expect(component.selectedCategory()?.unit).toBe('Liter');
  });

  it('requires an odometer only for categories that demand it', () => {
    component.transactionForm.controls.costCategoryId.setValue(10);
    expect(component.transactionForm.controls.odometer.invalid).toBe(true);

    component.transactionForm.controls.odometer.setValue('1234');
    expect(component.transactionForm.controls.odometer.valid).toBe(true);

    component.transactionForm.controls.costCategoryId.setValue(11);
    component.transactionForm.controls.odometer.setValue('');
    expect(component.transactionForm.controls.odometer.valid).toBe(true);
  });

  it('saves a valid fuel transaction with odometer', () => {
    component.transactionForm.setValue({
      buyerId: 1,
      costCategoryId: 10,
      price: '42',
      quantity: '35,5',
      dayOfTransaction: '2025-01-01',
      odometer: '12345',
      notes: '',
    });

    component.saveTransaction();

    expect(costService.createCost).toHaveBeenCalledWith(
      expect.objectContaining({
        carId: 5,
        buyerId: 1,
        costCategoryId: 10,
        price: 42,
        quantity: 35.5,
        odometer: 12345,
      }),
    );
    expect(component.message()).toBe('carWorkspace.messages.transactionSaved');
  });

  it('omits the odometer for categories without the requirement when left empty', () => {
    component.transactionForm.setValue({
      buyerId: 1,
      costCategoryId: 11,
      price: '300',
      quantity: '1',
      dayOfTransaction: '2025-01-01',
      odometer: '',
      notes: '',
    });

    component.saveTransaction();

    const request = costService.createCost.mock.calls[0][0];
    expect(request.odometer).toBeUndefined();
  });
});
