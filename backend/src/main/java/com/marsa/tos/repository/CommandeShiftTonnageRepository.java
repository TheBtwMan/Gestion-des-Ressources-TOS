package com.marsa.tos.repository;

import com.marsa.tos.domain.exploitation.CommandeShiftTonnage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandeShiftTonnageRepository extends JpaRepository<CommandeShiftTonnage, Long> {
    List<CommandeShiftTonnage> findByCommandeNumero(String commandeNumero);
}
