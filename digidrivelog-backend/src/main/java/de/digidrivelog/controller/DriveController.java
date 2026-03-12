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
@CrossOrigin(origins = "*")
public class DriveController {

    private final DriveService driveService;

    // Maybe just get the car options
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

    @PostMapping
    public ResponseEntity<DriveDto> createDrive(@Valid @RequestBody CreateDriveRequest request) {
        DriveDto createdDrive = driveService.createDrive(request);
        return ResponseEntity.ok(createdDrive);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DriveDto> updateDrive(@PathVariable Long id, @Valid @RequestBody UpdateDriveRequest request) {
        DriveDto updatedDrive = driveService.updateDrive(id, request);
        return ResponseEntity.ok(updatedDrive);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDrive(@PathVariable Long id) {
        driveService.deleteDrive(id);
        return ResponseEntity.noContent().build();
    }
}