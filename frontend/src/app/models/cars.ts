export interface CarDto {
  carId: number;
  name: string;
  plateNumber: string;
  ownerId: number | null;
  data: string | null;
}
