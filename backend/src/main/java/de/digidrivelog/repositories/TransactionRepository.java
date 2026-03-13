package de.digidrivelog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}