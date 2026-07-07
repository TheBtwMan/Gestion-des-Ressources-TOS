package com.marsa.tos.web;

import com.marsa.tos.repository.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Données référentielles : Port, Terminal, Fonction, Personnel (HR Access), Equipement/Famille et
 * Accessoire (APIPRO), TypeTrafic/Trafic. Alimentées par le DataSeeder à partir du mock TOS.
 */
@RestController
@RequestMapping("/api/referentiel")
@RequiredArgsConstructor
public class ReferentielController {

    private final PortRepository portRepository;
    private final TerminalRepository terminalRepository;
    private final FonctionRepository fonctionRepository;
    private final PersonnelRepository personnelRepository;
    private final EquipementFamilleRepository equipementFamilleRepository;
    private final EquipementRepository equipementRepository;
    private final AccessoireRepository accessoireRepository;
    private final TypeTraficRepository typeTraficRepository;
    private final TraficRepository traficRepository;

    @GetMapping("/ports")
    public List<?> ports() {
        return portRepository.findAll();
    }

    @GetMapping("/terminaux")
    public List<?> terminaux() {
        return terminalRepository.findAll();
    }

    @GetMapping("/fonctions")
    public List<?> fonctions() {
        return fonctionRepository.findAll();
    }

    @GetMapping("/personnel")
    public List<?> personnel() {
        return personnelRepository.findAll();
    }

    @GetMapping("/personnel/equipe/{equipeId}")
    public List<?> personnelParEquipe(@PathVariable String equipeId) {
        return personnelRepository.findByEquipeId(equipeId);
    }

    @GetMapping("/personnel/fonction/{fonctionCode}")
    public List<?> personnelParFonction(@PathVariable String fonctionCode) {
        return personnelRepository.findByFonctionCode(fonctionCode);
    }

    @GetMapping("/equipement-familles")
    public List<?> equipementFamilles() {
        return equipementFamilleRepository.findAll();
    }

    @GetMapping("/equipements")
    public List<?> equipements() {
        return equipementRepository.findAll();
    }

    @GetMapping("/equipements/famille/{familleId}")
    public List<?> equipementsParFamille(@PathVariable Long familleId) {
        return equipementRepository.findByFamilleId(familleId);
    }

    @GetMapping("/accessoires")
    public List<?> accessoires() {
        return accessoireRepository.findAll();
    }

    @GetMapping("/type-trafics")
    public List<?> typeTrafics() {
        return typeTraficRepository.findAll();
    }

    @GetMapping("/trafics")
    public List<?> trafics() {
        return traficRepository.findAll();
    }
}
