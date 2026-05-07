package de.digidrivelog.mappers;

import de.digidrivelog.dto.drive.*;
import de.digidrivelog.models.Drive;
import de.digidrivelog.models.Car;
import de.digidrivelog.models.User;

public final class DriveMapper {

    private DriveMapper() {}

    public static DriveDto toDto(Drive d) {
        return toDto(d, null);
    }

    public static DriveDto toDto(Drive d, Integer drivenDistance) {
        if (d == null) return null;
        return new DriveDto(
                d.getDriveId(),
                d.getCar() != null ? d.getCar().getCarId() : null,
                d.getDriveDate(),
                d.getCurrentMileage(),
                drivenDistance,
                d.getDriver() != null ? d.getDriver().getUserId() : null,
                d.getNotes()
        );
    }

    public static Drive fromCreate(CreateDriveRequest r, Car car, User driver) {
        if (r == null) return null;
        Drive d = new Drive();
        d.setCar(car);
        d.setCurrentMileage(r.getCurrentMileage());
        d.setDriver(driver);
        d.setDriveDate(r.getDriveDate());
        d.setNotes(r.getNotes());
        return d;
    }

    public static void applyUpdate(UpdateDriveRequest r, Drive existing, Car car, User driver) {
        if (r == null || existing == null) return;
        if (r.getCarId() != null && car != null) existing.setCar(car);
        if (r.getCurrentMileage() != null) existing.setCurrentMileage(r.getCurrentMileage());
        if (r.getDriverId() != null && driver != null) existing.setDriver(driver);
        if (r.getDriveDate() != null) existing.setDriveDate(r.getDriveDate());
        if (r.getNotes() != null) existing.setNotes(r.getNotes());
    }
}
