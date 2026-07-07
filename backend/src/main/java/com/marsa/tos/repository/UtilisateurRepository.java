package com.marsa.tos.repository;

import com.marsa.tos.domain.admin.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {
}
