package com.marsa.tos.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marsa.tos.common.Enums.Shift;
import com.marsa.tos.common.Enums.StatutCommande;
import com.marsa.tos.domain.exploitation.AffectationReelle;
import com.marsa.tos.domain.exploitation.Commande;
import com.marsa.tos.domain.parametrage.MainTheorique;
import com.marsa.tos.repository.AffectationReelleRepository;
import com.marsa.tos.repository.CommandeRepository;
import com.marsa.tos.repository.MainTheoriqueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AffectationReelleController.class)
class AffectationReelleControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AffectationReelleRepository repository;

    @MockitoBean
    private CommandeRepository commandeRepository;

    @MockitoBean
    private MainTheoriqueRepository mainTheoriqueRepository;

    @Test
    @WithMockUser
    void givenCommandeNumero_whenByCommande_thenReturnsAffectations() throws Exception {
        // Given
        AffectationReelle aff = new AffectationReelle();
        aff.setId(1L);
        aff.setShift(Shift.SHIFT_2);
        when(repository.findByCommandeNumero("CMD-200")).thenReturn(List.of(aff));

        // When & Then
        mockMvc.perform(get("/api/exploitation/affectations-reelles")
                        .param("commandeNumero", "CMD-200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser
    void givenExistingId_whenGet_thenReturnsAffectation() throws Exception {
        // Given
        AffectationReelle aff = new AffectationReelle();
        aff.setId(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(aff));

        // When & Then
        mockMvc.perform(get("/api/exploitation/affectations-reelles/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @WithMockUser
    void givenNonExistingId_whenGet_thenReturnsNotFound() throws Exception {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/exploitation/affectations-reelles/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_REELLE")
    void givenValidData_whenCreate_thenSavesAndReturnsAffectation() throws Exception {
        // Given
        Commande commande = new Commande();
        commande.setNumero("CMD-200");
        commande.setStatut(StatutCommande.EN_COURS);
        commande.setTonnageRealiseParShift(true);

        MainTheorique main = new MainTheorique();
        main.setId(10L);

        AffectationReelle input = new AffectationReelle();
        input.setCommande(commande);
        input.setMainTheorique(main);
        input.setDateTravail(LocalDate.of(2026, 7, 10));
        input.setShift(Shift.SHIFT_2);
        input.setTonnageRealise(750);
        input.setCdi(5);
        input.setCdd(2);
        input.setSousTraitant(1);
        input.setRessourcesHumaines(new ArrayList<>());
        input.setRessourcesMaterielles(new ArrayList<>());
        input.setAccessoires(new ArrayList<>());

        AffectationReelle saved = new AffectationReelle();
        saved.setId(200L);
        saved.setShift(Shift.SHIFT_2);
        saved.setTonnageRealise(750);

        when(commandeRepository.findById("CMD-200")).thenReturn(Optional.of(commande));
        when(mainTheoriqueRepository.findById(10L)).thenReturn(Optional.of(main));
        when(repository.save(any(AffectationReelle.class))).thenReturn(saved);

        // When & Then
        mockMvc.perform(post("/api/exploitation/affectations-reelles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(200))
                .andExpect(jsonPath("$.tonnageRealise").value(750));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_REELLE")
    void givenCommandeValidee_whenCreate_thenReturnsConflict() throws Exception {
        // Given
        Commande commande = new Commande();
        commande.setNumero("CMD-VALID");
        commande.setStatut(StatutCommande.VALIDEE);

        AffectationReelle input = new AffectationReelle();
        input.setCommande(commande);
        input.setRessourcesHumaines(new ArrayList<>());
        input.setRessourcesMaterielles(new ArrayList<>());
        input.setAccessoires(new ArrayList<>());

        when(commandeRepository.findById("CMD-VALID")).thenReturn(Optional.of(commande));

        // When & Then
        mockMvc.perform(post("/api/exploitation/affectations-reelles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Commande validée : les affectations ne peuvent plus être modifiées."));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_REELLE")
    void givenCommandeIntrouvable_whenCreate_thenReturnsBadRequest() throws Exception {
        // Given
        Commande commande = new Commande();
        commande.setNumero("CMD-MISSING");

        AffectationReelle input = new AffectationReelle();
        input.setCommande(commande);
        input.setRessourcesHumaines(new ArrayList<>());
        input.setRessourcesMaterielles(new ArrayList<>());
        input.setAccessoires(new ArrayList<>());

        when(commandeRepository.findById("CMD-MISSING")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/exploitation/affectations-reelles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Commande introuvable"));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_REELLE")
    void givenExistingId_whenDelete_thenReturnsNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/exploitation/affectations-reelles/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    @WithMockUser(roles = "CONSULTATION")
    void givenForbiddenRole_whenCreate_thenReturnsForbidden() throws Exception {
        // Given
        AffectationReelle input = new AffectationReelle();
        input.setRessourcesHumaines(new ArrayList<>());
        input.setRessourcesMaterielles(new ArrayList<>());
        input.setAccessoires(new ArrayList<>());

        // When & Then
        mockMvc.perform(post("/api/exploitation/affectations-reelles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }
}
