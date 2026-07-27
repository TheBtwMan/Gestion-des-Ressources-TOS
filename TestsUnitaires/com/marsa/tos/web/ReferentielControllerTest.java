package com.marsa.tos.web;

import com.marsa.tos.domain.referentiel.*;
import com.marsa.tos.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReferentielController.class)
class ReferentielControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PortRepository portRepository;

    @MockitoBean
    private TerminalRepository terminalRepository;

    @MockitoBean
    private FonctionRepository fonctionRepository;

    @MockitoBean
    private PersonnelRepository personnelRepository;

    @MockitoBean
    private EquipementFamilleRepository equipementFamilleRepository;

    @MockitoBean
    private EquipementRepository equipementRepository;

    @MockitoBean
    private AccessoireRepository accessoireRepository;

    @MockitoBean
    private TypeTraficRepository typeTraficRepository;

    @MockitoBean
    private TraficRepository traficRepository;

    @Test
    @WithMockUser
    void whenGetPorts_thenReturnsAllPorts() throws Exception {
        Port port = new Port();
        port.setId(1L);
        port.setNom("Port de Casablanca");
        when(portRepository.findAll()).thenReturn(List.of(port));

        mockMvc.perform(get("/api/referentiel/ports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nom").value("Port de Casablanca"));
    }

    @Test
    @WithMockUser
    void whenGetTerminaux_thenReturnsAllTerminaux() throws Exception {
        Terminal terminal = new Terminal();
        terminal.setId(1L);
        terminal.setNom("TC3PC");
        when(terminalRepository.findAll()).thenReturn(List.of(terminal));

        mockMvc.perform(get("/api/referentiel/terminaux"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("TC3PC"));
    }

    @Test
    @WithMockUser
    void whenGetFonctions_thenReturnsAllFonctions() throws Exception {
        Fonction fonction = new Fonction();
        fonction.setId(1L);
        fonction.setCode("GRUTIER");
        fonction.setLibelle("Grutier");
        when(fonctionRepository.findAll()).thenReturn(List.of(fonction));

        mockMvc.perform(get("/api/referentiel/fonctions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("GRUTIER"));
    }

    @Test
    @WithMockUser
    void whenGetPersonnel_thenReturnsAllPersonnel() throws Exception {
        Personnel p = new Personnel();
        p.setMatricule("MATR-001");
        p.setNom("Ali");
        when(personnelRepository.findAll()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/referentiel/personnel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matricule").value("MATR-001"));
    }

    @Test
    @WithMockUser
    void givenEquipeId_whenGetPersonnelParEquipe_thenReturnsFilteredPersonnel() throws Exception {
        Personnel p = new Personnel();
        p.setMatricule("MATR-002");
        when(personnelRepository.findByEquipeId("EQ-1")).thenReturn(List.of(p));

        mockMvc.perform(get("/api/referentiel/personnel/equipe/{equipeId}", "EQ-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matricule").value("MATR-002"));
    }

    @Test
    @WithMockUser
    void givenFonctionCode_whenGetPersonnelParFonction_thenReturnsFilteredPersonnel() throws Exception {
        Personnel p = new Personnel();
        p.setMatricule("MATR-003");
        when(personnelRepository.findByFonctionCode("GRUTIER")).thenReturn(List.of(p));

        mockMvc.perform(get("/api/referentiel/personnel/fonction/{fonctionCode}", "GRUTIER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matricule").value("MATR-003"));
    }

    @Test
    @WithMockUser
    void whenGetEquipementFamilles_thenReturnsAllFamilles() throws Exception {
        EquipementFamille famille = new EquipementFamille();
        famille.setId(1L);
        famille.setNom("Portique");
        when(equipementFamilleRepository.findAll()).thenReturn(List.of(famille));

        mockMvc.perform(get("/api/referentiel/equipement-familles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Portique"));
    }

    @Test
    @WithMockUser
    void whenGetEquipements_thenReturnsAllEquipements() throws Exception {
        Equipement eq = new Equipement();
        eq.setCode("GRU-01");
        eq.setDisponible(true);
        when(equipementRepository.findAll()).thenReturn(List.of(eq));

        mockMvc.perform(get("/api/referentiel/equipements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("GRU-01"));
    }

    @Test
    @WithMockUser
    void givenFamilleId_whenGetEquipementsParFamille_thenReturnsFiltered() throws Exception {
        Equipement eq = new Equipement();
        eq.setCode("GRU-02");
        when(equipementRepository.findByFamilleId(1L)).thenReturn(List.of(eq));

        mockMvc.perform(get("/api/referentiel/equipements/famille/{familleId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("GRU-02"));
    }

    @Test
    @WithMockUser
    void whenGetAccessoires_thenReturnsAllAccessoires() throws Exception {
        Accessoire acc = new Accessoire();
        acc.setId(1L);
        acc.setNom("Palonnier 50T");
        when(accessoireRepository.findAll()).thenReturn(List.of(acc));

        mockMvc.perform(get("/api/referentiel/accessoires"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Palonnier 50T"));
    }

    @Test
    @WithMockUser
    void whenGetTypeTrafics_thenReturnsAllTypeTrafics() throws Exception {
        TypeTrafic type = new TypeTrafic();
        type.setId(1L);
        type.setNom("Vrac");
        when(typeTraficRepository.findAll()).thenReturn(List.of(type));

        mockMvc.perform(get("/api/referentiel/type-trafics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Vrac"));
    }

    @Test
    @WithMockUser
    void whenGetTrafics_thenReturnsAllTrafics() throws Exception {
        Trafic trafic = new Trafic();
        trafic.setId(1L);
        trafic.setCode("CONTENEURS");
        trafic.setNom("Conteneurs");
        when(traficRepository.findAll()).thenReturn(List.of(trafic));

        mockMvc.perform(get("/api/referentiel/trafics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CONTENEURS"));
    }
}
