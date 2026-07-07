package com.marsa.tos.repository;

import com.marsa.tos.domain.referentiel.Trafic;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TraficRepository extends JpaRepository<Trafic, Long> {
    Optional<Trafic> findByCode(String code);
}
