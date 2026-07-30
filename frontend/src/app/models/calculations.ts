/** DTOs for the yearly/monthly calculation feature (issue #20). */

export interface MonthlyCalculationRequest {
  carId: number;
  year: number;
  month: number;
}

export interface YearlyCalculationRequest {
  carId: number;
  year: number;
}

export interface MonthlyDistance {
  month: number;
  userId: number;
  userName: string;
  distance: number;
}

/** One driver's owed-only line in a car's yearly settlement (no paid/balance). */
export interface YearlySettlementRow {
  userId: number;
  userName: string;
  distance: number;
  factorVariableCost: number;
  factorFixCost: number;
  variableOwed: number;
  fixedOwed: number;
  totalOwed: number;
}

export interface FactorRow {
  userId: number;
  userName: string;
  factorVariableCost: number;
  factorFixCost: number;
  manuallyAdded: boolean;
}

/** One driver's combined settlement across every calculated car in a year. */
export interface CombinedSettlementRow {
  userId: number;
  userName: string;
  paid: number;
  differenceVariableCost: number;
  differenceFixCost: number;
  netBalance: number;
}

/** DTOs for the participant management + availability feature (issue #32). */

export interface ParticipantRow {
  userId: number;
  userName: string;
  participating: boolean;
  manuallyAdded: boolean;
  hasDrives: boolean;
  distance: number;
  fixShare: number | null;
  varShare: number | null;
}

export interface ParticipantSet {
  carId: number;
  year: number;
  stored: boolean;
  rows: ParticipantRow[];
}

export interface ParticipantUpdateRequest {
  carId: number;
  year: number;
  userIds: number[];
}

export interface YearAvailability {
  year: number;
  yearCalculated: boolean;
  participantsStored: boolean;
  aggregatedMonths: number[];
  monthsWithDrives: number[];
}

export interface CarAvailability {
  carId: number;
  years: YearAvailability[];
}
