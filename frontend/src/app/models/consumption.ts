export interface ConsumptionIntervalDto {
  costId: number;
  date: string;
  odometer: number;
  distanceKm: number;
  liters: number;
  price: number;
  litersPer100Km: number;
}

export interface ConsumptionDto {
  carId: number;
  year: number | null;
  intervals: ConsumptionIntervalDto[];
  averageLitersPer100Km: number | null;
  totalDistanceKm: number;
  totalLiters: number;
}
