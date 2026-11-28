import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { CostCategoryManagementComponent } from './cost-category-management.component';
import { CostCategoryService } from '../../services/cost-category.service';
import { CostCategoryDto } from '../../models/cost-categories';

describe('CostCategoryManagementComponent', () => {
  let fixture: ComponentFixture<CostCategoryManagementComponent>;
  let component: CostCategoryManagementComponent;
  let service: Mocked<
    Pick<
      CostCategoryService,
      'getCostCategories' | 'createCostCategory' | 'updateCostCategory' | 'deleteCostCategory'
    >
  >;

  const fuel: CostCategoryDto = {
    costCategoryId: 1,
    name: 'Fuel',
    costType: 'VARIABLE',
    unit: 'Liter',
    requiresOdometer: true,
    fuel: true,
  };

  beforeEach(async () => {
    service = {
      getCostCategories: vi.fn().mockReturnValue(of([fuel])),
      createCostCategory: vi.fn().mockReturnValue(of(fuel)),
      updateCostCategory: vi.fn().mockReturnValue(of(fuel)),
      deleteCostCategory: vi.fn().mockReturnValue(of(undefined)),
    };

    await TestBed.configureTestingModule({
      imports: [CostCategoryManagementComponent],
      providers: [provideTranslateService(), { provide: CostCategoryService, useValue: service }],
    }).compileComponents();

    fixture = TestBed.createComponent(CostCategoryManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads and lists the categories', () => {
    expect(component.categories()).toEqual([fuel]);
    expect(fixture.nativeElement.textContent).toContain('Fuel');
  });

  it('preselects the fixed cost type, also after a reset', () => {
    expect(component.form.controls.costType.value).toBe('FIXED');

    component.edit(fuel);
    component.cancelEdit();
    expect(component.form.controls.costType.value).toBe('FIXED');
  });

  it('does not submit an invalid form', () => {
    component.submit();

    expect(service.createCostCategory).not.toHaveBeenCalled();
    expect(component.error()).toBe('costCategoryManagement.messages.formInvalid');
  });

  it('creates a category from the form', () => {
    component.form.setValue({
      name: ' Insurance ',
      costType: 'FIXED',
      unit: 'pcs',
      requiresOdometer: false,
      fuel: false,
    });

    component.submit();

    expect(service.createCostCategory).toHaveBeenCalledWith({
      name: 'Insurance',
      costType: 'FIXED',
      unit: 'pcs',
      requiresOdometer: false,
      fuel: false,
    });
    expect(component.message()).toBe('costCategoryManagement.messages.createSuccess');
  });

  it('updates the edited category', () => {
    component.edit(fuel);
    component.submit();

    expect(service.updateCostCategory).toHaveBeenCalledWith(
      1,
      expect.objectContaining({ name: 'Fuel' }),
    );
    expect(component.editingId()).toBeNull();
  });

  it('maps a 409 on create to the name-in-use message', () => {
    service.createCostCategory.mockReturnValue(throwError(() => ({ status: 409 })));
    component.form.patchValue({ name: 'Fuel', unit: 'Liter' });

    component.submit();

    expect(component.error()).toBe('costCategoryManagement.messages.nameInUse');
  });

  it('maps a 409 on delete to the dependency message', () => {
    service.deleteCostCategory.mockReturnValue(throwError(() => ({ status: 409 })));

    component.delete(1);

    expect(component.error()).toBe('costCategoryManagement.messages.deleteDependencyBlocked');
  });
});
