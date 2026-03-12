export interface Drive {
  id?: string;
  date: Date;
  driver: string;
  distance: number;
  notes?: string;
  fuelRefills?: FuelRefill[];
}

export interface DriveResponse {
  id?: string;
  date: Date;
  driver: string;
  distance: number;
  notes?: string;
  fuelRefills?: FuelRefill[];
}

export interface FuelRefill {
  id?: string;
  driveId?: string;
  date: Date;
  liters: number;
  cost: number;
  fuelType: string;
  costPerLiter?: number;
}

export interface DriveRequest {
  carId: string;
  date: Date;
  driver: string;
  distance: number;
  notes?: string;
}
