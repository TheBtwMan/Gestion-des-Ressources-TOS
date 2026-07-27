package com.marsa.tos.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marsa.tos.common.Enums.Shift;
import com.marsa.tos.common.Enums.StatutCommande;
import com.marsa.tos.domain.exploitation.AffectationPrevisionnelle;
import com.marsa.tos.domain.exploitation.Commande;
import com.marsa.tos.domain.parametrage.MainTheorique;
import com.marsa.tos.repository.AffectationPrevisionnelleRepository;
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

@WebMvcTest(AffectationPrevisionnelleController.class)
class AffectationPrevisionnelleControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AffectationPrevisionnelleRepository repository;

    @MockitoBean
    private CommandeRepository commandeRepository;

    @MockitoBean
    private MainTheoriqueRepository mainTheoriqueRepository;

    @Test
    @WithMockUser
    void givenCommandeNumero_whenByCommande_thenReturnsAffectations() throws Exception {
        // Given
        AffectationPrevisionnelle aff = new AffectationPrevisionnelle();
        aff.setId(1L);
        aff.setShift(Shift.SHIFT_1);
        when(repository.findByCommandeNumero("CMD-100")).thenReturn(List.of(aff));

        // When & Then
        mockMvc.perform(get("/api/exploitation/affectations-previsionnelles")
                        .param("commandeNumero", "CMD-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser
    void givenExistingId_whenGet_thenReturnsAffectation() throws Exception {
        // Given
        AffectationPrevisionnelle aff = new AffectationPrevisionnelle();
        aff.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(aff));

        // When & Then
        mockMvc.perform(get("/api/exploitation/affectations-previsionnelles/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void givenNonExistingId_whenGet_thenReturnsNotFound() throws Exception {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/exploitation/affectations-previsionnelles/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_PREVISIONNELLE")
    void givenValidData_whenCreate_thenSavesAndReturnsAffectation() throws Exception {
        // Given
        Commande commande = new Commande();
        commande.setNumero("CMD-100");
        commande.setStatut(StatutCommande.CREEE);

        MainTheorique main = new MainTheorique();
        main.setId(10L);

        AffectationPrevisionnelle input = new AffectationPrevisionnelle();
        input.setCommande(commande);
        input.setMainTheorique(main);
        input.setDateTravail(LocalDate.of(2026, 7, 10));
        input.setShift(Shift.SHIFT_1);
        input.setRessourcesHumaines(new ArrayList<>());
        input.setRessourcesMaterielles(new ArrayList<>());
        input.setAccessoires(new ArrayList<>());

        AffectationPrevisionnelle saved = new AffectationPrevisionnelle();
        saved.setId(100L);
        saved.setShift(Shift.SHIFT_1);

        when(commandeRepository.findById("CMD-100")).thenReturn(Optional.of(commande));
        when(mainTheoriqueRepository.findById(10L)).thenReturn(Optional.of(main));
        when(repository.save(any(AffectationPrevisionnelle.class))).thenReturn(saved);

        // When & Then
        mockMvc.perform(post("/api/exploitation/affectations-previsionnelles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_PREVISIONNELLE")
    void givenCommandeValidee_whenCreate_thenReturnsConflict() throws Exception {
        // Given
        Commande commande = new Commande();
        commande.setNumero("CMD-VALID");
        commande.setStatut(StatutCommande.VALIDEE);

        MainTheorique main = new MainTheorique();
        main.setId(10L);

        AffectationPrevisionnelle input = new AffectationPrevisionnelle();
        input.setCommande(commande);
        input.setMainTheorique(main);
        input.setRessourcesHumaines(new ArrayList<>());
        input.setRessourcesMaterielles(new ArrayList<>());
        input.setAccessoires(new ArrayList<>());

        when(commandeRepository.findById("CMD-VALID")).thenReturn(Optional.of(commande));

        // When & Then
        mockMvc.perform(post("/api/exploitation/affectations-previsionnelles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Commande validée : les affectations ne peuvent plus être modifiées."));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_PREVISIONNELLE")
    void givenCommandeIntrouvable_whenCreate_thenReturnsBadRequest() throws Exception {
        // Given
        Commande commande = new Commande();
        commande.setNumero("CMD-MISSING");

        MainTheorique main = new MainTheorique();
        main.setId(10L);

        AffectationPrevisionnelle input = new AffectationPrevisionnelle();
        input.setCommande(commande);
        input.setMainTheorique(main);
        input.setRessourcesHumaines(new ArrayList<>());
        input.setRessourcesMaterielles(new ArrayList<>());
        input.setAccessoires(new ArrayList<>());

        when(commandeRepository.findById("CMD-MISSING")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/exploitation/affectations-previsionnelles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Commande introuvable"));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_PREVISIONNELLE")
    void givenExistingId_whenDelete_thenReturnsNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/exploitation/affectations-previsionnelles/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    @WithMockUser(roles = "CONSULTATION")
    void givenForbiddenRole_whenCreate_thenReturnsForbidden() throws Exception {
        // Given
        AffectationPrevisionnelle input = new AffectationPrevisionnelle();
        input.setRessourcesHumaines(new ArrayList<>());
        input.setRessourcesMaterielles(new ArrayList<>());
        input.setAccessoires(new ArrayList<>());

        // When & Then
        mockMvc.perform(post("/api/exploitation/affectations-previsionnelles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }
}
