package com.marsa.tos.repository;

import com.marsa.tos.domain.exploitation.AffectationReelle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AffectationReelleRepository extends JpaRepository<AffectationReelle, Long> {
    List<AffectationReelle> findByCommandeNumero(String commandeNumero);
}
