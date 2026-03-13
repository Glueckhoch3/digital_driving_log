package de.digidrivelog.services;

import de.digidrivelog.dto.car.CarDto;
import de.digidrivelog.dto.car.CreateCarRequest;
import de.digidrivelog.dto.car.UpdateCarRequest;
import de.digidrivelog.models.Car;
import de.digidrivelog.models.User;
import de.digidrivelog.repositories.CarRepository;
import de.digidrivelog.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;

    public List<CarDto> getAllCars() {
        return carRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    public CarDto getCarById(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found"));
        return convertToDto(car);
    }

    public CarDto createCar(CreateCarRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Car car = new Car();
        car.setName(request.getName());
        car.setPlateNumber(request.getPlateNumber());
        car.setOwner(owner);
        car.setData(request.getData());

        Car savedCar = carRepository.save(car);
        return convertToDto(savedCar);
    }

    public CarDto updateCar(Long id, UpdateCarRequest request) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        car.setName(request.getName());
        car.setPlateNumber(request.getPlateNumber());
        car.setData(request.getData());

        Car updatedCar = carRepository.save(car);
        return convertToDto(updatedCar);
    }

    public void deleteCar(Long id) {
        if (!carRepository.existsById(id)) {
            throw new RuntimeException("Car not found");
        }
        carRepository.deleteById(id);
    }

    private CarDto convertToDto(Car car) {
        return new CarDto(
                car.getCarId(),
                car.getName(),
                car.getPlateNumber(),
                car.getOwner() != null ? car.getOwner().getUserId() : null,
                car.getOwner() != null ? car.getOwner().getFirstname() + " " + car.getOwner().getLastname() : null,
                car.getData()
        );
    }
}