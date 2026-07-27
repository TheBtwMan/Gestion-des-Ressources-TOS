package com.marsa.tos.web;

import com.marsa.tos.common.Enums.Sens;
import com.marsa.tos.common.Enums.StatutCommande;
import com.marsa.tos.common.Enums.StatutEscale;
import com.marsa.tos.domain.exploitation.*;
import com.marsa.tos.domain.referentiel.*;
import com.marsa.tos.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EscaleRepository escaleRepository;

    @MockitoBean
    private CommandeRepository commandeRepository;

    @MockitoBean
    private ArretRepository arretRepository;

    @MockitoBean
    private AbsenceRepository absenceRepository;

    @MockitoBean
    private PersonnelRepository personnelRepository;

    // ---------------------------------------------------------------- /kpis

    @Test
    @WithMockUser
    void givenNoFilters_whenGetKpis_thenReturnsAllKpis() throws Exception {
        // Given
        Trafic trafic = new Trafic();
        trafic.setId(1L);
        trafic.setNom("Conteneurs");

        Commande cmd = new Commande();
        cmd.setNumero("CMD-1");
        cmd.setTonnagePrevu(1000);
        cmd.setTonnageRealiseParShift(true);
        cmd.setDateTravail(LocalDate.of(2026, 7, 10));
        cmd.setTrafic(trafic);
        cmd.setSens(Sens.IMPORT);
        cmd.setStatut(StatutCommande.CREEE);
        CommandeShiftTonnage shift = new CommandeShiftTonnage();
        shift.setShiftId("SH-1");
        shift.setTonnageRealise(800);
        cmd.setShifts(List.of(shift));

        Escale escale = new Escale();
        escale.setId("ESC-1");
        escale.setStatut(StatutEscale.EN_COURS);
        escale.setDateArriveePrevue(LocalDateTime.of(2026, 7, 10, 8, 0));

        Arret arret = new Arret();
        arret.setId("ARR-1");
        arret.setDureeMinutes(60);
        arret.setDateDebut(LocalDateTime.of(2026, 7, 10, 10, 0));

        Personnel personnel = new Personnel();
        personnel.setMatricule("P-1");

        Absence absence = new Absence();
        absence.setId("ABS-1");
        absence.setPersonnel(personnel);
        absence.setDateDebut(LocalDateTime.of(2026, 7, 10, 6, 0));

        when(escaleRepository.findAll()).thenReturn(List.of(escale));
        when(commandeRepository.findAll()).thenReturn(List.of(cmd));
        when(arretRepository.findAll()).thenReturn(List.of(arret));
        when(absenceRepository.findAll()).thenReturn(List.of(absence));
        when(personnelRepository.count()).thenReturn(40L);

        // When & Then
        mockMvc.perform(get("/api/dashboard/kpis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tonnagePrevu").value(1000))
                .andExpect(jsonPath("$.tonnageRealise").value(800))
                .andExpect(jsonPath("$.nbEscales").value(1))
                .andExpect(jsonPath("$.nbCommandes").value(1))
                .andExpect(jsonPath("$.nbArrets").value(1))
                .andExpect(jsonPath("$.dureeArretTotaleMinutes").value(60))
                .andExpect(jsonPath("$.nbAbsences").value(1))
                .andExpect(jsonPath("$.effectifTotal").value(40));
    }

    @Test
    @WithMockUser
    void givenEmptyData_whenGetKpis_thenReturnsZeroValues() throws Exception {
        // Given
        when(escaleRepository.findAll()).thenReturn(Collections.emptyList());
        when(commandeRepository.findAll()).thenReturn(Collections.emptyList());
        when(arretRepository.findAll()).thenReturn(Collections.emptyList());
        when(absenceRepository.findAll()).thenReturn(Collections.emptyList());
        when(personnelRepository.count()).thenReturn(0L);

        // When & Then
        mockMvc.perform(get("/api/dashboard/kpis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tonnagePrevu").value(0))
                .andExpect(jsonPath("$.tonnageRealise").value(0))
                .andExpect(jsonPath("$.ecartPourcent").value(0))
                .andExpect(jsonPath("$.nbEscales").value(0))
                .andExpect(jsonPath("$.nbCommandes").value(0))
                .andExpect(jsonPath("$.tauxAbsenteismePourcent").value(0));
    }

    @Test
    @WithMockUser
    void givenDateFilters_whenGetKpis_thenFiltersCorrectly() throws Exception {
        // Given
        when(escaleRepository.findAll()).thenReturn(Collections.emptyList());
        when(commandeRepository.findAll()).thenReturn(Collections.emptyList());
        when(arretRepository.findAll()).thenReturn(Collections.emptyList());
        when(absenceRepository.findAll()).thenReturn(Collections.emptyList());
        when(personnelRepository.count()).thenReturn(10L);

        // When & Then
        mockMvc.perform(get("/api/dashboard/kpis")
                        .param("dateDebut", "2026-07-01")
                        .param("dateFin", "2026-07-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nbEscales").value(0));
    }

    // ---------------------------------------------------------------- /tonnage-par-trafic

    @Test
    @WithMockUser
    void givenCommandes_whenGetTonnageParTrafic_thenReturnsGroupedData() throws Exception {
        // Given
        Trafic trafic = new Trafic();
        trafic.setId(1L);
        trafic.setNom("Vrac solide");

        Commande cmd = new Commande();
        cmd.setNumero("CMD-1");
        cmd.setTonnagePrevu(3000);
        cmd.setTonnageRealiseParShift(false);
        cmd.setDateTravail(LocalDate.of(2026, 7, 10));
        cmd.setTrafic(trafic);
        cmd.setSens(Sens.IMPORT);
        cmd.setShifts(Collections.emptyList());

        when(commandeRepository.findAll()).thenReturn(List.of(cmd));

        // When & Then
        mockMvc.perform(get("/api/dashboard/tonnage-par-trafic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trafic").value("Vrac solide"))
                .andExpect(jsonPath("$[0].tonnagePrevu").value(3000))
                .andExpect(jsonPath("$[0].tonnageRealise").value(0));
    }

    // ---------------------------------------------------------------- /absenteisme-par-equipe

    @Test
    @WithMockUser
    void givenPersonnelWithAbsences_whenGetAbsenteismeParEquipe_thenReturnsGroupedData() throws Exception {
        // Given
        Terminal terminal = new Terminal();
        terminal.setId(1L);

        com.marsa.tos.domain.parametrage.Equipe equipe = new com.marsa.tos.domain.parametrage.Equipe();
        equipe.setId("EQ-A");
        equipe.setTerminal(terminal);

        Personnel p = new Personnel();
        p.setMatricule("P-1");
        p.setEquipe(equipe);

        Absence abs = new Absence();
        abs.setId("ABS-1");
        abs.setPersonnel(p);
        abs.setDateDebut(LocalDateTime.of(2026, 7, 10, 6, 0));

        when(personnelRepository.findAll()).thenReturn(List.of(p));
        when(absenceRepository.findAll()).thenReturn(List.of(abs));

        // When & Then
        mockMvc.perform(get("/api/dashboard/absenteisme-par-equipe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].equipeId").value("EQ-A"))
                .andExpect(jsonPath("$[0].effectif").value(1))
                .andExpect(jsonPath("$[0].nbAbsences").value(1));
    }

    // ---------------------------------------------------------------- /arrets-par-equipement

    @Test
    @WithMockUser
    void givenArretsWithEquipement_whenGetArretsParEquipement_thenReturnsGroupedAndSorted() throws Exception {
        // Given
        EquipementFamille famille = new EquipementFamille();
        famille.setId(1L);
        famille.setNom("Portique");

        Equipement eq = new Equipement();
        eq.setCode("PORT-01");
        eq.setFamille(famille);

        Arret arret = new Arret();
        arret.setId("ARR-1");
        arret.setEquipement(eq);
        arret.setDureeMinutes(120);
        arret.setDateDebut(LocalDateTime.of(2026, 7, 10, 10, 0));

        when(arretRepository.findAll()).thenReturn(List.of(arret));

        // When & Then
        mockMvc.perform(get("/api/dashboard/arrets-par-equipement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].equipementCode").value("PORT-01"))
                .andExpect(jsonPath("$[0].famille").value("Portique"))
                .andExpect(jsonPath("$[0].nbArrets").value(1))
                .andExpect(jsonPath("$[0].dureeTotaleMinutes").value(120));
    }

    // ---------------------------------------------------------------- /escales-en-cours

    @Test
    @WithMockUser
    void whenGetEscalesEnCours_thenReturnsOnlyEnCoursEscales() throws Exception {
        // Given
        Escale escale = new Escale();
        escale.setId("ESC-1");
        escale.setNavire("Navire Test");
        escale.setStatut(StatutEscale.EN_COURS);
        when(escaleRepository.findByStatut(StatutEscale.EN_COURS)).thenReturn(List.of(escale));

        // When & Then
        mockMvc.perform(get("/api/dashboard/escales-en-cours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("ESC-1"))
                .andExpect(jsonPath("$[0].navire").value("Navire Test"));
    }

    // ---------------------------------------------------------------- /export CSV

    @Test
    @WithMockUser
    void givenCommandes_whenExportCommandesCsv_thenReturnsCsvFile() throws Exception {
        // Given
        Trafic trafic = new Trafic();
        trafic.setId(1L);
        trafic.setNom("Conteneurs");

        Commande cmd = new Commande();
        cmd.setNumero("CMD-CSV");
        cmd.setClient("Client A");
        cmd.setTrafic(trafic);
        cmd.setSens(Sens.IMPORT);
        cmd.setStatut(StatutCommande.CREEE);
        cmd.setDateTravail(LocalDate.of(2026, 7, 10));
        cmd.setTonnagePrevu(500);
        cmd.setTonnageRealiseParShift(false);
        cmd.setShifts(Collections.emptyList());

        when(commandeRepository.findAll()).thenReturn(List.of(cmd));

        // When & Then
        mockMvc.perform(get("/api/dashboard/export/commandes.csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("commandes.csv")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }

    @Test
    @WithMockUser
    void givenData_whenExportKpisCsv_thenReturnsCsvFile() throws Exception {
        // Given
        when(escaleRepository.findAll()).thenReturn(Collections.emptyList());
        when(commandeRepository.findAll()).thenReturn(Collections.emptyList());
        when(arretRepository.findAll()).thenReturn(Collections.emptyList());
        when(absenceRepository.findAll()).thenReturn(Collections.emptyList());
        when(personnelRepository.count()).thenReturn(0L);

        // When & Then
        mockMvc.perform(get("/api/dashboard/export/kpis.csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("dashboard-kpis.csv")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }
}
