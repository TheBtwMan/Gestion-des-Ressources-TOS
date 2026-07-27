package com.marsa.tos.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marsa.tos.domain.exploitation.Absence;
import com.marsa.tos.domain.exploitation.Escale;
import com.marsa.tos.domain.referentiel.Personnel;
import com.marsa.tos.repository.AbsenceRepository;
import com.marsa.tos.repository.EscaleRepository;
import com.marsa.tos.repository.PersonnelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AbsenceController.class)
class AbsenceControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AbsenceRepository absenceRepository;

    @MockitoBean
    private EscaleRepository escaleRepository;

    @MockitoBean
    private PersonnelRepository personnelRepository;

    @Test
    @WithMockUser
    void givenNoFilters_whenAll_thenReturnsAllAbsences() throws Exception {
        // Given
        Absence abs = new Absence();
        abs.setId("ABS-101");
        abs.setMotif("Maladie");
        when(absenceRepository.findAll()).thenReturn(List.of(abs));

        // When & Then
        mockMvc.perform(get("/api/exploitation/absences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("ABS-101"))
                .andExpect(jsonPath("$[0].motif").value("Maladie"));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_REELLE")
    void givenValidAbsenceAndReelleRole_whenCreate_thenSavesAndReturnsAbsence() throws Exception {
        // Given
        Personnel personnel = new Personnel();
        personnel.setMatricule("MATR-123");

        Absence input = new Absence();
        input.setPersonnel(personnel);
        input.setMotif("Maladie");

        Absence saved = new Absence();
        saved.setId("ABS-ABCDEF");
        saved.setPersonnel(personnel);
        saved.setMotif("Maladie");

        when(personnelRepository.findById("MATR-123")).thenReturn(Optional.of(personnel));
        when(absenceRepository.save(any(Absence.class))).thenReturn(saved);

        // When & Then
        mockMvc.perform(post("/api/exploitation/absences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ABS-ABCDEF"))
                .andExpect(jsonPath("$.motif").value("Maladie"));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_REELLE")
    void givenAbsenceWithNonExistingPersonnel_whenCreate_thenReturnsBadRequest() throws Exception {
        // Given
        Personnel personnel = new Personnel();
        personnel.setMatricule("NON-EXISTENT");

        Absence input = new Absence();
        input.setPersonnel(personnel);

        when(personnelRepository.findById("NON-EXISTENT")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/exploitation/absences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Personnel introuvable"));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_REELLE")
    void givenExistingAbsenceId_whenDelete_thenDeletesAndReturnsNoContent() throws Exception {
        // Given
        String id = "ABS-101";

        // When & Then
        mockMvc.perform(delete("/api/exploitation/absences/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(absenceRepository, times(1)).deleteById(id);
    }

    @Test
    @WithMockUser
    void givenEscaleFilter_whenAll_thenReturnsFilteredAbsences() throws Exception {
        // Given
        String escaleId = "ESC-500";
        Absence abs = new Absence();
        abs.setId("ABS-FILTERED");
        abs.setMotif("Congé");
        when(absenceRepository.findByEscaleId(escaleId)).thenReturn(List.of(abs));

        // When & Then
        mockMvc.perform(get("/api/exploitation/absences").param("escaleId", escaleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("ABS-FILTERED"))
                .andExpect(jsonPath("$[0].motif").value("Congé"));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_REELLE")
    void givenAbsenceWithEscale_whenCreate_thenResolvesEscaleAndSaves() throws Exception {
        // Given
        Personnel personnel = new Personnel();
        personnel.setMatricule("MATR-123");

        Escale escale = new Escale();
        escale.setId("ESC-500");

        Absence input = new Absence();
        input.setPersonnel(personnel);
        input.setEscale(escale);
        input.setMotif("Formation");

        Absence saved = new Absence();
        saved.setId("ABS-ESC");
        saved.setPersonnel(personnel);
        saved.setEscale(escale);
        saved.setMotif("Formation");

        when(personnelRepository.findById("MATR-123")).thenReturn(Optional.of(personnel));
        when(escaleRepository.findById("ESC-500")).thenReturn(Optional.of(escale));
        when(absenceRepository.save(any(Absence.class))).thenReturn(saved);

        // When & Then
        mockMvc.perform(post("/api/exploitation/absences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ABS-ESC"))
                .andExpect(jsonPath("$.motif").value("Formation"));
    }

    @Test
    @WithMockUser(roles = "CONSULTATION")
    void givenForbiddenRole_whenCreate_thenReturnsForbidden() throws Exception {
        // Given
        Absence input = new Absence();

        // When & Then
        mockMvc.perform(post("/api/exploitation/absences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }
}
