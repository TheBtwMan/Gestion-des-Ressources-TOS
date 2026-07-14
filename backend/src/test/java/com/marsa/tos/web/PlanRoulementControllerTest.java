package com.marsa.tos.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marsa.tos.common.Enums.Shift;
import com.marsa.tos.common.Enums.TypeRoulement;
import com.marsa.tos.domain.parametrage.Equipe;
import com.marsa.tos.domain.parametrage.PlanRoulement;
import com.marsa.tos.repository.EquipeRepository;
import com.marsa.tos.repository.PlanRoulementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanRoulementController.class)
class PlanRoulementControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlanRoulementRepository planRoulementRepository;

    @MockitoBean
    private EquipeRepository equipeRepository;

    @Test
    @WithMockUser
    void givenNoFilters_whenAll_thenReturnsAllPlans() throws Exception {
        // Given
        PlanRoulement plan = new PlanRoulement();
        plan.setId(1L);
        plan.setTypeRoulement(TypeRoulement.SEMAINE);
        when(planRoulementRepository.findAll()).thenReturn(List.of(plan));

        // When & Then
        mockMvc.perform(get("/api/parametrage/plan-roulement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].typeRoulement").value("SEMAINE"));
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenValidPlanAndParametrageRole_whenCreate_thenSavesAndReturnsPlan() throws Exception {
        // Given
        Equipe equipe = new Equipe();
        equipe.setId("EQ-1");

        PlanRoulement input = new PlanRoulement();
        input.setEquipe(equipe);
        input.setTypeRoulement(TypeRoulement.SEMAINE);
        input.setDateDebut(LocalDate.now());
        input.setDateFin(LocalDate.now().plusDays(7));
        input.setShift(Shift.SHIFT_1);

        PlanRoulement saved = new PlanRoulement();
        saved.setId(100L);
        saved.setEquipe(equipe);
        saved.setTypeRoulement(TypeRoulement.SEMAINE);
        saved.setDateDebut(LocalDate.now());
        saved.setDateFin(LocalDate.now().plusDays(7));
        saved.setShift(Shift.SHIFT_1);

        when(equipeRepository.findById("EQ-1")).thenReturn(Optional.of(equipe));
        when(planRoulementRepository.save(any(PlanRoulement.class))).thenReturn(saved);

        // When & Then
        mockMvc.perform(post("/api/parametrage/plan-roulement")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.shift").value("SHIFT_1"));
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenPlanWithNonExistingEquipe_whenCreate_thenReturnsBadRequest() throws Exception {
        // Given
        Equipe equipe = new Equipe();
        equipe.setId("NON-EXISTENT");

        PlanRoulement input = new PlanRoulement();
        input.setEquipe(equipe);

        when(equipeRepository.findById("NON-EXISTENT")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/parametrage/plan-roulement")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Equipe introuvable"));
    }

    @Test
    @WithMockUser(roles = "PARAMETRAGE")
    void givenExistingPlanId_whenDelete_thenDeletesAndReturnsNoContent() throws Exception {
        // Given
        Long planId = 1L;

        // When & Then
        mockMvc.perform(delete("/api/parametrage/plan-roulement/{id}", planId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(planRoulementRepository, times(1)).deleteById(planId);
    }
}
