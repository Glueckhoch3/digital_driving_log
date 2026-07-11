/**
 * Matches the backend `CostType` enum. Responses always use the uppercase form;
 * request payloads are parsed case-insensitively, so sending uppercase is safe.
 */
export type CostType = 'VARIABLE' | 'FIXED';

export interface CostDto {
  costId: number;
  carId: number;
  buyerId: number;
  description: string;
  price: number;
  quantity: number;
  dayOfTransaction: string;
  costType: CostType;
  notes: string | null;
}

export interface CreateCostRequest {
  carId: number;
  buyerId: number;
  description: string;
  price: number;
  quantity: number;
  dayOfTransaction: string;
  costType: CostType;
  notes?: string;
}
