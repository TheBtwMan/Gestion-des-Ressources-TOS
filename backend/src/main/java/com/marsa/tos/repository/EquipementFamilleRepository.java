package com.marsa.tos.repository;

import com.marsa.tos.domain.referentiel.EquipementFamille;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipementFamilleRepository extends JpaRepository<EquipementFamille, Long> {
    Optional<EquipementFamille> findByNom(String nom);
}
