// Fichero: model/infrastructure/persistence/ConfirmationRepository.java
package com.uade.tpo.Scrims.model.infrastructure.persistence;

import com.uade.tpo.Scrims.model.domain.Confirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfirmationRepository extends JpaRepository<Confirmation, Long> {
    // Contamos cuántas confirmaciones tiene un scrim específico
    long countByScrimId(Long scrimId);

    // Verificamos si un usuario ya ha confirmado para un scrim
    boolean existsByScrimIdAndUserId(Long scrimId, Long userId);
}