import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { CostCategoryService } from '../services/cost-category.service';
import { CostCategoryDto } from '../models/cost-categories';

export const costCategoriesResolver: ResolveFn<CostCategoryDto[]> = () =>
  inject(CostCategoryService).getCostCategories();
