package com.marsa.tos.repository;

import com.marsa.tos.domain.parametrage.ModeTravail;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModeTravailRepository extends JpaRepository<ModeTravail, Long> {
    Optional<ModeTravail> findByTerminalId(Long terminalId);
}
