package com.marsa.tos.security;

import com.marsa.tos.domain.admin.Droit;
import com.marsa.tos.domain.admin.Profil;
import com.marsa.tos.domain.admin.Utilisateur;
import com.marsa.tos.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour UserDetailsServiceImpl avec Mockito (sans contexte Spring).
 */
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void givenExistingActiveUser_whenLoadByUsername_thenReturnsUserDetailsWithAuthorities() {
        // Given
        Droit droitParam = new Droit();
        droitParam.setCode("PARAMETRAGE");
        droitParam.setLibelle("Paramétrage");

        Droit droitConsult = new Droit();
        droitConsult.setCode("CONSULTATION");
        droitConsult.setLibelle("Consultation");

        Profil profil = new Profil();
        profil.setId(1L);
        profil.setNom("Super Admin");
        profil.setDroits(List.of(droitParam, droitConsult));

        Utilisateur utilisateur = Utilisateur.builder()
                .matricule("ADMIN001")
                .nom("Maroc")
                .prenom("Marsa")
                .motDePasseHash("$2a$10$hashedpassword")
                .actif(true)
                .profils(List.of(profil))
                .build();

        when(utilisateurRepository.findById("ADMIN001")).thenReturn(Optional.of(utilisateur));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("ADMIN001");

        // Then
        assertThat(userDetails.getUsername()).isEqualTo("ADMIN001");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$hashedpassword");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities()).hasSize(2);
        assertThat(userDetails.getAuthorities().stream().map(Object::toString))
                .containsExactlyInAnyOrder("ROLE_PARAMETRAGE", "ROLE_CONSULTATION");
    }

    @Test
    void givenInactiveUser_whenLoadByUsername_thenReturnsDisabledUserDetails() {
        // Given
        Utilisateur utilisateur = Utilisateur.builder()
                .matricule("DISABLED001")
                .nom("Dupont")
                .prenom("Jean")
                .motDePasseHash("$2a$10$hashed")
                .actif(false)
                .profils(new ArrayList<>())
                .build();

        when(utilisateurRepository.findById("DISABLED001")).thenReturn(Optional.of(utilisateur));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("DISABLED001");

        // Then
        assertThat(userDetails.getUsername()).isEqualTo("DISABLED001");
        assertThat(userDetails.isEnabled()).isFalse();
        assertThat(userDetails.getAuthorities()).isEmpty();
    }

    @Test
    void givenNonExistingUser_whenLoadByUsername_thenThrowsUsernameNotFoundException() {
        // Given
        when(utilisateurRepository.findById("GHOST")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("GHOST"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("GHOST");
    }

    @Test
    void givenUserWithMultipleProfils_whenLoadByUsername_thenDeduplicatesAuthorities() {
        // Given
        Droit droitConsult = new Droit();
        droitConsult.setCode("CONSULTATION");
        droitConsult.setLibelle("Consultation");

        Droit droitParam = new Droit();
        droitParam.setCode("PARAMETRAGE");
        droitParam.setLibelle("Paramétrage");

        Profil profil1 = new Profil();
        profil1.setId(1L);
        profil1.setNom("Profil A");
        profil1.setDroits(List.of(droitConsult, droitParam));

        Profil profil2 = new Profil();
        profil2.setId(2L);
        profil2.setNom("Profil B");
        profil2.setDroits(List.of(droitConsult)); // duplicate CONSULTATION

        Utilisateur utilisateur = Utilisateur.builder()
                .matricule("MULTI001")
                .nom("Multi")
                .prenom("User")
                .motDePasseHash("$2a$10$hashed")
                .actif(true)
                .profils(List.of(profil1, profil2))
                .build();

        when(utilisateurRepository.findById("MULTI001")).thenReturn(Optional.of(utilisateur));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("MULTI001");

        // Then — CONSULTATION appears in both profils but should be deduplicated
        assertThat(userDetails.getAuthorities()).hasSize(2);
        assertThat(userDetails.getAuthorities().stream().map(Object::toString))
                .containsExactlyInAnyOrder("ROLE_PARAMETRAGE", "ROLE_CONSULTATION");
    }
}
