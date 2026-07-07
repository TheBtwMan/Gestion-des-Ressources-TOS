package com.marsa.tos.repository;

import com.marsa.tos.domain.referentiel.Equipement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipementRepository extends JpaRepository<Equipement, String> {
    List<Equipement> findByFamilleId(Long familleId);
}
