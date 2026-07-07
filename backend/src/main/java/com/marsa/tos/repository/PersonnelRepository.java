package com.marsa.tos.repository;

import com.marsa.tos.domain.referentiel.Personnel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonnelRepository extends JpaRepository<Personnel, String> {
    List<Personnel> findByEquipeId(String equipeId);
    List<Personnel> findByFonctionCode(String fonctionCode);
}
