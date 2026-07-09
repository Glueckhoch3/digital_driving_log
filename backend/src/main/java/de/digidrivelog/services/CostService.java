package de.digidrivelog.services;

import de.digidrivelog.dto.cost.CostDto;
import de.digidrivelog.dto.cost.CreateCostRequest;
import de.digidrivelog.dto.cost.UpdateCostRequest;
import de.digidrivelog.models.Car;
import de.digidrivelog.models.Cost;
import de.digidrivelog.models.User;
import de.digidrivelog.repositories.CarRepository;
import de.digidrivelog.repositories.CostRepository;
import de.digidrivelog.repositories.UserRepository;
import de.digidrivelog.mappers.CostMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CostService {

    private final CostRepository costRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;

    public List<CostDto> getAllCosts() {
        return costRepository.findAll().stream().map(CostMapper::toDto).toList();
    }

    public CostDto createCost(CreateCostRequest request) {
        Car car = carRepository.findById(request.getCarId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        User buyer = userRepository.findById(request.getBuyerId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer user not found"));
        Cost c = CostMapper.fromCreate(request, car, buyer);
        Cost saved = costRepository.save(c);
        return CostMapper.toDto(saved);
    }

    public CostDto getCostById(Long costId) {
        Cost c = costRepository.findById(costId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cost not found"));
        return CostMapper.toDto(c);
    }

    public CostDto updateCost(Long costId, UpdateCostRequest request) {
        Cost existing = costRepository.findById(costId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cost not found"));
        Car car = carRepository.findById(request.getCarId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        User buyer = userRepository.findById(request.getBuyerId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer user not found"));
        CostMapper.applyUpdate(request, existing, car, buyer);
        Cost saved = costRepository.save(existing);
        return CostMapper.toDto(saved);
    }

    public void deleteCost(Long costId) {
        if (!costRepository.existsById(costId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cost not found");
        }
        costRepository.deleteById(costId);
    }

    public List<CostDto> getAllCostsByVehicle(Long carId) {
        return costRepository.findByCarIdCarId(carId).stream().map(CostMapper::toDto).toList();
    }

    public List<CostDto> getAllCostsByUser(Long userId) {
        return costRepository.findByBuyerIdUserId(userId).stream().map(CostMapper::toDto).toList();
    }

}
