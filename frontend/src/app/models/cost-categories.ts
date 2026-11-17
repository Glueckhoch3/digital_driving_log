import { CostType } from './costs';

export interface CostCategoryDto {
  costCategoryId: number;
  name: string;
  costType: CostType;
  /** Unit of the booked quantity (e.g. "Liter"). */
  unit: string;
  /** When true, costs of this category must carry an odometer reading. */
  requiresOdometer: boolean;
  /** Marks fuel categories, which feed the consumption statistics. */
  fuel: boolean;
}

export type CreateCostCategoryRequest = Omit<CostCategoryDto, 'costCategoryId'>;
export type UpdateCostCategoryRequest = CreateCostCategoryRequest;
