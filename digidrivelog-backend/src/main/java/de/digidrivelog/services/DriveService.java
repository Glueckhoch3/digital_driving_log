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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DriveService {

    private final DriveRepository driveRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;

    public List<DriveDto> getAllDrives() {
        return driveRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    public DriveDto getDriveById(Long id) {
        Drive drive = driveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drive not found"));
        return convertToDto(drive);
    }

    public DriveDto createDrive(CreateDriveRequest request) {
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new RuntimeException("Car not found"));
        User driver = userRepository.findById(request.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        Drive drive = new Drive();
        drive.setDriveId(System.currentTimeMillis()); // Simple ID generation
        drive.setCar(car);
        drive.setDistance(request.getDistance());
        drive.setDriver(driver);
        drive.setCreatedAt(LocalDateTime.now());

        Drive savedDrive = driveRepository.save(drive);
        return convertToDto(savedDrive);
    }

    public DriveDto updateDrive(Long id, UpdateDriveRequest request) {
        Drive drive = driveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drive not found"));

        drive.setDistance(request.getDistance());

        Drive updatedDrive = driveRepository.save(drive);
        return convertToDto(updatedDrive);
    }

    public void deleteDrive(Long id) {
        if (!driveRepository.existsById(id)) {
            throw new RuntimeException("Drive not found");
        }
        driveRepository.deleteById(id);
    }

    private DriveDto convertToDto(Drive drive) {
        return new DriveDto(
                drive.getDriveId(),
                drive.getCar() != null ? drive.getCar().getCarId() : null,
                drive.getCar() != null ? drive.getCar().getName() : null,
                drive.getDistance(),
                drive.getDriver() != null ? drive.getDriver().getUserId() : null,
                drive.getDriver() != null ? drive.getDriver().getFirstname() + " " + drive.getDriver().getLastname() : null,
                drive.getCreatedAt()
        );
    }
}