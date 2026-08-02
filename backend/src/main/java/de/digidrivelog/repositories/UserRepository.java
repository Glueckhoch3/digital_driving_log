package de.digidrivelog.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Resolve a user by the (first name, last name) pair used in CSV imports, case-insensitively. */
    Optional<User> findByFirstnameIgnoreCaseAndLastnameIgnoreCase(String firstname, String lastname);
}
