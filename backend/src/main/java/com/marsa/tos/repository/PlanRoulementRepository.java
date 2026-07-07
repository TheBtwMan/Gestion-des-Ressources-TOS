package com.marsa.tos.repository;

import com.marsa.tos.domain.parametrage.PlanRoulement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRoulementRepository extends JpaRepository<PlanRoulement, Long> {
    List<PlanRoulement> findByEquipeId(String equipeId);
}
