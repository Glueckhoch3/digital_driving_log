import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { CarService } from '../services/car.service';
import { CarDto } from '../models/cars';

export const carResolver: ResolveFn<CarDto> = (route) => {
  const carId = Number(route.paramMap.get('carId'));
  return inject(CarService).getCarById(carId);
};
