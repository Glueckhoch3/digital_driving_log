export type CostType = 'fixed' | 'variable';

export interface CostDto {
  costId: number;
  carId: number;
  buyerId: number;
  transactionObject: string;
  price: number;
  amount: number;
  dayOfTransaction: string;
  costType: string;
  notes: string | null;
}

export interface CreateCostRequest {
  carId: number;
  buyerId: number;
  transactionObject: string;
  price: number;
  amount: number;
  dayOfTransaction: string;
  costType: CostType;
  notes?: string;
}
