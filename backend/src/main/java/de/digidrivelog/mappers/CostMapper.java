package de.digidrivelog.mappers;

import de.digidrivelog.dto.cost.*;
import de.digidrivelog.models.Cost;
import de.digidrivelog.models.Car;
import de.digidrivelog.models.User;

public final class CostMapper {

    private CostMapper() {}

    public static CostDto toDto(Cost c) {
        if (c == null) return null;
        return new CostDto(
                c.getCostId(),
                c.getCar() != null ? c.getCar().getCarId() : null,
                c.getBuyer() != null ? c.getBuyer().getUserId() : null,
                c.getDescription(),
                c.getPrice(),
                c.getDayOfTransaction(),
                c.getCostType(),
                c.getQuantity(),
                c.getNotes()
        );
    }

    public static Cost fromCreate(CreateCostRequest r, Car car, User buyer) {
        if (r == null) return null;
        Cost c = new Cost();
        c.setCar(car);
        c.setBuyer(buyer);
        c.setDescription(r.getDescription());
        c.setPrice(r.getPrice());
        c.setQuantity(r.getQuantity());
        c.setDayOfTransaction(r.getDayOfTransaction());
        c.setNotes(r.getNotes());
        c.setCostType(r.getCostType());
        return c;
    }

    /** Full replace (PUT semantics): all editable fields are applied. */
    public static void applyUpdate(UpdateCostRequest r, Cost existing, Car car, User buyer) {
        if (r == null || existing == null) return;
        existing.setCar(car);
        existing.setBuyer(buyer);
        existing.setDescription(r.getDescription());
        existing.setPrice(r.getPrice());
        existing.setQuantity(r.getQuantity());
        existing.setDayOfTransaction(r.getDayOfTransaction());
        existing.setCostType(r.getCostType());
        existing.setNotes(r.getNotes());
    }
}
