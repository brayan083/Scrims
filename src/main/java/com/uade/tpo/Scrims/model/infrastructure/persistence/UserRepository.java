// Fichero: model/infrastructure/persistence/UserRepository.java
package com.uade.tpo.Scrims.model.infrastructure.persistence;

import com.uade.tpo.Scrims.model.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA creará automáticamente los métodos para guardar, buscar, etc.

    Optional<User> findByUsername(String username);

}