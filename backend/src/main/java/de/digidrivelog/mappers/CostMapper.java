package de.digidrivelog.mappers;

import de.digidrivelog.dto.cost.*;
import de.digidrivelog.models.Cost;
import de.digidrivelog.models.Car;
import de.digidrivelog.models.User;
import de.digidrivelog.models.CostType;

public final class CostMapper {

    private CostMapper() {}

    public static CostDto toDto(Cost c) {
        if (c == null) return null;
        return new CostDto(
                c.getId(),
                c.getCar() != null ? c.getCar().getCarId() : null,
                c.getBuyer() != null ? c.getBuyer().getUserId() : null,
                c.getTransactionObject(),
                c.getPrice(),
                c.getDayOfTransaction(),
                c.getCostType() != null ? c.getCostType().name() : null,
                c.getAmount(),
                c.getNotes()
        );
    }

    public static Cost fromCreate(CreateCostRequest r, Car car, User buyer) {
        if (r == null) return null;
        Cost c = new Cost();
        c.setCar(car);
        c.setBuyer(buyer);
        c.setTransactionObject(r.getTransactionObject());
        c.setPrice(r.getPrice());
        c.setAmount(r.getAmount());
        c.setDayOfTransaction(r.getDayOfTransaction());
        c.setNotes(r.getNotes());
        if (r.getCostType() != null) {
            try {
                c.setCostType(CostType.valueOf(r.getCostType().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
                // leave null if unknown
            }
        }
        return c;
    }

    public static void applyUpdate(UpdateCostRequest r, Cost existing, Car car, User buyer) {
        if (r == null || existing == null) return;
        if (r.getCarId() != null && car != null) existing.setCar(car);
        if (r.getBuyerId() != null && buyer != null) existing.setBuyer(buyer);
        if (r.getTransactionObject() != null) existing.setTransactionObject(r.getTransactionObject());
        if (r.getPrice() != null) existing.setPrice(r.getPrice());
        if (r.getAmount() != null) existing.setAmount(r.getAmount());
        if (r.getDayOfTransaction() != null) existing.setDayOfTransaction(r.getDayOfTransaction());
        if (r.getCostType() != null) {
            try {
                existing.setCostType(CostType.valueOf(r.getCostType().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (r.getNotes() != null) existing.setNotes(r.getNotes());
    }
}
