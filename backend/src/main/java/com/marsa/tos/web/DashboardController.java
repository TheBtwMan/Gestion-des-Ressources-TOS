package com.marsa.tos.web;

import com.marsa.tos.common.Enums.StatutEscale;
import com.marsa.tos.domain.exploitation.Absence;
import com.marsa.tos.domain.exploitation.Arret;
import com.marsa.tos.domain.exploitation.Commande;
import com.marsa.tos.domain.exploitation.CommandeShiftTonnage;
import com.marsa.tos.domain.exploitation.Escale;
import com.marsa.tos.domain.referentiel.Personnel;
import com.marsa.tos.repository.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reporting / dashboard de suivi (objectif final du cadrage de stage) : écarts prévu/réalisé,
 * taux d'absentéisme, arrêts, avancement des escales. Alimenté par les données TOS (mock).
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final EscaleRepository escaleRepository;
    private final CommandeRepository commandeRepository;
    private final ArretRepository arretRepository;
    private final AbsenceRepository absenceRepository;
    private final PersonnelRepository personnelRepository;

    @GetMapping("/kpis")
    public Map<String, Object> kpis() {
        List<Escale> escales = escaleRepository.findAll();
        List<Commande> commandes = commandeRepository.findAll();
        List<Arret> arrets = arretRepository.findAll();
        List<Absence> absences = absenceRepository.findAll();
        long effectifTotal = personnelRepository.count();

        long tonnagePrevu = commandes.stream().mapToLong(c -> nz(c.getTonnagePrevu())).sum();
        long tonnageRealise = commandes.stream().mapToLong(this::tonnageRealiseCommande).sum();
        double ecartPourcent = tonnagePrevu == 0 ? 0 : ((tonnageRealise - tonnagePrevu) * 100.0) / tonnagePrevu;

        Map<StatutEscale, Long> escalesParStatut = escales.stream()
                .collect(Collectors.groupingBy(Escale::getStatut, Collectors.counting()));

        long dureeArretTotaleMinutes = arrets.stream()
                .mapToLong(a -> a.getDureeMinutes() != null ? a.getDureeMinutes() : 0)
                .sum();
        long arretsEnCours = arrets.stream().filter(a -> a.getDateFin() == null).count();

        // Absentéisme approximatif : nb de shifts d'absence déclarés / (effectif * nb de jours de la période mock).
        int joursPeriode = 14;
        double tauxAbsenteisme = effectifTotal == 0 ? 0
                : (absences.size() * 100.0) / (effectifTotal * joursPeriode);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tonnagePrevu", tonnagePrevu);
        result.put("tonnageRealise", tonnageRealise);
        result.put("ecartPourcent", Math.round(ecartPourcent * 10) / 10.0);
        result.put("nbEscales", escales.size());
        result.put("escalesParStatut", escalesParStatut);
        result.put("nbCommandes", commandes.size());
        result.put("nbArrets", arrets.size());
        result.put("arretsEnCours", arretsEnCours);
        result.put("dureeArretTotaleMinutes", dureeArretTotaleMinutes);
        result.put("nbAbsences", absences.size());
        result.put("effectifTotal", effectifTotal);
        result.put("tauxAbsenteismePourcent", Math.round(tauxAbsenteisme * 10) / 10.0);
        return result;
    }

    @GetMapping("/tonnage-par-trafic")
    public List<Map<String, Object>> tonnageParTrafic() {
        List<Commande> commandes = commandeRepository.findAll();
        Map<String, List<Commande>> parTrafic = commandes.stream()
                .collect(Collectors.groupingBy(c -> c.getTrafic().getNom()));

        return parTrafic.entrySet().stream().map(e -> {
            long prevu = e.getValue().stream().mapToLong(c -> nz(c.getTonnagePrevu())).sum();
            long realise = e.getValue().stream().mapToLong(this::tonnageRealiseCommande).sum();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("trafic", e.getKey());
            row.put("tonnagePrevu", prevu);
            row.put("tonnageRealise", realise);
            row.put("ecartPourcent", prevu == 0 ? 0 : Math.round(((realise - prevu) * 1000.0) / prevu) / 10.0);
            return row;
        }).collect(Collectors.toList());
    }

    @GetMapping("/absenteisme-par-equipe")
    public List<Map<String, Object>> absenteismeParEquipe() {
        List<Personnel> personnel = personnelRepository.findAll();
        List<Absence> absences = absenceRepository.findAll();
        Map<String, Long> absencesParMatricule = absences.stream()
                .collect(Collectors.groupingBy(a -> a.getPersonnel().getMatricule(), Collectors.counting()));

        Map<String, List<Personnel>> parEquipe = personnel.stream()
                .filter(p -> p.getEquipe() != null)
                .collect(Collectors.groupingBy(p -> p.getEquipe().getId()));

        return parEquipe.entrySet().stream().map(e -> {
            long nbAbsences = e.getValue().stream()
                    .mapToLong(p -> absencesParMatricule.getOrDefault(p.getMatricule(), 0L))
                    .sum();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("equipeId", e.getKey());
            row.put("effectif", e.getValue().size());
            row.put("nbAbsences", nbAbsences);
            return row;
        }).collect(Collectors.toList());
    }

    @GetMapping("/arrets-par-equipement")
    public List<Map<String, Object>> arretsParEquipement() {
        List<Arret> arrets = arretRepository.findAll();
        Map<String, List<Arret>> parEquipement = arrets.stream()
                .filter(a -> a.getEquipement() != null)
                .collect(Collectors.groupingBy(a -> a.getEquipement().getCode()));

        return parEquipement.entrySet().stream().map(e -> {
            long dureeTotale = e.getValue().stream()
                    .mapToLong(a -> a.getDureeMinutes() != null ? a.getDureeMinutes() : 0)
                    .sum();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("equipementCode", e.getKey());
            row.put("famille", e.getValue().get(0).getEquipement().getFamille().getNom());
            row.put("nbArrets", e.getValue().size());
            row.put("dureeTotaleMinutes", dureeTotale);
            return row;
        }).sorted((a, b) -> Long.compare((long) b.get("dureeTotaleMinutes"), (long) a.get("dureeTotaleMinutes")))
                .collect(Collectors.toList());
    }

    @GetMapping("/escales-en-cours")
    public List<Escale> escalesEnCours() {
        return escaleRepository.findByStatut(StatutEscale.EN_COURS);
    }

    private long tonnageRealiseCommande(Commande c) {
        if (c.isTonnageRealiseParShift()) {
            return c.getShifts().stream().mapToLong(s -> nz(s.getTonnageRealise())).sum();
        }
        return 0L;
    }

    private long nz(Integer value) {
        return value != null ? value : 0L;
    }
}
