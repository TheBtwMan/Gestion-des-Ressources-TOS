package com.marsa.tos.repository;

import com.marsa.tos.common.Enums.StatutEscale;
import com.marsa.tos.domain.exploitation.Escale;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EscaleRepository extends JpaRepository<Escale, String> {
    List<Escale> findByStatut(StatutEscale statut);
}
