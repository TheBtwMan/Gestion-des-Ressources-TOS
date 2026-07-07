package com.marsa.tos.repository;

import com.marsa.tos.domain.parametrage.PeriodeShift;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeriodeShiftRepository extends JpaRepository<PeriodeShift, Long> {
    Optional<PeriodeShift> findByTerminalId(Long terminalId);
}
