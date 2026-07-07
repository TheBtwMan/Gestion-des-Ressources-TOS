package com.marsa.tos.repository;

import com.marsa.tos.domain.admin.Profil;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfilRepository extends JpaRepository<Profil, Long> {
    Optional<Profil> findByNom(String nom);
}
