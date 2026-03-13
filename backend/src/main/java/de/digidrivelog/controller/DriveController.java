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
@RequestMapping("/api/drives")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class DriveController {

    private final DriveService driveService;

    @GetMapping
    public ResponseEntity<List<DriveDto>> getAllDrives() {
        List<DriveDto> drives = driveService.getAllDrives();
        return ResponseEntity.ok(drives);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriveDto> getDriveById(@PathVariable Long id) {
        DriveDto drive = driveService.getDriveById(id);
        return ResponseEntity.ok(drive);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DriveDto> updateDriveWithId(@PathVariable Long id, @Valid @RequestBody UpdateDriveRequest request) {
        DriveDto updatedDrive = driveService.updateDriveWithId(id, request);
        return ResponseEntity.ok(updatedDrive);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriveWithId(@PathVariable Long id) {
        driveService.deleteDriveWithId(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vehicles/{carId}")
    public ResponseEntity<List<DriveDto>> getAllDrivesByVehicle(@PathVariable Long carId) {
        List<DriveDto> drives = driveService.getAllDrivesByVehicle(carId);
        return ResponseEntity.ok(drives);
    }

    @PostMapping("/vehicles/{carId}")
    public ResponseEntity<DriveDto> createDrive(@Valid @RequestBody CreateDriveRequest request, @PathVariable Long carId) {
        DriveDto createdDrive = driveService.createDrive(request, carId);
        return ResponseEntity.ok(createdDrive);
    }
}