/**
 * Matches the backend `CostType` enum. Responses always use the uppercase form;
 * request payloads are parsed case-insensitively, so sending uppercase is safe.
 */
export type CostType = 'VARIABLE' | 'FIXED';

export interface CostDto {
  costId: number;
  carId: number;
  buyerId: number;
  costCategoryId: number;
  /** Denormalised from the cost category for display. */
  costCategoryName: string;
  unit: string;
  price: number;
  quantity: number;
  dayOfTransaction: string;
  /** Derived from the cost category. */
  costType: CostType;
  odometer: number | null;
  notes: string | null;
}

export interface CreateCostRequest {
  carId: number;
  buyerId: number;
  costCategoryId: number;
  price: number;
  quantity: number;
  dayOfTransaction: string;
  /** Required by the backend when the category has `requiresOdometer`. */
  odometer?: number;
  notes?: string;
}
