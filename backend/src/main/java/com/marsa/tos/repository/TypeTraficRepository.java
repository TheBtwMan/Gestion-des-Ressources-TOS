package com.marsa.tos.repository;

import com.marsa.tos.domain.referentiel.TypeTrafic;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeTraficRepository extends JpaRepository<TypeTrafic, Long> {
    Optional<TypeTrafic> findByNom(String nom);
}
