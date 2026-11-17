package de.digidrivelog.services;

import de.digidrivelog.dto.cost.CostDto;
import de.digidrivelog.dto.cost.CreateCostRequest;
import de.digidrivelog.dto.cost.UpdateCostRequest;
import de.digidrivelog.models.Car;
import de.digidrivelog.models.Cost;
import de.digidrivelog.models.User;
import de.digidrivelog.repositories.CarRepository;
import de.digidrivelog.repositories.CostCategoryRepository;
import de.digidrivelog.repositories.CostRepository;
import de.digidrivelog.models.CostCategory;
import de.digidrivelog.repositories.UserRepository;
import de.digidrivelog.mappers.CostMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CostService {

    private final CostRepository costRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final CostCategoryRepository costCategoryRepository;

    @Transactional(readOnly = true)
    public Page<CostDto> getAllCosts(Pageable pageable) {
        return costRepository.findAll(pageable).map(CostMapper::toDto);
    }

    @Transactional
    public CostDto createCost(CreateCostRequest request) {
        Car car = carRepository.findById(request.getCarId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        User buyer = userRepository.findById(request.getBuyerId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer user not found"));
        CostCategory category = requireCategory(request.getCostCategoryId(), request.getOdometer());
        Cost c = CostMapper.fromCreate(request, car, buyer, category);
        Cost saved = costRepository.save(c);
        return CostMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public CostDto getCostById(Long costId) {
        Cost c = costRepository.findById(costId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cost not found"));
        return CostMapper.toDto(c);
    }

    @Transactional
    public CostDto updateCost(Long costId, UpdateCostRequest request) {
        Cost existing = costRepository.findById(costId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cost not found"));
        Car car = carRepository.findById(request.getCarId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        User buyer = userRepository.findById(request.getBuyerId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer user not found"));
        CostCategory category = requireCategory(request.getCostCategoryId(), request.getOdometer());
        CostMapper.applyUpdate(request, existing, car, buyer, category);
        Cost saved = costRepository.save(existing);
        return CostMapper.toDto(saved);
    }

    @Transactional
    public void deleteCost(Long costId) {
        if (!costRepository.existsById(costId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cost not found");
        }
        costRepository.deleteById(costId);
    }

    @Transactional(readOnly = true)
    public Page<CostDto> getAllCostsByVehicle(Long carId, Pageable pageable) {
        return costRepository.findByCarCarId(carId, pageable).map(CostMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CostDto> getAllCostsByUser(Long userId, Pageable pageable) {
        return costRepository.findByBuyerUserId(userId, pageable).map(CostMapper::toDto);
    }

    /** Resolve the category and enforce its odometer requirement. */
    private CostCategory requireCategory(Long categoryId, Integer odometer) {
        CostCategory category = costCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cost category not found"));
        if (category.isRequiresOdometer() && odometer == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Odometer is required for cost category '" + category.getName() + "'");
        }
        return category;
    }
}
