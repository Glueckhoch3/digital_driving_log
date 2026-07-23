import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { CostsListComponent } from './costs-list.component';
import { CostService } from '../../services/cost.service';
import { CarDto } from '../../models/cars';
import { UserDto } from '../../models/users';
import { Page } from '../../models/page';
import { CostDto } from '../../models/costs';

describe('CostsListComponent', () => {
  let fixture: ComponentFixture<CostsListComponent>;
  let component: CostsListComponent;
  let costService: Mocked<Pick<CostService, 'getCostsForCar'>>;

  const car: CarDto = { carId: 5, name: 'City Car', plateNumber: 'M-AB-1', ownerId: 1, data: null };
  const users: UserDto[] = [
    { userId: 1, firstname: 'Anna', lastname: 'Meyer', driverLicense: true, birthday: null },
  ];

  const costPage = (costs: CostDto[], totalPages: number): Page<CostDto> => ({
    content: costs,
    totalElements: costs.length,
    totalPages,
    number: 0,
    size: 10,
  });

  beforeEach(async () => {
    costService = { getCostsForCar: vi.fn().mockReturnValue(of(costPage([], 0))) };

    await TestBed.configureTestingModule({
      imports: [CostsListComponent],
      providers: [
        provideTranslateService(),
        provideRouter([]),
        { provide: CostService, useValue: costService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CostsListComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('car', car);
    fixture.componentRef.setInput('users', users);
  });

  it('shows the empty state when there are no transactions', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(costService.getCostsForCar).toHaveBeenCalledWith(5, {
      page: 0,
      size: 10,
      sort: 'dayOfTransaction,desc',
    });
    expect(fixture.nativeElement.textContent).toContain('carWorkspace.transactions.empty');
  });

  it('requests the next page when changePage(1) is called', async () => {
    costService.getCostsForCar.mockReturnValue(of(costPage([], 2)));
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    component.changePage(1);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(costService.getCostsForCar).toHaveBeenCalledWith(5, {
      page: 1,
      size: 10,
      sort: 'dayOfTransaction,desc',
    });
  });
});
