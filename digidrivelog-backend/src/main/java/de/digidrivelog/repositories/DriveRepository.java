package de.digidrivelog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.Drive;

@Repository
public interface DriveRepository extends JpaRepository<Drive, Long> {
}