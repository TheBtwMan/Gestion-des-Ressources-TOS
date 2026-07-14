package com.marsa.tos.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marsa.tos.domain.parametrage.MainTheorique;
import com.marsa.tos.domain.referentiel.Terminal;
import com.marsa.tos.domain.referentiel.Trafic;
import com.marsa.tos.repository.AccessoireRepository;
import com.marsa.tos.repository.MainTheoriqueRepository;
import com.marsa.tos.repository.TerminalRepository;
import com.marsa.tos.repository.TraficRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MainTheoriqueController.class)
class MainTheoriqueControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MainTheoriqueRepository mainTheoriqueRepository;

    @MockitoBean
    private TraficRepository traficRepository;

    @MockitoBean
    private TerminalRepository terminalRepository;

    @MockitoBean
    private AccessoireRepository accessoireRepository;

    @Test
    @WithMockUser
    void givenNoFilters_whenAll_thenReturnsAllMains() throws Exception {
        // Given
        MainTheorique main = new MainTheorique();
        main.setId(1L);
        main.setNom("Main A");
        when(mainTheoriqueRepository.findAll()).thenReturn(List.of(main));

        // When & Then
        mockMvc.perform(get("/api/parametrage/mains-theoriques"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nom").value("Main A"));
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenValidMainAndParametrageRole_whenCreate_thenSavesAndReturnsMain() throws Exception {
        // Given
        Trafic trafic = new Trafic();
        trafic.setId(10L);
        Terminal terminal = new Terminal();
        terminal.setId(20L);

        MainTheorique input = new MainTheorique();
        input.setNom("Main Container");
        input.setTrafic(trafic);
        input.setTerminal(terminal);
        input.setRessourcesHumaines(new ArrayList<>());
        input.setRessourcesMaterielles(new ArrayList<>());

        MainTheorique saved = new MainTheorique();
        saved.setId(100L);
        saved.setNom("Main Container");
        saved.setTrafic(trafic);
        saved.setTerminal(terminal);

        when(traficRepository.findById(10L)).thenReturn(Optional.of(trafic));
        when(terminalRepository.findById(20L)).thenReturn(Optional.of(terminal));
        when(mainTheoriqueRepository.save(any(MainTheorique.class))).thenReturn(saved);

        // When & Then
        mockMvc.perform(post("/api/parametrage/mains-theoriques")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.nom").value("Main Container"));
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenMainWithNonExistingTrafic_whenCreate_thenReturnsBadRequest() throws Exception {
        // Given
        Trafic trafic = new Trafic();
        trafic.setId(999L);

        MainTheorique input = new MainTheorique();
        input.setTrafic(trafic);

        when(traficRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/parametrage/mains-theoriques")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Trafic introuvable"));
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenExistingMainId_whenDelete_thenDeletesAndReturnsNoContent() throws Exception {
        // Given
        Long mainId = 1L;

        // When & Then
        mockMvc.perform(delete("/api/parametrage/mains-theoriques/{id}", mainId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(mainTheoriqueRepository, times(1)).deleteById(mainId);
    }
}
