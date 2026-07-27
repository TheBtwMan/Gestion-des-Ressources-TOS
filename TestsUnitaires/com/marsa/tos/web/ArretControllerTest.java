package com.marsa.tos.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marsa.tos.domain.exploitation.Arret;
import com.marsa.tos.domain.exploitation.Escale;
import com.marsa.tos.domain.referentiel.Equipement;
import com.marsa.tos.repository.ArretRepository;
import com.marsa.tos.repository.EquipementRepository;
import com.marsa.tos.repository.EscaleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArretController.class)
class ArretControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ArretRepository arretRepository;

    @MockitoBean
    private EscaleRepository escaleRepository;

    @MockitoBean
    private EquipementRepository equipementRepository;

    @Test
    @WithMockUser
    void givenNoFilters_whenAll_thenReturnsAllArrets() throws Exception {
        // Given
        Arret arret = new Arret();
        arret.setId("ARR-101");
        arret.setCodeArret("PANNE_GRUE");
        when(arretRepository.findAll()).thenReturn(List.of(arret));

        // When & Then
        mockMvc.perform(get("/api/exploitation/arrets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("ARR-101"))
                .andExpect(jsonPath("$[0].codeArret").value("PANNE_GRUE"));
    }

    @Test
    @WithMockUser
    void givenEscaleFilter_whenAll_thenReturnsFilteredArrets() throws Exception {
        // Given
        String escaleId = "ESC-1";
        Arret arret = new Arret();
        arret.setId("ARR-201");
        when(arretRepository.findByEscaleId(escaleId)).thenReturn(List.of(arret));

        // When & Then
        mockMvc.perform(get("/api/exploitation/arrets").param("escaleId", escaleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("ARR-201"));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_REELLE")
    void givenValidArretWithEquipement_whenCreate_thenSavesAndReturnsArret() throws Exception {
        // Given
        Equipement equipement = new Equipement();
        equipement.setCode("GRU-01");

        Arret input = new Arret();
        input.setEquipement(equipement);
        input.setCodeArret("PANNE");

        Arret saved = new Arret();
        saved.setId("ARR-NEW");
        saved.setCodeArret("PANNE");
        saved.setEquipement(equipement);

        when(equipementRepository.findById("GRU-01")).thenReturn(Optional.of(equipement));
        when(arretRepository.save(any(Arret.class))).thenReturn(saved);

        // When & Then
        mockMvc.perform(post("/api/exploitation/arrets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ARR-NEW"))
                .andExpect(jsonPath("$.codeArret").value("PANNE"));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_REELLE")
    void givenArretWithEscale_whenCreate_thenResolvesEscaleAndSaves() throws Exception {
        // Given
        Escale escale = new Escale();
        escale.setId("ESC-500");

        Arret input = new Arret();
        input.setEscale(escale);
        input.setCodeArret("METEO");

        Arret saved = new Arret();
        saved.setId("ARR-ESC");
        saved.setEscale(escale);
        saved.setCodeArret("METEO");

        when(escaleRepository.findById("ESC-500")).thenReturn(Optional.of(escale));
        when(arretRepository.save(any(Arret.class))).thenReturn(saved);

        // When & Then
        mockMvc.perform(post("/api/exploitation/arrets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ARR-ESC"));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_REELLE")
    void givenArretWithDates_whenCreate_thenCalculatesDureeMinutes() throws Exception {
        // Given
        Arret input = new Arret();
        input.setDateDebut(LocalDateTime.of(2026, 7, 14, 10, 0));
        input.setDateFin(LocalDateTime.of(2026, 7, 14, 12, 30));
        input.setCodeArret("MAINTENANCE");

        Arret saved = new Arret();
        saved.setId("ARR-DUR");
        saved.setDureeMinutes(150);

        when(arretRepository.save(any(Arret.class))).thenReturn(saved);

        // When & Then
        mockMvc.perform(post("/api/exploitation/arrets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dureeMinutes").value(150));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_REELLE")
    void givenArretWithNonExistingEquipement_whenCreate_thenReturnsBadRequest() throws Exception {
        // Given
        Equipement equipement = new Equipement();
        equipement.setCode("NON-EXISTENT");

        Arret input = new Arret();
        input.setEquipement(equipement);

        when(equipementRepository.findById("NON-EXISTENT")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/exploitation/arrets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Equipement introuvable"));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_REELLE")
    void givenExistingArretId_whenDelete_thenDeletesAndReturnsNoContent() throws Exception {
        // Given
        String id = "ARR-101";

        // When & Then
        mockMvc.perform(delete("/api/exploitation/arrets/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(arretRepository, times(1)).deleteById(id);
    }

    @Test
    @WithMockUser(roles = "CONSULTATION")
    void givenForbiddenRole_whenCreate_thenReturnsForbidden() throws Exception {
        // Given
        Arret input = new Arret();

        // When & Then
        mockMvc.perform(post("/api/exploitation/arrets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }
}
