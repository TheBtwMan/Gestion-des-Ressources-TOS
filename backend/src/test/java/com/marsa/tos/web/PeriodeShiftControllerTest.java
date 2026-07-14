package com.marsa.tos.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marsa.tos.domain.parametrage.PeriodeShift;
import com.marsa.tos.domain.referentiel.Terminal;
import com.marsa.tos.repository.PeriodeShiftRepository;
import com.marsa.tos.repository.TerminalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PeriodeShiftController.class)
class PeriodeShiftControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PeriodeShiftRepository periodeShiftRepository;

    @MockitoBean
    private TerminalRepository terminalRepository;

    @Test
    @WithMockUser
    void givenExistingTerminalId_whenGetByTerminal_thenReturnsPeriodeShift() throws Exception {
        // Given
        Long terminalId = 1L;
        PeriodeShift periodeShift = new PeriodeShift();
        periodeShift.setId(10L);
        periodeShift.setShift1NormalDebut(LocalTime.of(6, 45));
        periodeShift.setShift1NormalFin(LocalTime.of(14, 45));

        when(periodeShiftRepository.findByTerminalId(terminalId)).thenReturn(Optional.of(periodeShift));

        // When & Then
        mockMvc.perform(get("/api/parametrage/periode-shift/terminal/{terminalId}", terminalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.shift1NormalDebut").value("06:45:00"));
    }

    @Test
    @WithMockUser
    void givenNonExistingTerminalId_whenGetByTerminal_thenReturnsNotFound() throws Exception {
        // Given
        Long terminalId = 999L;
        when(periodeShiftRepository.findByTerminalId(terminalId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/parametrage/periode-shift/terminal/{terminalId}", terminalId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenValidDataAndParametrageRole_whenUpsert_thenSavesAndReturnsPeriodeShift() throws Exception {
        // Given
        Long terminalId = 1L;
        Terminal terminal = new Terminal();
        terminal.setId(terminalId);
        terminal.setNom("Terminal Container");

        PeriodeShift input = new PeriodeShift();
        input.setShift1NormalDebut(LocalTime.of(7, 0));
        input.setShift1NormalFin(LocalTime.of(15, 0));

        PeriodeShift saved = new PeriodeShift();
        saved.setId(15L);
        saved.setTerminal(terminal);
        saved.setShift1NormalDebut(LocalTime.of(7, 0));
        saved.setShift1NormalFin(LocalTime.of(15, 0));

        when(terminalRepository.findById(terminalId)).thenReturn(Optional.of(terminal));
        when(periodeShiftRepository.findByTerminalId(terminalId)).thenReturn(Optional.empty());
        when(periodeShiftRepository.save(any(PeriodeShift.class))).thenReturn(saved);

        // When & Then
        mockMvc.perform(put("/api/parametrage/periode-shift/terminal/{terminalId}", terminalId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15L))
                .andExpect(jsonPath("$.shift1NormalDebut").value("07:00:00"));
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenNonExistingTerminal_whenUpsert_thenReturnsBadRequest() throws Exception {
        // Given
        Long terminalId = 999L;
        PeriodeShift input = new PeriodeShift();

        when(terminalRepository.findById(terminalId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/parametrage/periode-shift/terminal/{terminalId}", terminalId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Terminal introuvable : 999"));
    }
}
