package com.marsa.tos.repository;

import com.marsa.tos.domain.parametrage.NormeProductivite;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NormeProductiviteRepository extends JpaRepository<NormeProductivite, Long> {
    List<NormeProductivite> findByTraficId(Long traficId);
}
