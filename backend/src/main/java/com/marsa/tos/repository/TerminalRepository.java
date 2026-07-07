package com.marsa.tos.repository;

import com.marsa.tos.domain.referentiel.Terminal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerminalRepository extends JpaRepository<Terminal, Long> {
    Optional<Terminal> findByNom(String nom);
}
