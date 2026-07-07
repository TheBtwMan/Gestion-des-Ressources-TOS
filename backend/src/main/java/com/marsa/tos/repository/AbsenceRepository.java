package com.marsa.tos.repository;

import com.marsa.tos.domain.exploitation.Absence;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbsenceRepository extends JpaRepository<Absence, String> {
    List<Absence> findByEscaleId(String escaleId);
    List<Absence> findByPersonnelMatricule(String matricule);
}
