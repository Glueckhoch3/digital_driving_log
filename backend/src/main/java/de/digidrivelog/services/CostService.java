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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CostService {

    private final CostRepository costRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;

    public List<CostDto> getAllTransactions() {
        return costRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    public CostDto getTransactionById(Long id) {
        Cost transaction = costRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return convertToDto(transaction);
    }

    public CostDto createTransaction(CreateCostRequest request) {
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new RuntimeException("Car not found"));
        User buyer = userRepository.findById(request.getBuyerId())
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

        Cost transaction = new Cost();
        transaction.setCar(car);
        transaction.setBuyer(buyer);
        transaction.setTransactionObject(request.getTransactionObject());
        transaction.setPrice(request.getPrice());
        transaction.setDayOfTransaction(request.getDayOfTransaction());
        transaction.setCostType(request.getCostType());

        Cost savedTransaction = costRepository.save(transaction);
        return convertToDto(savedTransaction);
    }

    public CostDto updateTransaction(Long id, UpdateCostRequest request) {
        Cost transaction = costRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transaction.setTransactionObject(request.getTransactionObject());
        transaction.setPrice(request.getPrice());
        transaction.setDayOfTransaction(request.getDayOfTransaction());
        transaction.setCostType(request.getCostType());

        Cost updatedTransaction = costRepository.save(transaction);
        return convertToDto(updatedTransaction);
    }

    public void deleteTransaction(Long id) {
        if (!costRepository.existsById(id)) {
            throw new RuntimeException("Transaction not found");
        }
        costRepository.deleteById(id);
    }

    private CostDto convertToDto(Cost transaction) {
        return new CostDto(
                transaction.getId(),
                transaction.getCar() != null ? transaction.getCar().getCarId() : null,
                transaction.getCar() != null ? transaction.getCar().getName() : null,
                transaction.getBuyer() != null ? transaction.getBuyer().getUserId() : null,
                transaction.getBuyer() != null ? transaction.getBuyer().getFirstname() + " " + transaction.getBuyer().getLastname() : null,
                transaction.getTransactionObject(),
                transaction.getPrice(),
                transaction.getDayOfTransaction(),
                transaction.getCostType()
        );
    }
}