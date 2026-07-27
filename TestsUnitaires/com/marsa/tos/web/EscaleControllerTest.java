package com.marsa.tos.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marsa.tos.common.Enums.StatutEscale;
import com.marsa.tos.domain.exploitation.Commande;
import com.marsa.tos.domain.exploitation.Escale;
import com.marsa.tos.repository.ArretRepository;
import com.marsa.tos.repository.CommandeRepository;
import com.marsa.tos.repository.EscaleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EscaleController.class)
class EscaleControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EscaleRepository escaleRepository;

    @MockitoBean
    private CommandeRepository commandeRepository;

    @MockitoBean
    private ArretRepository arretRepository;

    @Test
    @WithMockUser
    void givenNoStatut_whenAll_thenReturnsAllEscales() throws Exception {
        Escale escale = new Escale();
        escale.setId("ESC-1");
        when(escaleRepository.findAll()).thenReturn(List.of(escale));

        mockMvc.perform(get("/api/exploitation/escales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("ESC-1"));
    }

    @Test
    @WithMockUser
    void givenStatutFilter_whenAll_thenReturnsFilteredEscales() throws Exception {
        Escale escale = new Escale();
        escale.setId("ESC-EN-COURS");
        escale.setStatut(StatutEscale.EN_COURS);
        when(escaleRepository.findByStatut(StatutEscale.EN_COURS)).thenReturn(List.of(escale));

        mockMvc.perform(get("/api/exploitation/escales").param("statut", "EN_COURS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("ESC-EN-COURS"));
    }

    @Test
    @WithMockUser
    void givenExistingEscaleId_whenGet_thenReturnsEscale() throws Exception {
        String escaleId = "ESC-1";
        Escale escale = new Escale();
        escale.setId(escaleId);
        escale.setNavire("Maroc Star");
        escale.setStatut(StatutEscale.PREVU);

        when(escaleRepository.findById(escaleId)).thenReturn(Optional.of(escale));

        mockMvc.perform(get("/api/exploitation/escales/{id}", escaleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(escaleId))
                .andExpect(jsonPath("$.navire").value("Maroc Star"));
    }

    @Test
    @WithMockUser
    void givenNonExistingEscaleId_whenGet_thenReturnsNotFound() throws Exception {
        when(escaleRepository.findById("ESC-MISSING")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/exploitation/escales/{id}", "ESC-MISSING"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void givenExistingEscale_whenGetCommandes_thenReturnsCommandesList() throws Exception {
        Commande cmd = new Commande();
        cmd.setNumero("CMD-1");
        when(commandeRepository.findByEscaleId("ESC-1")).thenReturn(List.of(cmd));

        mockMvc.perform(get("/api/exploitation/escales/{id}/commandes", "ESC-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value("CMD-1"));
    }

    @Test
    @WithMockUser
    void givenExistingEscale_whenPreparerCloture_thenReturnsClotureDetails() throws Exception {
        String escaleId = "ESC-1";
        Escale escale = new Escale();
        escale.setId(escaleId);
        escale.setDateArriveeReelle(LocalDateTime.of(2026, 7, 14, 10, 0));

        when(escaleRepository.findById(escaleId)).thenReturn(Optional.of(escale));
        when(commandeRepository.findByEscaleId(escaleId)).thenReturn(Collections.emptyList());
        when(arretRepository.findByEscaleId(escaleId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/exploitation/escales/{id}/cloture", escaleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateDebutTravailSuggeree").value("2026-07-14T10:00:00"));
    }

    @Test
    @WithMockUser(roles = "VALIDATION")
    void givenExistingEscaleAndValidationRole_whenCloturer_thenUpdatesToCloturee() throws Exception {
        String escaleId = "ESC-1";
        Escale escale = new Escale();
        escale.setId(escaleId);
        escale.setStatut(StatutEscale.EN_COURS);

        Escale saved = new Escale();
        saved.setId(escaleId);
        saved.setStatut(StatutEscale.CLOTUREE);

        when(escaleRepository.findById(escaleId)).thenReturn(Optional.of(escale));
        when(escaleRepository.save(any(Escale.class))).thenReturn(saved);

        Map<String, String> body = Map.of(
                "dateDebutTravail", "2026-07-14T10:00:00",
                "dateFinTravail", "2026-07-14T18:00:00"
        );

        mockMvc.perform(post("/api/exploitation/escales/{id}/cloturer", escaleId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("CLOTUREE"));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_REELLE")
    void givenForbiddenRole_whenCloturer_thenReturnsForbidden() throws Exception {
        String escaleId = "ESC-1";
        Map<String, String> body = Collections.emptyMap();

        mockMvc.perform(post("/api/exploitation/escales/{id}/cloturer", escaleId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }
}
