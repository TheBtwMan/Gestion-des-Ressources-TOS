package com.marsa.tos.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marsa.tos.domain.admin.Profil;
import com.marsa.tos.domain.admin.Utilisateur;
import com.marsa.tos.repository.DroitRepository;
import com.marsa.tos.repository.ProfilRepository;
import com.marsa.tos.repository.TerminalRepository;
import com.marsa.tos.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DroitRepository droitRepository;

    @MockitoBean
    private ProfilRepository profilRepository;

    @MockitoBean
    private UtilisateurRepository utilisateurRepository;

    @MockitoBean
    private TerminalRepository terminalRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(roles = "GESTION_UTILISATEURS")
    void givenValidDataAndGestionRole_whenCreateProfil_thenSavesAndReturnsProfil() throws Exception {
        // Given
        Profil input = new Profil();
        input.setNom("Resp. Equipe");
        input.setDroits(new ArrayList<>());

        Profil saved = new Profil();
        saved.setId(10L);
        saved.setNom("Resp. Equipe");
        saved.setDroits(new ArrayList<>());

        when(profilRepository.save(any(Profil.class))).thenReturn(saved);

        // When & Then
        mockMvc.perform(post("/api/admin/profils")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.nom").value("Resp. Equipe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN_PORT")
    void givenAdminPortRole_whenCreateProfil_thenReturnsForbidden() throws Exception {
        // Given
        Profil input = new Profil();

        // When & Then
        mockMvc.perform(post("/api/admin/profils")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN_PORT")
    void givenAdminPortRole_whenCreateUtilisateur_thenSavesAndReturnsUtilisateur() throws Exception {
        // Given
        AdminController.UtilisateurRequest body = new AdminController.UtilisateurRequest();
        body.matricule = "USER001";
        body.nom = "Nom";
        body.prenom = "Prenom";
        body.motDePasse = "pass";
        body.profilIds = List.of(1L);

        Profil profil = new Profil();
        profil.setId(1L);

        Utilisateur saved = new Utilisateur();
        saved.setMatricule("USER001");
        saved.setNom("Nom");
        saved.setPrenom("Prenom");
        saved.setProfils(List.of(profil));

        when(profilRepository.findAllById(List.of(1L))).thenReturn(List.of(profil));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(saved);

        // When & Then
        mockMvc.perform(post("/api/admin/utilisateurs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricule").value("USER001"))
                .andExpect(jsonPath("$.nom").value("Nom"));
    }
}
