package com.marsa.tos.repository;

import com.marsa.tos.domain.parametrage.MainTheorique;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MainTheoriqueRepository extends JpaRepository<MainTheorique, Long> {
    List<MainTheorique> findByTraficId(Long traficId);
    List<MainTheorique> findByTerminalId(Long terminalId);
}
