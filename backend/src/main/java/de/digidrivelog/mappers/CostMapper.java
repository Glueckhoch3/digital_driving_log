package de.digidrivelog.mappers;

import de.digidrivelog.dto.cost.*;
import de.digidrivelog.models.Cost;
import de.digidrivelog.models.CostCategory;
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
                c.getCostCategory() != null ? c.getCostCategory().getCostCategoryId() : null,
                c.getCostCategory() != null ? c.getCostCategory().getName() : null,
                c.getCostCategory() != null ? c.getCostCategory().getUnit() : null,
                c.getPrice(),
                c.getDayOfTransaction(),
                c.getCostCategory() != null ? c.getCostCategory().getCostType() : null,
                c.getOdometer(),
                c.getQuantity(),
                c.getNotes()
        );
    }

    public static Cost fromCreate(CreateCostRequest r, Car car, User buyer, CostCategory category) {
        if (r == null) return null;
        Cost c = new Cost();
        c.setCar(car);
        c.setBuyer(buyer);
        c.setCostCategory(category);
        c.setOdometer(r.getOdometer());
        c.setPrice(r.getPrice());
        c.setQuantity(r.getQuantity());
        c.setDayOfTransaction(r.getDayOfTransaction());
        c.setNotes(r.getNotes());
        return c;
    }

    /** Full replace (PUT semantics): all editable fields are applied. */
    public static void applyUpdate(UpdateCostRequest r, Cost existing, Car car, User buyer, CostCategory category) {
        if (r == null || existing == null) return;
        existing.setCar(car);
        existing.setBuyer(buyer);
        existing.setCostCategory(category);
        existing.setOdometer(r.getOdometer());
        existing.setPrice(r.getPrice());
        existing.setQuantity(r.getQuantity());
        existing.setDayOfTransaction(r.getDayOfTransaction());
        existing.setNotes(r.getNotes());
    }
}
