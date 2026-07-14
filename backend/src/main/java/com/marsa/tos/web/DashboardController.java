package com.marsa.tos.web;

import com.marsa.tos.common.Enums.StatutEscale;
import com.marsa.tos.domain.exploitation.Absence;
import com.marsa.tos.domain.exploitation.Arret;
import com.marsa.tos.domain.exploitation.Commande;
import com.marsa.tos.domain.exploitation.Escale;
import com.marsa.tos.domain.referentiel.Personnel;
import com.marsa.tos.repository.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reporting / dashboard de suivi (objectif final du cadrage de stage) : écarts prévu/réalisé,
 * taux d'absentéisme, arrêts, avancement des escales. Alimenté par les données TOS (mock).
 * Chaque endpoint accepte les mêmes filtres optionnels : terminal et plage de dates.
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
    public Map<String, Object> kpis(@RequestParam(required = false) Long terminalId,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        List<Escale> escales = escalesFiltrees(terminalId, dateDebut, dateFin);
        List<Commande> commandes = commandesFiltrees(terminalId, dateDebut, dateFin);
        List<Arret> arrets = arretsFiltres(terminalId, dateDebut, dateFin);
        List<Absence> absences = absencesFiltrees(terminalId, dateDebut, dateFin);
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

        // Absentéisme approximatif : nb de shifts d'absence déclarés / (effectif * nb de jours de la période).
        int joursPeriode = dateDebut != null && dateFin != null
                ? (int) Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFin) + 1)
                : 14;
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
    public List<Map<String, Object>> tonnageParTrafic(@RequestParam(required = false) Long terminalId,
                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        List<Commande> commandes = commandesFiltrees(terminalId, dateDebut, dateFin);
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
    public List<Map<String, Object>> absenteismeParEquipe(@RequestParam(required = false) Long terminalId,
                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        List<Personnel> personnel = personnelRepository.findAll();
        List<Absence> absences = absencesFiltrees(terminalId, dateDebut, dateFin);
        Map<String, Long> absencesParMatricule = absences.stream()
                .collect(Collectors.groupingBy(a -> a.getPersonnel().getMatricule(), Collectors.counting()));

        Map<String, List<Personnel>> parEquipe = personnel.stream()
                .filter(p -> p.getEquipe() != null)
                .filter(p -> terminalId == null || terminalId.equals(p.getEquipe().getTerminal().getId()))
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
    public List<Map<String, Object>> arretsParEquipement(@RequestParam(required = false) Long terminalId,
                                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        List<Arret> arrets = arretsFiltres(terminalId, dateDebut, dateFin);
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

    /** Export CSV de la liste des commandes (mêmes filtres que le dashboard), pour Excel/LibreOffice. */
    @GetMapping("/export/commandes.csv")
    public ResponseEntity<byte[]> exportCommandesCsv(@RequestParam(required = false) Long terminalId,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        List<Commande> commandes = commandesFiltrees(terminalId, dateDebut, dateFin);

        StringBuilder csv = new StringBuilder();
        csv.append("Numero;Client;Trafic;Sens;Statut;Terminal;Escale;Date travail;Tonnage prevu;Tonnage realise;Ecart %\n");
        for (Commande c : commandes) {
            long realise = tonnageRealiseCommande(c);
            long prevu = nz(c.getTonnagePrevu());
            double ecart = prevu == 0 ? 0 : Math.round(((realise - prevu) * 1000.0) / prevu) / 10.0;
            csv.append(csvEscape(c.getNumero())).append(';')
                    .append(csvEscape(c.getClient())).append(';')
                    .append(csvEscape(c.getTrafic().getNom())).append(';')
                    .append(csvEscape(c.getSens().name())).append(';')
                    .append(csvEscape(c.getStatut().name())).append(';')
                    .append(csvEscape(c.getEscale() != null ? c.getEscale().getTerminal().getNom() : "")).append(';')
                    .append(csvEscape(c.getEscale() != null ? c.getEscale().getNavire() : "")).append(';')
                    .append(csvEscape(String.valueOf(c.getDateTravail()))).append(';')
                    .append(prevu).append(';')
                    .append(c.isTonnageRealiseParShift() ? String.valueOf(realise) : "").append(';')
                    .append(c.isTonnageRealiseParShift() ? String.valueOf(ecart) : "")
                    .append('\n');
        }
        return csvResponse(csv.toString(), "commandes.csv");
    }

    /** Export CSV du résumé des KPIs affichés sur le dashboard (mêmes filtres). */
    @GetMapping("/export/kpis.csv")
    public ResponseEntity<byte[]> exportKpisCsv(@RequestParam(required = false) Long terminalId,
                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        Map<String, Object> k = kpis(terminalId, dateDebut, dateFin);

        StringBuilder csv = new StringBuilder();
        csv.append("Indicateur;Valeur\n");
        for (Map.Entry<String, Object> entry : k.entrySet()) {
            csv.append(csvEscape(entry.getKey())).append(';').append(csvEscape(String.valueOf(entry.getValue()))).append('\n');
        }
        return csvResponse(csv.toString(), "dashboard-kpis.csv");
    }

    // ---------------------------------------------------------------- Filtrage commun

    private List<Escale> escalesFiltrees(Long terminalId, LocalDate dateDebut, LocalDate dateFin) {
        return escaleRepository.findAll().stream()
                .filter(e -> terminalId == null || (e.getTerminal() != null && terminalId.equals(e.getTerminal().getId())))
                .filter(e -> dansPeriode(e.getDateArriveePrevue() != null ? e.getDateArriveePrevue().toLocalDate() : null, dateDebut, dateFin))
                .collect(Collectors.toList());
    }

    private List<Commande> commandesFiltrees(Long terminalId, LocalDate dateDebut, LocalDate dateFin) {
        return commandeRepository.findAll().stream()
                .filter(c -> terminalId == null
                        || (c.getEscale() != null && c.getEscale().getTerminal() != null
                            && terminalId.equals(c.getEscale().getTerminal().getId())))
                .filter(c -> dansPeriode(c.getDateTravail(), dateDebut, dateFin))
                .collect(Collectors.toList());
    }

    private List<Arret> arretsFiltres(Long terminalId, LocalDate dateDebut, LocalDate dateFin) {
        return arretRepository.findAll().stream()
                .filter(a -> terminalId == null
                        || (a.getEscale() != null && a.getEscale().getTerminal() != null
                            && terminalId.equals(a.getEscale().getTerminal().getId())))
                .filter(a -> dansPeriode(a.getDateDebut() != null ? a.getDateDebut().toLocalDate() : null, dateDebut, dateFin))
                .collect(Collectors.toList());
    }

    /**
     * Filtre par terminal via la chaîne Personnel -> Equipe -> Terminal (le mock n'attache pas
     * directement une escale/un terminal à une absence).
     */
    private List<Absence> absencesFiltrees(Long terminalId, LocalDate dateDebut, LocalDate dateFin) {
        return absenceRepository.findAll().stream()
                .filter(a -> terminalId == null
                        || (a.getPersonnel().getEquipe() != null
                            && terminalId.equals(a.getPersonnel().getEquipe().getTerminal().getId())))
                .filter(a -> dansPeriode(a.getDateDebut() != null ? a.getDateDebut().toLocalDate() : null, dateDebut, dateFin))
                .collect(Collectors.toList());
    }

    private boolean dansPeriode(LocalDate date, LocalDate dateDebut, LocalDate dateFin) {
        if (date == null) {
            return dateDebut == null && dateFin == null;
        }
        if (dateDebut != null && date.isBefore(dateDebut)) {
            return false;
        }
        return dateFin == null || !date.isAfter(dateFin);
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

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(";", ",").replace("\n", " ").replace("\r", "");
    }

    private ResponseEntity<byte[]> csvResponse(String csv, String filename) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}); // BOM UTF-8 pour Excel
        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            writer.print(csv);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(out.toByteArray());
    }
}
