package de.digidrivelog.services;

import de.digidrivelog.dto.drive.DriveDto;
import de.digidrivelog.dto.drive.CreateDriveRequest;
import de.digidrivelog.dto.drive.UpdateDriveRequest;
import de.digidrivelog.models.Car;
import de.digidrivelog.models.Drive;
import de.digidrivelog.models.User;
import de.digidrivelog.repositories.CarRepository;
import de.digidrivelog.repositories.DriveRepository;
import de.digidrivelog.repositories.UserRepository;
import de.digidrivelog.mappers.DriveMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.stream.Collectors;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DriveService {

    private final DriveRepository driveRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;

    public DriveDto createDrive(CreateDriveRequest request) {
        Car car = carRepository.findById(request.getCarId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        User driver = userRepository.findById(request.getDriverId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver user not found"));
        Drive d = DriveMapper.fromCreate(request, car, driver);
        Drive saved = driveRepository.save(d);
        return DriveMapper.toDto(saved);
    }

    public DriveDto getDriveById(Long id) {
        Drive d = driveRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Drive not found"));
        return DriveMapper.toDto(d);
    }

    public DriveDto updateDrive(Long id, UpdateDriveRequest request) {
        Drive existing = driveRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Drive not found"));
        Car car = carRepository.findById(request.getCarId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        User driver = userRepository.findById(request.getDriverId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver user not found"));
        DriveMapper.applyUpdate(request, existing, car, driver);
        Drive saved = driveRepository.save(existing);
        return DriveMapper.toDto(saved);
    }

    public void deleteDrive(Long id) {
        if (!driveRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Drive not found");
        }
        driveRepository.deleteById(id);
    }

    public List<DriveDto> getAllDrivesByVehicle(Long carId) {
        return driveRepository.findByCarCarId(carId).stream().map(DriveMapper::toDto).collect(Collectors.toList());
    }

    public List<DriveDto> getAllDrivesByUser(Long userId) {
        return driveRepository.findByDriverUserId(userId).stream().map(DriveMapper::toDto).collect(Collectors.toList());
    }

    public List<DriveDto> getAllDrivesByVehicleAndUser(Long carId, Long userId) {
        return driveRepository.findByCarCarIdAndDriverUserId(carId, userId).stream().map(DriveMapper::toDto).collect(Collectors.toList());
    }

}
