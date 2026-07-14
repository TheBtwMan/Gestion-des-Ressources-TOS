package com.marsa.tos.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marsa.tos.common.Enums.JourType;
import com.marsa.tos.common.Enums.SemaineType;
import com.marsa.tos.domain.parametrage.ModeTravail;
import com.marsa.tos.domain.referentiel.Terminal;
import com.marsa.tos.repository.ModeTravailRepository;
import com.marsa.tos.repository.TerminalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModeTravailController.class)
class ModeTravailControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ModeTravailRepository modeTravailRepository;

    @MockitoBean
    private TerminalRepository terminalRepository;

    @Test
    @WithMockUser
    void givenExistingTerminalId_whenGetByTerminal_thenReturnsModeTravail() throws Exception {
        // Given
        Long terminalId = 1L;
        ModeTravail modeTravail = new ModeTravail();
        modeTravail.setId(10L);
        modeTravail.setSemaine(SemaineType.SIX_SUR_SEPT);
        modeTravail.setJour(JourType.DEUX_SHIFTS);
        
        when(modeTravailRepository.findByTerminalId(terminalId)).thenReturn(Optional.of(modeTravail));

        // When & Then
        mockMvc.perform(get("/api/parametrage/mode-travail/terminal/{terminalId}", terminalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.semaine").value("SIX_SUR_SEPT"))
                .andExpect(jsonPath("$.jour").value("DEUX_SHIFTS"));
    }

    @Test
    @WithMockUser
    void givenNonExistingTerminalId_whenGetByTerminal_thenReturnsNotFound() throws Exception {
        // Given
        Long terminalId = 999L;
        when(modeTravailRepository.findByTerminalId(terminalId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/parametrage/mode-travail/terminal/{terminalId}", terminalId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenValidDataAndParametrageRole_whenUpsert_thenSavesAndReturnsModeTravail() throws Exception {
        // Given
        Long terminalId = 1L;
        Terminal terminal = new Terminal();
        terminal.setId(terminalId);
        terminal.setNom("Terminal Container");

        ModeTravail input = new ModeTravail();
        input.setSemaine(SemaineType.SEPT_SUR_SEPT);
        input.setJour(JourType.TROIS_SHIFTS);

        ModeTravail saved = new ModeTravail();
        saved.setId(15L);
        saved.setTerminal(terminal);
        saved.setSemaine(SemaineType.SEPT_SUR_SEPT);
        saved.setJour(JourType.TROIS_SHIFTS);

        when(terminalRepository.findById(terminalId)).thenReturn(Optional.of(terminal));
        when(modeTravailRepository.findByTerminalId(terminalId)).thenReturn(Optional.empty());
        when(modeTravailRepository.save(any(ModeTravail.class))).thenReturn(saved);

        // When & Then
        mockMvc.perform(put("/api/parametrage/mode-travail/terminal/{terminalId}", terminalId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15L))
                .andExpect(jsonPath("$.semaine").value("SEPT_SUR_SEPT"))
                .andExpect(jsonPath("$.jour").value("TROIS_SHIFTS"));
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenNonExistingTerminal_whenUpsert_thenReturnsBadRequest() throws Exception {
        // Given
        Long terminalId = 999L;
        ModeTravail input = new ModeTravail();
        input.setSemaine(SemaineType.SEPT_SUR_SEPT);
        input.setJour(JourType.TROIS_SHIFTS);

        when(terminalRepository.findById(terminalId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/parametrage/mode-travail/terminal/{terminalId}", terminalId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Terminal introuvable : 999"));
    }

    @Test
    @WithMockUser(roles = "CONSULTATION")
    void givenForbiddenRole_whenUpsert_thenReturnsForbiddenStatus() throws Exception {
        // Given
        Long terminalId = 1L;
        ModeTravail input = new ModeTravail();

        // When & Then
        mockMvc.perform(put("/api/parametrage/mode-travail/terminal/{terminalId}", terminalId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }
}
