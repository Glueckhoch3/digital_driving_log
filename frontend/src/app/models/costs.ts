export type CostType = 'fixed' | 'variable';

export interface Cost {
  id?: string;
  type: CostType;
  price: number;
  amount: number;
  shareholder: string;
  date: Date;
  description: string;
  category?: string;
}

export interface FixedCost extends Cost {
  type: 'fixed';
}

export interface VariableCost extends Cost {
  type: 'variable';
}

export interface CostDistribution {
  shareholder: string;
  totalOwed: number;
  fixedCosts: number;
  variableCosts: number;
  fuelCosts: number;
  distance: number;
}

export interface CostSummary {
  totalFixed: number;
  totalVariable: number;
  totalFuel: number;
  totalCosts: number;
  distributions: CostDistribution[];
}

