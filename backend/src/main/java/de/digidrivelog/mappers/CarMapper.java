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
                c.getBrand(),
                c.getData()
        );
    }

    public static Car fromCreate(CreateCarRequest r, User owner) {
        if (r == null) return null;
        Car c = new Car();
        c.setName(r.getName());
        c.setPlateNumber(r.getPlateNumber());
        c.setBrand(r.getBrand());
        c.setOwner(owner);
        c.setData(r.getData());
        return c;
    }

    public static void applyUpdate(UpdateCarRequest r, Car existing, User owner) {
        if (r == null || existing == null) return;
        if (r.getName() != null) existing.setName(r.getName());
        if (r.getPlateNumber() != null) existing.setPlateNumber(r.getPlateNumber());
        if (r.getBrand() != null) existing.setBrand(r.getBrand());
        if (r.getData() != null) existing.setData(r.getData());
        if (owner != null) existing.setOwner(owner);
    }
}
