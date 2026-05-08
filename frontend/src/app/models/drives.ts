export interface DriveDto {
  driveId: number;
  carId: number;
  driveDate: string;
  currentMileage: number;
  drivenDistance: number | null;
  driverId: number;
  notes: string | null;
}

export interface CreateDriveRequest {
  carId: number;
  currentMileage: number;
  driverId: number;
  driveDate: string;
  notes?: string;
}
