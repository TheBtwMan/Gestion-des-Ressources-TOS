package com.marsa.tos.repository;

import com.marsa.tos.domain.parametrage.Equipe;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipeRepository extends JpaRepository<Equipe, String> {
    List<Equipe> findByTerminalId(Long terminalId);
}
