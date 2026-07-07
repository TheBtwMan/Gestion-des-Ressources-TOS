package com.marsa.tos.repository;

import com.marsa.tos.domain.referentiel.Fonction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FonctionRepository extends JpaRepository<Fonction, Long> {
    Optional<Fonction> findByCode(String code);
}
