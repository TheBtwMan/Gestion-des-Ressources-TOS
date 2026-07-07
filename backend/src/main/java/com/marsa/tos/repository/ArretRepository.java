package com.marsa.tos.repository;

import com.marsa.tos.domain.exploitation.Arret;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArretRepository extends JpaRepository<Arret, String> {
    List<Arret> findByEscaleId(String escaleId);
    List<Arret> findByEquipementCode(String equipementCode);
}
