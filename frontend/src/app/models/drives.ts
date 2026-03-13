import { Cost } from './costs';

export interface DriveFormValues {
  drive?: Drive[];
  fuelRefills?: FuelRefill[];
}

export interface Drive {
  id?: string;
  date: Date;
  driver: string;
  distance: number;
  notes?: string;
}

export interface FuelRefill extends Cost {
  type: 'variable';
  description: 'Fuel Refill';
  category?: 'Fuel';
}

