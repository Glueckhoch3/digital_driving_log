package de.digidrivelog.controller;

import de.digidrivelog.services.DriveService;
import de.digidrivelog.dto.drive.DriveDto;
import de.digidrivelog.dto.drive.CreateDriveRequest;
import de.digidrivelog.dto.drive.UpdateDriveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class DriveController {

    private final DriveService driveService;

    @PostMapping("/drives")
    public ResponseEntity<DriveDto> createDrive(@Valid @RequestBody CreateDriveRequest request, @PathVariable Long carId) {
        if (request.getCarId() == null) {
            request.setCarId(carId);
        }
        DriveDto createdDrive = driveService.createDrive(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(createdDrive);
    }

    @GetMapping("/drives/{id}")
    public ResponseEntity<DriveDto> getDriveById(@PathVariable Long id) {
        DriveDto drive = driveService.getDriveById(id);
        return ResponseEntity.ok(drive);
    }

    @PutMapping("/drives/{id}")
    public ResponseEntity<DriveDto> updateDriveWithId(@PathVariable Long id, @Valid @RequestBody UpdateDriveRequest request) {
        DriveDto updatedDrive = driveService.updateDrive(id, request);
        return ResponseEntity.ok(updatedDrive);
    }

    @DeleteMapping("/drives/{id}")
    public ResponseEntity<Void> deleteDriveWithId(@PathVariable Long id) {
        driveService.deleteDrive(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vehicles/{carId}/drives")
    public ResponseEntity<List<DriveDto>> getAllDrivesByVehicle(@PathVariable Long carId) {
        List<DriveDto> drives = driveService.getAllDrivesByVehicle(carId);
        return ResponseEntity.ok(drives);
    }

    @GetMapping("users/{userId}/drives")
    public ResponseEntity<List<DriveDto>> getAllDrivesByUser(@PathVariable Long userId) {
        List<DriveDto> drives = driveService.getAllDrivesByUser(userId);
        return ResponseEntity.ok(drives);
    }

    @GetMapping("/vehicles/{carId}/users/{userId}/drives")
    public ResponseEntity<List<DriveDto>> getAllDrivesByVehicleAndUser(@PathVariable Long carId, @PathVariable Long userId) {
        List<DriveDto> drives = driveService.getAllDrivesByVehicleAndUser(carId, userId);
        return ResponseEntity.ok(drives);
    }
}