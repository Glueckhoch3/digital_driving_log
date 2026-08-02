package de.digidrivelog.mappers;

import de.digidrivelog.dto.car.*;
import de.digidrivelog.models.Car;
import de.digidrivelog.models.User;

public final class CarMapper {

    private CarMapper() {}

    public static CarDto toDto(Car c) {
        if (c == null) return null;
        return new CarDto(
                c.getCarId(),
                c.getName(),
                c.getPlateNumber(),
                c.getOwner() != null ? c.getOwner().getUserId() : null,
                c.getData()
        );
    }

    public static Car fromCreate(CreateCarRequest r, User ownerId) {
        if (r == null) return null;
        Car c = new Car();
        c.setName(r.getName());
        c.setPlateNumber(r.getPlateNumber());
        c.setOwner(ownerId);
        c.setData(r.getData());
        return c;
    }

    public static void applyUpdate(UpdateCarRequest r, Car existing, User ownerId) {
        if (r == null || existing == null) return;
        if (r.getName() != null) existing.setName(r.getName());
        if (r.getPlateNumber() != null) existing.setPlateNumber(r.getPlateNumber());
        if (r.getData() != null) existing.setData(r.getData());
        if (ownerId != null) existing.setOwner(ownerId);
    }
}
