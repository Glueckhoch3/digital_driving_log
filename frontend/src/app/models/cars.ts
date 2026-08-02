export interface CarDto {
  carId: number;
  name: string;
  plateNumber: string;
  ownerId: number | null;
  data: string | null;
}

export interface CreateCarRequest {
  name: string;
  plateNumber: string;
  ownerId: number;
  data?: string;
}

export interface UpdateCarRequest extends CreateCarRequest {}
