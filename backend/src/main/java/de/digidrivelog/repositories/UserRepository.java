package de.digidrivelog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
