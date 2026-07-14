package com.marsa.tos.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marsa.tos.common.Enums.Sens;
import com.marsa.tos.domain.parametrage.MainTheorique;
import com.marsa.tos.domain.parametrage.NormeProductivite;
import com.marsa.tos.domain.referentiel.Trafic;
import com.marsa.tos.repository.MainTheoriqueRepository;
import com.marsa.tos.repository.NormeProductiviteRepository;
import com.marsa.tos.repository.TraficRepository;
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

@WebMvcTest(NormeProductiviteController.class)
class NormeProductiviteControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NormeProductiviteRepository normeProductiviteRepository;

    @MockitoBean
    private TraficRepository traficRepository;

    @MockitoBean
    private MainTheoriqueRepository mainTheoriqueRepository;

    @Test
    @WithMockUser
    void givenNoFilters_whenAll_thenReturnsAllNormes() throws Exception {
        // Given
        NormeProductivite norme = new NormeProductivite();
        norme.setId(1L);
        norme.setNorme(1500);
        when(normeProductiviteRepository.findAll()).thenReturn(List.of(norme));

        // When & Then
        mockMvc.perform(get("/api/parametrage/normes-productivite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].norme").value(1500));
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenValidNormeAndParametrageRole_whenCreate_thenSavesAndReturnsNorme() throws Exception {
        // Given
        Trafic trafic = new Trafic();
        trafic.setId(10L);
        MainTheorique main = new MainTheorique();
        main.setId(20L);

        NormeProductivite input = new NormeProductivite();
        input.setTrafic(trafic);
        input.setMainTheorique(main);
        input.setMode("T/Shift");
        input.setNorme(1200);
        input.setSens(Sens.IMPORT);

        NormeProductivite saved = new NormeProductivite();
        saved.setId(100L);
        saved.setTrafic(trafic);
        saved.setMainTheorique(main);
        saved.setMode("T/Shift");
        saved.setNorme(1200);
        saved.setSens(Sens.IMPORT);

        when(traficRepository.findById(10L)).thenReturn(Optional.of(trafic));
        when(mainTheoriqueRepository.findById(20L)).thenReturn(Optional.of(main));
        when(normeProductiviteRepository.save(any(NormeProductivite.class))).thenReturn(saved);

        // When & Then
        mockMvc.perform(post("/api/parametrage/normes-productivite")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.norme").value(1200))
                .andExpect(jsonPath("$.sens").value("IMPORT"));
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenNormeWithNonExistingTrafic_whenCreate_thenReturnsBadRequest() throws Exception {
        // Given
        Trafic trafic = new Trafic();
        trafic.setId(999L);
        MainTheorique main = new MainTheorique();
        main.setId(20L);

        NormeProductivite input = new NormeProductivite();
        input.setTrafic(trafic);
        input.setMainTheorique(main);

        when(traficRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/parametrage/normes-productivite")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Trafic introuvable"));
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenExistingNormeId_whenDelete_thenDeletesAndReturnsNoContent() throws Exception {
        // Given
        Long id = 1L;

        // When & Then
        mockMvc.perform(delete("/api/parametrage/normes-productivite/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(normeProductiviteRepository, times(1)).deleteById(id);
    }
}
