package de.digidrivelog.mappers;

import de.digidrivelog.dto.costcategory.CostCategoryDto;
import de.digidrivelog.dto.costcategory.CreateCostCategoryRequest;
import de.digidrivelog.dto.costcategory.UpdateCostCategoryRequest;
import de.digidrivelog.models.CostCategory;

public final class CostCategoryMapper {

    private CostCategoryMapper() {}

    public static CostCategoryDto toDto(CostCategory c) {
        if (c == null) return null;
        return new CostCategoryDto(c.getCostCategoryId(), c.getName(), c.getCostType(),
                c.getUnit(), c.isRequiresOdometer(), c.isFuel());
    }

    public static CostCategory fromCreate(CreateCostCategoryRequest r) {
        if (r == null) return null;
        CostCategory c = new CostCategory();
        c.setName(r.getName().trim());
        c.setCostType(r.getCostType());
        c.setUnit(r.getUnit().trim());
        c.setRequiresOdometer(r.isRequiresOdometer());
        c.setFuel(r.isFuel());
        return c;
    }

    /** Full replace (PUT semantics). */
    public static void applyUpdate(UpdateCostCategoryRequest r, CostCategory existing) {
        if (r == null || existing == null) return;
        existing.setName(r.getName().trim());
        existing.setCostType(r.getCostType());
        existing.setUnit(r.getUnit().trim());
        existing.setRequiresOdometer(r.isRequiresOdometer());
        existing.setFuel(r.isFuel());
    }
}
