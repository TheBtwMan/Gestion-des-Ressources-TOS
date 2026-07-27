package com.marsa.tos.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marsa.tos.common.Enums.Sens;
import com.marsa.tos.common.Enums.StatutCommande;
import com.marsa.tos.domain.exploitation.Commande;
import com.marsa.tos.domain.exploitation.Escale;
import com.marsa.tos.domain.referentiel.Trafic;
import com.marsa.tos.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommandeController.class)
class CommandeControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommandeRepository commandeRepository;

    @MockitoBean
    private EscaleRepository escaleRepository;

    @MockitoBean
    private TraficRepository traficRepository;

    @MockitoBean
    private MainTheoriqueRepository mainTheoriqueRepository;

    @MockitoBean
    private AffectationPrevisionnelleRepository affectationPrevisionnelleRepository;

    @MockitoBean
    private AffectationReelleRepository affectationReelleRepository;

    @Test
    @WithMockUser
    void givenNoFilters_whenAll_thenReturnsAllCommandes() throws Exception {
        Commande cmd = new Commande();
        cmd.setNumero("CMD-100");
        cmd.setSens(Sens.IMPORT);
        cmd.setStatut(StatutCommande.CREEE);
        when(commandeRepository.findAll()).thenReturn(List.of(cmd));

        mockMvc.perform(get("/api/exploitation/commandes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value("CMD-100"))
                .andExpect(jsonPath("$[0].statut").value("CREEE"));
    }

    @Test
    @WithMockUser
    void givenSansEscaleFilter_whenAll_thenReturnsUnlinkedCommandes() throws Exception {
        Commande cmd = new Commande();
        cmd.setNumero("CMD-UNLINKED");
        when(commandeRepository.findByEscaleIsNull()).thenReturn(List.of(cmd));

        mockMvc.perform(get("/api/exploitation/commandes").param("sansEscale", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value("CMD-UNLINKED"));
    }

    @Test
    @WithMockUser
    void givenEscaleIdFilter_whenAll_thenReturnsEscaleCommandes() throws Exception {
        Commande cmd = new Commande();
        cmd.setNumero("CMD-ESC");
        when(commandeRepository.findByEscaleId("ESC-1")).thenReturn(List.of(cmd));

        mockMvc.perform(get("/api/exploitation/commandes").param("escaleId", "ESC-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value("CMD-ESC"));
    }

    @Test
    @WithMockUser
    void givenExistingNumero_whenGet_thenReturnsCommande() throws Exception {
        Commande cmd = new Commande();
        cmd.setNumero("CMD-100");
        when(commandeRepository.findById("CMD-100")).thenReturn(Optional.of(cmd));

        mockMvc.perform(get("/api/exploitation/commandes/{numero}", "CMD-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("CMD-100"));
    }

    @Test
    @WithMockUser
    void givenNonExistingNumero_whenGet_thenReturnsNotFound() throws Exception {
        when(commandeRepository.findById("CMD-MISSING")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/exploitation/commandes/{numero}", "CMD-MISSING"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void givenExistingNumero_whenValidationView_thenReturnsCommandeAndAffectations() throws Exception {
        Commande cmd = new Commande();
        cmd.setNumero("CMD-100");
        when(commandeRepository.findById("CMD-100")).thenReturn(Optional.of(cmd));
        when(affectationPrevisionnelleRepository.findByCommandeNumero("CMD-100")).thenReturn(Collections.emptyList());
        when(affectationReelleRepository.findByCommandeNumero("CMD-100")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/exploitation/commandes/{numero}/validation", "CMD-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commande.numero").value("CMD-100"))
                .andExpect(jsonPath("$.previsionnelles").isArray())
                .andExpect(jsonPath("$.reelles").isArray());
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_PREVISIONNELLE")
    void givenValidDataAndRolePrevis_whenCreate_thenSavesAndReturnsCommande() throws Exception {
        Trafic trafic = new Trafic();
        trafic.setId(10L);

        Commande input = new Commande();
        input.setNumero("CMD-101");
        input.setTrafic(trafic);
        input.setSens(Sens.IMPORT);
        input.setDateTravail(LocalDate.now());
        input.setTonnagePrevu(500);

        Commande saved = new Commande();
        saved.setNumero("CMD-101");
        saved.setTrafic(trafic);
        saved.setSens(Sens.IMPORT);
        saved.setDateTravail(LocalDate.now());
        saved.setTonnagePrevu(500);
        saved.setStatut(StatutCommande.CREEE);

        when(traficRepository.findById(10L)).thenReturn(Optional.of(trafic));
        when(commandeRepository.save(any(Commande.class))).thenReturn(saved);

        mockMvc.perform(post("/api/exploitation/commandes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("CMD-101"))
                .andExpect(jsonPath("$.statut").value("CREEE"));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_PREVISIONNELLE")
    void givenValidPrevisRole_whenLierEscale_thenLinksAndReturnsCommande() throws Exception {
        String cmdNo = "CMD-100";
        String escaleId = "ESC-500";
        Commande cmd = new Commande();
        cmd.setNumero(cmdNo);
        Escale escale = new Escale();
        escale.setId(escaleId);

        Commande saved = new Commande();
        saved.setNumero(cmdNo);
        saved.setEscale(escale);
        saved.setStatut(StatutCommande.LIEE_ESCALE);

        when(commandeRepository.findById(cmdNo)).thenReturn(Optional.of(cmd));
        when(escaleRepository.findById(escaleId)).thenReturn(Optional.of(escale));
        when(commandeRepository.save(any(Commande.class))).thenReturn(saved);

        mockMvc.perform(post("/api/exploitation/commandes/{numero}/lier-escale/{escaleId}", cmdNo, escaleId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(cmdNo))
                .andExpect(jsonPath("$.statut").value("LIEE_ESCALE"));
    }

    @Test
    @WithMockUser(roles = "VALIDATION")
    void givenValidValidationRole_whenLierEscale_thenLinksAndReturnsCommande() throws Exception {
        String cmdNo = "CMD-100";
        String escaleId = "ESC-500";
        Commande cmd = new Commande();
        cmd.setNumero(cmdNo);
        Escale escale = new Escale();
        escale.setId(escaleId);

        Commande saved = new Commande();
        saved.setNumero(cmdNo);
        saved.setEscale(escale);
        saved.setStatut(StatutCommande.LIEE_ESCALE);

        when(commandeRepository.findById(cmdNo)).thenReturn(Optional.of(cmd));
        when(escaleRepository.findById(escaleId)).thenReturn(Optional.of(escale));
        when(commandeRepository.save(any(Commande.class))).thenReturn(saved);

        mockMvc.perform(post("/api/exploitation/commandes/{numero}/lier-escale/{escaleId}", cmdNo, escaleId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(cmdNo))
                .andExpect(jsonPath("$.statut").value("LIEE_ESCALE"));
    }

    @Test
    @WithMockUser(roles = "VALIDATION")
    void givenExistingCommandeAndValidationRole_whenValider_thenUpdatesStatutToValidee() throws Exception {
        String cmdNo = "CMD-100";
        Commande cmd = new Commande();
        cmd.setNumero(cmdNo);

        Commande saved = new Commande();
        saved.setNumero(cmdNo);
        saved.setStatut(StatutCommande.VALIDEE);

        when(commandeRepository.findById(cmdNo)).thenReturn(Optional.of(cmd));
        when(commandeRepository.save(any(Commande.class))).thenReturn(saved);

        mockMvc.perform(post("/api/exploitation/commandes/{numero}/valider", cmdNo)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(cmdNo))
                .andExpect(jsonPath("$.statut").value("VALIDEE"));
    }

    @Test
    @WithMockUser(roles = "AFFECTATION_PREVISIONNELLE")
    void givenForbiddenRole_whenValider_thenReturnsForbidden() throws Exception {
        String cmdNo = "CMD-100";

        mockMvc.perform(post("/api/exploitation/commandes/{numero}/valider", cmdNo)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
