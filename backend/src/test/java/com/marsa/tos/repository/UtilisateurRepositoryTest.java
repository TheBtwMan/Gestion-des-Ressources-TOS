package com.marsa.tos.repository;

import com.marsa.tos.domain.admin.Utilisateur;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UtilisateurRepositoryTest {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void givenNewUtilisateur_whenSave_thenCanBeFoundByMatricule() {
        // Given
        Utilisateur user = Utilisateur.builder()
                .matricule("USER999")
                .nom("Dupont")
                .prenom("Jean")
                .motDePasseHash("hash-pwd")
                .actif(true)
                .build();

        // When
        utilisateurRepository.save(user);
        Optional<Utilisateur> found = utilisateurRepository.findById("USER999");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNom()).isEqualTo("Dupont");
        assertThat(found.get().isActif()).isTrue();
    }

    @Test
    void givenExistingUtilisateur_whenDeactivate_thenIsActifIsFalse() {
        // Given
        Utilisateur user = Utilisateur.builder()
                .matricule("USER888")
                .nom("Durand")
                .prenom("Marie")
                .motDePasseHash("hash-pwd")
                .actif(true)
                .build();
        utilisateurRepository.save(user);

        // When
        Utilisateur saved = utilisateurRepository.findById("USER888").orElseThrow();
        saved.setActif(false);
        utilisateurRepository.save(saved);

        Optional<Utilisateur> updated = utilisateurRepository.findById("USER888");

        // Then
        assertThat(updated).isPresent();
        assertThat(updated.get().isActif()).isFalse();
    }
}
