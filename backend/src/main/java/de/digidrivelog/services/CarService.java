package de.digidrivelog.services;

import de.digidrivelog.dto.car.CarDto;
import de.digidrivelog.dto.car.CreateCarRequest;
import de.digidrivelog.dto.car.UpdateCarRequest;
import de.digidrivelog.models.Car;
import de.digidrivelog.models.User;
import de.digidrivelog.mappers.CarMapper;
import de.digidrivelog.repositories.CarRepository;
import de.digidrivelog.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CarDto> getAllCars() {
        return carRepository.findAll().stream().map(CarMapper::toDto).toList();
    }

    @Transactional
    public CarDto createCar(CreateCarRequest request) {
        User ownerId = userRepository.findById(request.getOwnerId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner user not found"));
        Car car = new Car();
        car.setName(request.getName());
        car.setPlateNumber(request.getPlateNumber());
        car.setOwner(ownerId);
        car.setData(request.getData());
        Car saved = carRepository.save(car);
        return CarMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public CarDto getCarById(Long carId) {
        Car car = carRepository.findById(carId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        return CarMapper.toDto(car);
    }

    @Transactional
    public CarDto updateCar(Long carId, UpdateCarRequest request) {
        Car car = carRepository.findById(carId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        User ownerId = userRepository.findById(request.getOwnerId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner user not found"));
        car.setName(request.getName());
        car.setPlateNumber(request.getPlateNumber());
        car.setOwner(ownerId);
        car.setData(request.getData());
        Car saved = carRepository.save(car);
        return CarMapper.toDto(saved);
    }

    @Transactional
    public void deleteCar(Long carId) {
        if (!carRepository.existsById(carId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found");
        }
        try {
            carRepository.deleteById(carId);
            // Flush inside the transaction so a FK violation surfaces here (and is
            // translated to 409) rather than escaping at commit time.
            carRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Car cannot be deleted because related drives or costs exist");
        }
    }



}
