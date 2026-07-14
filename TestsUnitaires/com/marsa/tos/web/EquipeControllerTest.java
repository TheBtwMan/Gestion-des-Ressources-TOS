package com.marsa.tos.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marsa.tos.domain.parametrage.Equipe;
import com.marsa.tos.domain.referentiel.Personnel;
import com.marsa.tos.domain.referentiel.Terminal;
import com.marsa.tos.repository.EquipeRepository;
import com.marsa.tos.repository.PersonnelRepository;
import com.marsa.tos.repository.TerminalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EquipeController.class)
class EquipeControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EquipeRepository equipeRepository;

    @MockitoBean
    private PersonnelRepository personnelRepository;

    @MockitoBean
    private TerminalRepository terminalRepository;

    @Test
    @WithMockUser
    void givenNoTerminalFilter_whenAll_thenReturnsAllEquipes() throws Exception {
        // Given
        Equipe equipe = new Equipe();
        equipe.setId("EQ-1");
        equipe.setNom("Equipe A");
        when(equipeRepository.findAll()).thenReturn(List.of(equipe));

        // When & Then
        mockMvc.perform(get("/api/parametrage/equipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("EQ-1"))
                .andExpect(jsonPath("$[0].nom").value("Equipe A"));
    }

    @Test
    @WithMockUser
    void givenTerminalFilter_whenAll_thenReturnsFilteredEquipes() throws Exception {
        // Given
        Long terminalId = 1L;
        Equipe equipe = new Equipe();
        equipe.setId("EQ-1");
        when(equipeRepository.findByTerminalId(terminalId)).thenReturn(List.of(equipe));

        // When & Then
        mockMvc.perform(get("/api/parametrage/equipes").param("terminalId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("EQ-1"));
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenValidEquipeAndParametrageRole_whenCreate_thenSavesAndReturnsEquipe() throws Exception {
        // Given
        Terminal terminal = new Terminal();
        terminal.setId(1L);

        Equipe input = new Equipe();
        input.setId("EQ-1");
        input.setNom("Equipe A");
        input.setTerminal(terminal);

        when(terminalRepository.findById(1L)).thenReturn(Optional.of(terminal));
        when(equipeRepository.save(any(Equipe.class))).thenReturn(input);

        // When & Then
        mockMvc.perform(post("/api/parametrage/equipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("EQ-1"))
                .andExpect(jsonPath("$.nom").value("Equipe A"));
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenEquipeWithNonExistingTerminal_whenCreate_thenReturnsBadRequest() throws Exception {
        // Given
        Terminal terminal = new Terminal();
        terminal.setId(999L);

        Equipe input = new Equipe();
        input.setId("EQ-1");
        input.setTerminal(terminal);

        when(terminalRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/parametrage/equipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Terminal introuvable"));
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenExistingEquipe_whenSetMembres_thenUpdatesAndReturnsMembres() throws Exception {
        // Given
        String equipeId = "EQ-1";
        Equipe equipe = new Equipe();
        equipe.setId(equipeId);

        Personnel personnel = new Personnel();
        personnel.setMatricule("MATR-100");
        personnel.setNom("Alice");

        when(equipeRepository.findById(equipeId)).thenReturn(Optional.of(equipe));
        when(personnelRepository.findByEquipeId(equipeId)).thenReturn(Collections.emptyList());
        when(personnelRepository.findAllById(List.of("MATR-100"))).thenReturn(List.of(personnel));
        when(personnelRepository.saveAll(anyList())).thenReturn(List.of(personnel));

        // When & Then
        mockMvc.perform(put("/api/parametrage/equipes/{id}/membres", equipeId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of("MATR-100"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matricule").value("MATR-100"))
                .andExpect(jsonPath("$[0].nom").value("Alice"));
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenNonExistingEquipe_whenSetMembres_thenReturnsBadRequest() throws Exception {
        // Given
        String equipeId = "NON-EXISTENT";
        when(equipeRepository.findById(equipeId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/parametrage/equipes/{id}/membres", equipeId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of("MATR-100"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Equipe introuvable : NON-EXISTENT"));
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenExistingEquipeId_whenDelete_thenDeletesAndReturnsNoContent() throws Exception {
        // Given
        String equipeId = "EQ-1";

        // When & Then
        mockMvc.perform(delete("/api/parametrage/equipes/{id}", equipeId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(equipeRepository, times(1)).deleteById(equipeId);
    }
}
