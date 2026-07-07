package com.marsa.tos.repository;

import com.marsa.tos.domain.referentiel.Port;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortRepository extends JpaRepository<Port, Long> {
    Optional<Port> findByNom(String nom);
}
