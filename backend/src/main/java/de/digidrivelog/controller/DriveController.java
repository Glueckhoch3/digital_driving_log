package de.digidrivelog.controller;

import de.digidrivelog.services.DriveService;
import de.digidrivelog.dto.drive.DriveDto;
import de.digidrivelog.dto.drive.CreateDriveRequest;
import de.digidrivelog.dto.drive.UpdateDriveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/ddl/api")
@RequiredArgsConstructor
public class DriveController {

    private final DriveService driveService;

    @PostMapping("/drives")
    public ResponseEntity<DriveDto> createDrive(@Valid @RequestBody CreateDriveRequest request) {
        DriveDto createdDrive = driveService.createDrive(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(createdDrive);
    }

    @GetMapping("/drives/{driveId}")
    public ResponseEntity<DriveDto> getDriveById(@PathVariable Long driveId) {
        DriveDto drive = driveService.getDriveById(driveId);
        return ResponseEntity.ok(drive);
    }

    @PutMapping("/drives/{driveId}")
    public ResponseEntity<DriveDto> updateDriveWithId(@PathVariable Long driveId, @Valid @RequestBody UpdateDriveRequest request) {
        DriveDto updatedDrive = driveService.updateDrive(driveId, request);
        return ResponseEntity.ok(updatedDrive);
    }

    @DeleteMapping("/drives/{driveId}")
    public ResponseEntity<Void> deleteDriveWithId(@PathVariable Long driveId) {
        driveService.deleteDrive(driveId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vehicles/{carId}/drives")
    public ResponseEntity<Page<DriveDto>> getAllDrivesByVehicle(
            @PathVariable Long carId,
            @PageableDefault(size = 50, sort = "driveDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(driveService.getAllDrivesByVehicle(carId, pageable));
    }

    @GetMapping("/users/{userId}/drives")
    public ResponseEntity<Page<DriveDto>> getAllDrivesByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 50, sort = "driveDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(driveService.getAllDrivesByUser(userId, pageable));
    }

    @GetMapping("/vehicles/{carId}/users/{userId}/drives")
    public ResponseEntity<Page<DriveDto>> getAllDrivesByVehicleAndUser(
            @PathVariable Long carId,
            @PathVariable Long userId,
            @PageableDefault(size = 50, sort = "driveDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(driveService.getAllDrivesByVehicleAndUser(carId, userId, pageable));
    }
}
