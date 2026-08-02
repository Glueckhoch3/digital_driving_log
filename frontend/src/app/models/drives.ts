export interface DriveDto {
  driveId: number;
  carId: number;
  driveDate: string;
  /** Cumulative odometer reading at the time of the drive (not per-drive distance). */
  odometer: number;
  driverId: number;
  notes: string | null;
}

export interface CreateDriveRequest {
  carId: number;
  odometer: number;
  driverId: number;
  driveDate: string;
  notes?: string;
}
