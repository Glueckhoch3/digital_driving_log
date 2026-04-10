package de.digidrivelog.mappers;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
                c.getCostId(),
                c.getCarId() != null ? c.getCarId().getCarId() : null,
                c.getBuyerId() != null ? c.getBuyerId().getUserId() : null,
                c.getTransactionObject(),
                c.getPrice(),
                c.getDayOfTransaction(),
                c.getCostType() != null ? c.getCostType().name() : null,
                c.getAmount(),
                c.getNotes()
        );
    }

    public static Cost fromCreate(CreateCostRequest r, Car carId, User buyerId) {
        if (r == null) return null;
        Cost c = new Cost();
        c.setCarId(carId);
        c.setBuyerId(buyerId);
        c.setTransactionObject(r.getTransactionObject());
        c.setPrice(r.getPrice());
        c.setAmount(r.getAmount());
        c.setDayOfTransaction(r.getDayOfTransaction());
        c.setNotes(r.getNotes());
        if (r.getCostType() != null) {
            try {
                c.setCostType(CostType.valueOf(r.getCostType().toUpperCase()));
            } catch (IllegalArgumentException handled) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cost type: " + r.getCostType());
            }
        }
        return c;
    }

    public static void applyUpdate(UpdateCostRequest r, Cost existing, Car carId, User buyerId) {
        if (r == null || existing == null) return;
        if (r.getCarId() != null && carId != null) existing.setCarId(carId);
        if (r.getBuyerId() != null && buyerId != null) existing.setBuyerId(buyerId);
        if (r.getTransactionObject() != null) existing.setTransactionObject(r.getTransactionObject());
        if (r.getPrice() != null) existing.setPrice(r.getPrice());
        if (r.getAmount() != null) existing.setAmount(r.getAmount());
        if (r.getDayOfTransaction() != null) existing.setDayOfTransaction(r.getDayOfTransaction());
        if (r.getCostType() != null) {
            try {
                existing.setCostType(CostType.valueOf(r.getCostType().toUpperCase()));
            } catch (IllegalArgumentException handled) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cost type: " + r.getCostType());
            }
        }
        if (r.getNotes() != null) existing.setNotes(r.getNotes());
    }
}
