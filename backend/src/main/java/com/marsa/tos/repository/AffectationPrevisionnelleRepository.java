package com.marsa.tos.repository;

import com.marsa.tos.domain.exploitation.AffectationPrevisionnelle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AffectationPrevisionnelleRepository extends JpaRepository<AffectationPrevisionnelle, Long> {
    List<AffectationPrevisionnelle> findByCommandeNumero(String commandeNumero);
}
