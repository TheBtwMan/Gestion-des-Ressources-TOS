package com.marsa.tos.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marsa.tos.domain.admin.Droit;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    void whenGetDroits_thenReturnsAllDroits() throws Exception {
        Droit droit = new Droit();
        droit.setCode("PARAMETRAGE");
        droit.setLibelle("Paramétrage");
        when(droitRepository.findAll()).thenReturn(List.of(droit));

        mockMvc.perform(get("/api/admin/droits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("PARAMETRAGE"));
    }

    @Test
    @WithMockUser(roles = "GESTION_UTILISATEURS")
    void whenGetProfils_thenReturnsAllProfils() throws Exception {
        Profil profil = new Profil();
        profil.setId(1L);
        profil.setNom("Super Admin");
        when(profilRepository.findAll()).thenReturn(List.of(profil));

        mockMvc.perform(get("/api/admin/profils"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Super Admin"));
    }

    @Test
    @WithMockUser(roles = "GESTION_UTILISATEURS")
    void givenValidDroits_whenSetDroits_thenUpdatesAndReturnsProfil() throws Exception {
        Profil profil = new Profil();
        profil.setId(1L);
        profil.setNom("Admin");
        profil.setDroits(new ArrayList<>());

        Droit droit = new Droit();
        droit.setCode("PARAMETRAGE");

        when(profilRepository.findById(1L)).thenReturn(Optional.of(profil));
        when(droitRepository.findAllById(List.of("PARAMETRAGE"))).thenReturn(List.of(droit));
        when(profilRepository.save(any(Profil.class))).thenReturn(profil);

        mockMvc.perform(put("/api/admin/profils/{id}/droits", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of("PARAMETRAGE"))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "GESTION_UTILISATEURS")
    void givenEmptyDroits_whenSetDroits_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/admin/profils/{id}/droits", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Collections.emptyList())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Un profil doit contenir au moins un droit."));
    }

    @Test
    @WithMockUser(roles = "GESTION_UTILISATEURS")
    void givenValidDataAndGestionRole_whenCreateProfil_thenSavesAndReturnsProfil() throws Exception {
        Profil input = new Profil();
        input.setNom("Resp. Equipe");
        input.setDroits(new ArrayList<>());

        Profil saved = new Profil();
        saved.setId(10L);
        saved.setNom("Resp. Equipe");
        saved.setDroits(new ArrayList<>());

        when(profilRepository.save(any(Profil.class))).thenReturn(saved);

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
        Profil input = new Profil();

        mockMvc.perform(post("/api/admin/profils")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GESTION_UTILISATEURS")
    void whenGetUtilisateurs_thenReturnsAllUtilisateurs() throws Exception {
        Utilisateur user = new Utilisateur();
        user.setMatricule("USER001");
        when(utilisateurRepository.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/admin/utilisateurs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matricule").value("USER001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN_PORT")
    void givenAdminPortRole_whenCreateUtilisateur_thenSavesAndReturnsUtilisateur() throws Exception {
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

        mockMvc.perform(post("/api/admin/utilisateurs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricule").value("USER001"))
                .andExpect(jsonPath("$.nom").value("Nom"));
    }

    @Test
    @WithMockUser(roles = "ADMIN_PORT")
    void givenNoProfils_whenCreateUtilisateur_thenReturnsBadRequest() throws Exception {
        AdminController.UtilisateurRequest body = new AdminController.UtilisateurRequest();
        body.matricule = "USER002";
        body.profilIds = Collections.emptyList();

        mockMvc.perform(post("/api/admin/utilisateurs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Un utilisateur doit avoir au moins un profil."));
    }

    @Test
    @WithMockUser(roles = "GESTION_UTILISATEURS")
    void givenValidData_whenUpdateUtilisateur_thenUpdatesAndReturnsUtilisateur() throws Exception {
        AdminController.UtilisateurRequest body = new AdminController.UtilisateurRequest();
        body.nom = "NomModifie";
        body.prenom = "PrenomModifie";

        Utilisateur user = new Utilisateur();
        user.setMatricule("USER001");

        when(utilisateurRepository.findById("USER001")).thenReturn(Optional.of(user));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(user);

        mockMvc.perform(put("/api/admin/utilisateurs/{matricule}", "USER001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "GESTION_UTILISATEURS")
    void givenExistingMatricule_whenDeactivate_thenDeactivatesUser() throws Exception {
        Utilisateur user = new Utilisateur();
        user.setMatricule("USER001");
        user.setActif(true);

        when(utilisateurRepository.findById("USER001")).thenReturn(Optional.of(user));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(user);

        mockMvc.perform(delete("/api/admin/utilisateurs/{matricule}", "USER001")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
