package com.marsa.tos.repository;

import com.marsa.tos.domain.referentiel.Accessoire;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessoireRepository extends JpaRepository<Accessoire, Long> {
    Optional<Accessoire> findByNom(String nom);
}
