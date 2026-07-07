package com.marsa.tos.repository;

import com.marsa.tos.domain.exploitation.Commande;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandeRepository extends JpaRepository<Commande, String> {
    List<Commande> findByEscaleIsNull();
    List<Commande> findByEscaleId(String escaleId);
}
