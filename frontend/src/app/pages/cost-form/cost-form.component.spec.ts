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

describe('CostFormComponent', () => {
  let fixture: ComponentFixture<CostFormComponent>;
  let component: CostFormComponent;
  let costService: Mocked<Pick<CostService, 'createCost'>>;

  const car: CarDto = { carId: 5, name: 'City Car', plateNumber: 'M-AB-1', ownerId: 1, data: null };
  const users: UserDto[] = [
    { userId: 1, firstname: 'Anna', lastname: 'Meyer', driverLicense: true, birthday: null },
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
    fixture.detectChanges();
  });

  it('does not submit an invalid transaction form', () => {
    component.transactionForm.setValue({
      buyerId: 0,
      description: '',
      price: '0',
      quantity: '1',
      dayOfTransaction: '2025-01-01',
      costType: 'VARIABLE',
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

  it('saves a valid transaction', () => {
    component.transactionForm.setValue({
      buyerId: 1,
      description: 'Oil change',
      price: '42',
      quantity: '1',
      dayOfTransaction: '2025-01-01',
      costType: 'FIXED',
      notes: '',
    });

    component.saveTransaction();

    expect(costService.createCost).toHaveBeenCalledWith(
      expect.objectContaining({ carId: 5, buyerId: 1, description: 'Oil change', price: 42 }),
    );
    expect(component.message()).toBe('carWorkspace.messages.transactionSaved');
  });
});
