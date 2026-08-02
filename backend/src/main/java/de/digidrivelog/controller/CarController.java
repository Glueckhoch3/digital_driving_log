package de.digidrivelog.controller;

import de.digidrivelog.dto.car.CarDto;
import de.digidrivelog.dto.car.CreateCarRequest;
import de.digidrivelog.dto.car.UpdateCarRequest;
import de.digidrivelog.services.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/ddl/api/vehicles")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping
    public ResponseEntity<List<CarDto>> getAllCars() {
        List<CarDto> cars = carService.getAllCars();
        return ResponseEntity.ok(cars);
    }

    @PostMapping
    public ResponseEntity<CarDto> createCar(@Valid @RequestBody CreateCarRequest request) {
        CarDto createdCar = carService.createCar(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(createdCar);
    }

    @GetMapping("/{carId}")
    public ResponseEntity<CarDto> getCarById(@PathVariable Long carId) {
        CarDto car = carService.getCarById(carId);
        return ResponseEntity.ok(car);
    }

    @PutMapping("/{carId}")
    public ResponseEntity<CarDto> updateCar(@PathVariable Long carId, @Valid @RequestBody UpdateCarRequest request) {
        CarDto updatedCar = carService.updateCar(carId, request);
        return ResponseEntity.ok(updatedCar);
    }

    @DeleteMapping("/{carId}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long carId) {
        carService.deleteCar(carId);
        return ResponseEntity.noContent().build();
    }
}
