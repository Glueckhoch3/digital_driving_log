export interface Settlement {
  id?: string;
  period: string;
  startDate: Date;
  endDate: Date;
  finalBalances: { [key: string]: number };
  totalDrives: number;
  totalDistance: number;
  totalCosts: number;
  status: 'pending' | 'completed';
  createdAt?: Date;
  completedAt?: Date;
}

export interface SettlementReport {
  settlement: Settlement;
  costBreakdown: CostBreakdown;
  shareholderReports: ShareholderSettlementReport[];
}

export interface CostBreakdown {
  fixedCosts: number;
  variableCosts: number;
  fuelCosts: number;
  totalCosts: number;
}

export interface ShareholderSettlementReport {
  shareholderId: string;
  shareholderName: string;
  distance: number;
  fixedCostsShare: number;
  variableCostsShare: number;
  fuelCostsShare: number;
  totalOwed: number;
  paid?: boolean;
  paymentDate?: Date;
}
