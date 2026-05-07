package de.digidrivelog.services;

import de.digidrivelog.dto.drive.DriveDto;
import de.digidrivelog.dto.drive.CreateDriveRequest;
import de.digidrivelog.dto.drive.UpdateDriveRequest;
import de.digidrivelog.models.Car;
import de.digidrivelog.models.Drive;
import de.digidrivelog.models.User;
import de.digidrivelog.repositories.CarRepository;
import de.digidrivelog.repositories.DriveDistanceProjection;
import de.digidrivelog.repositories.DriveRepository;
import de.digidrivelog.repositories.UserRepository;
import de.digidrivelog.mappers.DriveMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        return toDtoWithDrivenDistance(saved);
    }

    public DriveDto getDriveById(Long id) {
        Drive d = driveRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Drive not found"));
        return toDtoWithDrivenDistance(d);
    }

    public DriveDto updateDrive(Long id, UpdateDriveRequest request) {
        Drive existing = driveRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Drive not found"));
        Car car = carRepository.findById(request.getCarId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        User driver = userRepository.findById(request.getDriverId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver user not found"));
        DriveMapper.applyUpdate(request, existing, car, driver);
        Drive saved = driveRepository.save(existing);
        return toDtoWithDrivenDistance(saved);
    }

    public void deleteDrive(Long id) {
        if (!driveRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Drive not found");
        }
        driveRepository.deleteById(id);
    }

    public List<DriveDto> getAllDrivesByVehicle(Long carId) {
        List<Drive> drives = driveRepository.findByCarCarId(carId);
        Map<Long, Integer> drivenDistanceByDriveId = findDrivenDistanceByCar(carId);
        return drives.stream()
                .map(drive -> DriveMapper.toDto(drive, drivenDistanceByDriveId.get(drive.getDriveId())))
                .toList();
    }

    public List<DriveDto> getAllDrivesByUser(Long userId) {
        List<Drive> drives = driveRepository.findByDriverUserId(userId);
        Map<Long, Integer> drivenDistanceByDriveId = findDrivenDistanceByDriveList(drives);
        return drives.stream()
                .map(drive -> DriveMapper.toDto(drive, drivenDistanceByDriveId.get(drive.getDriveId())))
                .toList();
    }

    public List<DriveDto> getAllDrivesByVehicleAndUser(Long carId, Long userId) {
        List<Drive> drives = driveRepository.findByCarCarIdAndDriverUserId(carId, userId);
        Map<Long, Integer> drivenDistanceByDriveId = findDrivenDistanceByCar(carId);
        return drives.stream()
                .map(drive -> DriveMapper.toDto(drive, drivenDistanceByDriveId.get(drive.getDriveId())))
                .toList();
    }

    private DriveDto toDtoWithDrivenDistance(Drive drive) {
        Map<Long, Integer> drivenDistanceByDriveId = findDrivenDistanceByCar(drive.getCar().getCarId());
        return DriveMapper.toDto(drive, drivenDistanceByDriveId.get(drive.getDriveId()));
    }

    private Map<Long, Integer> findDrivenDistanceByDriveList(List<Drive> drives) {
        Map<Long, Integer> drivenDistanceByDriveId = new HashMap<>();
        drives.stream()
                .map(Drive::getCar)
                .filter(car -> car != null && car.getCarId() != null)
                .map(Car::getCarId)
                .distinct()
                .forEach(carId -> drivenDistanceByDriveId.putAll(findDrivenDistanceByCar(carId)));
        return drivenDistanceByDriveId;
    }

    private Map<Long, Integer> findDrivenDistanceByCar(Long carId) {
        List<DriveDistanceProjection> distances = driveRepository.findDrivenDistanceByCarId(carId);
        Map<Long, Integer> drivenDistanceByDriveId = new HashMap<>();
        for (DriveDistanceProjection distance : distances) {
            drivenDistanceByDriveId.put(distance.getDriveId(), distance.getDrivenDistance());
        }
        return drivenDistanceByDriveId;
    }

}
