package de.digidrivelog.mappers;

import de.digidrivelog.dto.drive.*;
import de.digidrivelog.models.Drive;
import de.digidrivelog.models.Car;
import de.digidrivelog.models.User;

public final class DriveMapper {

    private DriveMapper() {}

    public static DriveDto toDto(Drive d) {
        if (d == null) return null;
        return new DriveDto(
                d.getDriveId(),
                d.getCar() != null ? d.getCar().getCarId() : null,
                d.getDriveDate(),
                d.getOdometer(),
                d.getDriver() != null ? d.getDriver().getUserId() : null,
                d.getNotes()
        );
    }

    public static Drive fromCreate(CreateDriveRequest r, Car car, User driver) {
        if (r == null) return null;
        Drive d = new Drive();
        d.setCar(car);
        d.setOdometer(r.getOdometer());
        d.setDriver(driver);
        d.setDriveDate(r.getDriveDate());
        d.setNotes(r.getNotes());
        return d;
    }

    /** Full replace (PUT semantics): all editable fields are applied. */
    public static void applyUpdate(UpdateDriveRequest r, Drive existing, Car car, User driver) {
        if (r == null || existing == null) return;
        existing.setCar(car);
        existing.setOdometer(r.getOdometer());
        existing.setDriver(driver);
        existing.setDriveDate(r.getDriveDate());
        existing.setNotes(r.getNotes());
    }
}
