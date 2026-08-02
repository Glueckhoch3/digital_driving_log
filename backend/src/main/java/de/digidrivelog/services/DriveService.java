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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DriveService {

    private final DriveRepository driveRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;

    @Transactional
    public DriveDto createDrive(CreateDriveRequest request) {
        Car car = carRepository.findById(request.getCarId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        User driver = userRepository.findById(request.getDriverId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver user not found"));
        Drive d = DriveMapper.fromCreate(request, car, driver);
        Drive saved = driveRepository.save(d);
        return DriveMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public DriveDto getDriveById(Long id) {
        Drive d = driveRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Drive not found"));
        return DriveMapper.toDto(d);
    }

    @Transactional
    public DriveDto updateDrive(Long id, UpdateDriveRequest request) {
        Drive existing = driveRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Drive not found"));
        Car car = carRepository.findById(request.getCarId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        User driver = userRepository.findById(request.getDriverId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver user not found"));
        DriveMapper.applyUpdate(request, existing, car, driver);
        Drive saved = driveRepository.save(existing);
        return DriveMapper.toDto(saved);
    }

    @Transactional
    public void deleteDrive(Long id) {
        if (!driveRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Drive not found");
        }
        driveRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<DriveDto> getAllDrivesByVehicle(Long carId, Pageable pageable) {
        return driveRepository.findByCarCarId(carId, pageable).map(DriveMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<DriveDto> getAllDrivesByUser(Long userId, Pageable pageable) {
        return driveRepository.findByDriverUserId(userId, pageable).map(DriveMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<DriveDto> getAllDrivesByVehicleAndUser(Long carId, Long userId, Pageable pageable) {
        return driveRepository.findByCarCarIdAndDriverUserId(carId, userId, pageable).map(DriveMapper::toDto);
    }

}
