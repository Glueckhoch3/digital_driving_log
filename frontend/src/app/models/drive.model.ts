export interface Drive {
  id?: string;
  date: Date;
  driver: string;
  distance: number;
  notes?: string;
  fuelRefills?: FuelRefill[];
  createdAt?: Date;
  updatedAt?: Date;
}

export interface FuelRefill {
  id?: string;
  driveId?: string;
  date: Date;
  liters: number;
  cost: number;
  fuelType: string;
  costPerLiter?: number;
  createdAt?: Date;
  updatedAt?: Date;
}

export interface CreateDriveRequest {
  date: Date;
  driver: string;
  distance: number;
  notes?: string;
}

export interface CreateFuelRefillRequest {
  driveId: string;
  date: Date;
  liters: number;
  cost: number;
  fuelType: string;
}
