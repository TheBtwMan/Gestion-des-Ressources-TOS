package com.marsa.tos.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marsa.tos.common.Enums.*;
import com.marsa.tos.domain.admin.Droit;
import com.marsa.tos.domain.admin.Profil;
import com.marsa.tos.domain.admin.Utilisateur;
import com.marsa.tos.domain.exploitation.*;
import com.marsa.tos.domain.parametrage.*;
import com.marsa.tos.domain.referentiel.*;
import com.marsa.tos.repository.*;
import java.io.InputStream;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Charge le mock TOS (simulation des interfaces HR Access / APIPRO / Trafic) et sème les données
 * de paramétrage minimales nécessaires (mode de travail, période shift, mains théoriques, normes,
 * profils/droits) au démarrage, si la base est vide.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${marsa.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${marsa.seed.mock-data-path}")
    private String mockDataPath;

    private final PortRepository portRepository;
    private final TerminalRepository terminalRepository;
    private final FonctionRepository fonctionRepository;
    private final PersonnelRepository personnelRepository;
    private final EquipementFamilleRepository equipementFamilleRepository;
    private final EquipementRepository equipementRepository;
    private final AccessoireRepository accessoireRepository;
    private final TypeTraficRepository typeTraficRepository;
    private final TraficRepository traficRepository;

    private final ModeTravailRepository modeTravailRepository;
    private final PeriodeShiftRepository periodeShiftRepository;
    private final EquipeRepository equipeRepository;
    private final PlanRoulementRepository planRoulementRepository;
    private final MainTheoriqueRepository mainTheoriqueRepository;
    private final NormeProductiviteRepository normeProductiviteRepository;

    private final EscaleRepository escaleRepository;
    private final CommandeRepository commandeRepository;
    private final ArretRepository arretRepository;
    private final AbsenceRepository absenceRepository;

    private final DroitRepository droitRepository;
    private final ProfilRepository profilRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!seedEnabled) {
            return;
        }
        if (escaleRepository.count() > 0) {
            log.info("Données déjà présentes, DataSeeder ignoré.");
            return;
        }

        MockTosData data;
        try (InputStream is = resourceLoader.getResource(mockDataPath).getInputStream()) {
            data = objectMapper.readValue(is, MockTosData.class);
        }

        log.info("Chargement du mock TOS : {} escales, {} commandes, {} personnel, {} équipements",
                data.getEscales().size(), data.getCommandes().size(), data.getPersonnel().size(), data.getEquipements().size());

        Port port = seedPort(data);
        Map<String, Terminal> terminauxByNom = seedTerminaux(data, port);
        Map<String, Fonction> fonctionsByLibelle = seedFonctions(data);
        Map<String, EquipementFamille> famillesByNom = seedEquipementFamilles(data);
        seedEquipements(data, famillesByNom);
        List<Accessoire> accessoires = seedAccessoires();
        Map<String, Trafic> traficsByCode = seedTrafics(data);

        seedOrganisationTravail(terminauxByNom);
        Map<String, Equipe> equipesById = seedEquipes(data, terminauxByNom);
        seedPersonnel(data, fonctionsByLibelle, equipesById);
        seedPlanRoulement(data, equipesById);

        Map<String, Escale> escalesById = seedEscales(data, terminauxByNom);
        Map<String, MainTheorique> mainsParTerminalEtTrafic = seedMainsTheoriquesEtNormes(
                data, terminauxByNom, traficsByCode, fonctionsByLibelle, famillesByNom, accessoires, escalesById);
        seedCommandes(data, escalesById, traficsByCode, mainsParTerminalEtTrafic);

        seedArrets(data, escalesById);
        seedAbsences(data);

        seedAdministration(terminauxByNom);

        log.info("DataSeeder terminé avec succès.");
    }

    // ---------------------------------------------------------------- Référentiels

    private Port seedPort(MockTosData data) {
        String nom = data.get_meta() != null && data.get_meta().getPort() != null
                ? data.get_meta().getPort() : "Port de Casablanca";
        return portRepository.findByNom(nom).orElseGet(() -> portRepository.save(Port.builder().nom(nom).build()));
    }

    private Map<String, Terminal> seedTerminaux(MockTosData data, Port port) {
        Map<String, Terminal> result = new LinkedHashMap<>();
        for (String nom : data.getTerminaux()) {
            Terminal terminal = terminalRepository.findByNom(nom)
                    .orElseGet(() -> terminalRepository.save(Terminal.builder().nom(nom).port(port).build()));
            result.put(nom, terminal);
        }
        return result;
    }

    private Map<String, Fonction> seedFonctions(MockTosData data) {
        Set<String> libelles = data.getPersonnel().stream().map(MockTosData.PersonnelJson::getFonction)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> vacationAutorisees = Set.of("Grutier");
        Map<String, Fonction> result = new LinkedHashMap<>();
        for (String libelle : libelles) {
            String code = slug(libelle);
            Fonction fonction = fonctionRepository.findByCode(code).orElseGet(() -> fonctionRepository.save(
                    Fonction.builder().code(code).libelle(libelle).vacationAutorisee(vacationAutorisees.contains(libelle)).build()));
            result.put(libelle, fonction);
        }
        return result;
    }

    private Map<String, EquipementFamille> seedEquipementFamilles(MockTosData data) {
        Set<String> noms = data.getEquipements().stream().map(MockTosData.EquipementJson::getType)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, EquipementFamille> result = new LinkedHashMap<>();
        for (String nom : noms) {
            EquipementFamille famille = equipementFamilleRepository.findByNom(nom)
                    .orElseGet(() -> equipementFamilleRepository.save(EquipementFamille.builder().nom(nom).build()));
            result.put(nom, famille);
        }
        return result;
    }

    private void seedEquipements(MockTosData data, Map<String, EquipementFamille> famillesByNom) {
        for (MockTosData.EquipementJson e : data.getEquipements()) {
            if (equipementRepository.existsById(e.getId())) {
                continue;
            }
            equipementRepository.save(Equipement.builder()
                    .code(e.getId())
                    .famille(famillesByNom.get(e.getType()))
                    .disponible(true)
                    .build());
        }
    }

    private List<Accessoire> seedAccessoires() {
        List<String> noms = List.of("Palonnier 50T", "Elingues à câbles");
        List<Accessoire> result = new ArrayList<>();
        for (String nom : noms) {
            result.add(accessoireRepository.findByNom(nom)
                    .orElseGet(() -> accessoireRepository.save(Accessoire.builder().nom(nom).build())));
        }
        return result;
    }

    private static final Map<String, String> TRAFIC_LIBELLES = Map.of(
            "RORO", "Ro-Ro / Roulier",
            "VRAC_LIQUIDE", "Vrac liquide",
            "VRAC_SOLIDE", "Vrac solide",
            "CONTENEURS", "Conteneurs",
            "DIVERS", "Divers"
    );
    private static final Map<String, String> TRAFIC_TYPE = Map.of(
            "RORO", "Sacherie et divers",
            "VRAC_LIQUIDE", "Vrac",
            "VRAC_SOLIDE", "Vrac",
            "CONTENEURS", "Conteneur",
            "DIVERS", "Sacherie et divers"
    );

    private Map<String, Trafic> seedTrafics(MockTosData data) {
        Set<String> codes = data.getCommandes().stream().map(MockTosData.CommandeJson::getTrafic)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, TypeTrafic> typesByNom = new LinkedHashMap<>();
        Map<String, Trafic> result = new LinkedHashMap<>();
        for (String code : codes) {
            String typeNom = TRAFIC_TYPE.getOrDefault(code, "Divers");
            TypeTrafic type = typesByNom.computeIfAbsent(typeNom, n -> typeTraficRepository.findByNom(n)
                    .orElseGet(() -> typeTraficRepository.save(TypeTrafic.builder().nom(n).build())));
            Trafic trafic = traficRepository.findByCode(code).orElseGet(() -> traficRepository.save(Trafic.builder()
                    .code(code)
                    .nom(TRAFIC_LIBELLES.getOrDefault(code, code))
                    .typeTrafic(type)
                    .build()));
            result.put(code, trafic);
        }
        return result;
    }

    // ---------------------------------------------------------------- Paramétrage / organisation

    private void seedOrganisationTravail(Map<String, Terminal> terminauxByNom) {
        for (Terminal terminal : terminauxByNom.values()) {
            if (modeTravailRepository.findByTerminalId(terminal.getId()).isEmpty()) {
                modeTravailRepository.save(ModeTravail.builder()
                        .terminal(terminal).semaine(SemaineType.SIX_SUR_SEPT).jour(JourType.DEUX_SHIFTS).build());
            }
            if (periodeShiftRepository.findByTerminalId(terminal.getId()).isEmpty()) {
                periodeShiftRepository.save(PeriodeShift.builder()
                        .terminal(terminal)
                        .shift1NormalDebut(LocalTime.of(6, 45)).shift1NormalFin(LocalTime.of(14, 45))
                        .shift2NormalDebut(LocalTime.of(14, 45)).shift2NormalFin(LocalTime.of(22, 45))
                        .shift3NormalDebut(LocalTime.of(22, 45)).shift3NormalFin(LocalTime.of(6, 45))
                        .shift1RamadanDebut(LocalTime.of(7, 45)).shift1RamadanFin(LocalTime.of(15, 45))
                        .shift2RamadanDebut(LocalTime.of(15, 45)).shift2RamadanFin(LocalTime.of(23, 45))
                        .shift3RamadanDebut(LocalTime.of(23, 45)).shift3RamadanFin(LocalTime.of(7, 45))
                        .build());
            }
        }
    }

    private Map<String, Equipe> seedEquipes(MockTosData data, Map<String, Terminal> terminauxByNom) {
        Terminal terminalParDefaut = terminauxByNom.values().iterator().next();
        Map<String, Equipe> result = new LinkedHashMap<>();
        for (MockTosData.EquipeJson e : data.getEquipes()) {
            Equipe equipe = equipeRepository.findById(e.getId()).orElseGet(() -> equipeRepository.save(Equipe.builder()
                    .id(e.getId())
                    .nom(e.getNom())
                    .responsableMatricule(e.getResponsable())
                    .terminal(terminalParDefaut)
                    .build()));
            result.put(e.getId(), equipe);
        }
        return result;
    }

    private void seedPersonnel(MockTosData data, Map<String, Fonction> fonctionsByLibelle, Map<String, Equipe> equipesById) {
        for (MockTosData.PersonnelJson p : data.getPersonnel()) {
            if (personnelRepository.existsById(p.getMatricule())) {
                continue;
            }
            personnelRepository.save(Personnel.builder()
                    .matricule(p.getMatricule())
                    .nom(p.getNom())
                    .fonction(fonctionsByLibelle.get(p.getFonction()))
                    .equipe(equipesById.get(p.getEquipe()))
                    .actif(true)
                    .build());
        }
    }

    private void seedPlanRoulement(MockTosData data, Map<String, Equipe> equipesById) {
        LocalDate debut = LocalDate.of(2026, 7, 1);
        LocalDate fin = LocalDate.of(2026, 7, 14);
        Shift[] rotation = {Shift.SHIFT_1, Shift.SHIFT_2, Shift.SHIFT_3};
        int i = 0;
        for (Equipe equipe : equipesById.values()) {
            if (!planRoulementRepository.findByEquipeId(equipe.getId()).isEmpty()) {
                continue;
            }
            planRoulementRepository.save(PlanRoulement.builder()
                    .typeRoulement(TypeRoulement.SEMAINE)
                    .dateDebut(debut)
                    .dateFin(fin)
                    .equipe(equipe)
                    .shift(rotation[i % rotation.length])
                    .build());
            i++;
        }
    }

    // ---------------------------------------------------------------- Mains théoriques & normes

    private Map<String, MainTheorique> seedMainsTheoriquesEtNormes(MockTosData data,
                                                                    Map<String, Terminal> terminauxByNom,
                                                                    Map<String, Trafic> traficsByCode,
                                                                    Map<String, Fonction> fonctionsByLibelle,
                                                                    Map<String, EquipementFamille> famillesByNom,
                                                                    List<Accessoire> accessoires,
                                                                    Map<String, Escale> escalesById) {
        // Détermine les couples (terminal, trafic) réellement utilisés par les commandes.
        Map<String, String[]> couples = new LinkedHashMap<>(); // key "terminal||trafic" -> {terminal, trafic}
        for (MockTosData.CommandeJson c : data.getCommandes()) {
            Escale escale = c.getEscaleId() != null ? escalesById.get(c.getEscaleId()) : null;
            String terminalNom = escale != null ? escale.getTerminal().getNom() : terminauxByNom.keySet().iterator().next();
            String key = terminalNom + "||" + c.getTrafic();
            couples.putIfAbsent(key, new String[]{terminalNom, c.getTrafic()});
        }

        Map<String, MainTheorique> result = new LinkedHashMap<>();
        Map<String, Map<Sens, List<Boolean>>> suiviParTrafic = new HashMap<>();
        for (MockTosData.CommandeJson c : data.getCommandes()) {
            suiviParTrafic.computeIfAbsent(c.getTrafic(), k -> new HashMap<>())
                    .computeIfAbsent(Sens.valueOf(c.getSens()), k -> new ArrayList<>())
                    .add(c.isTonnageRealiseParShift());
        }

        for (String[] couple : couples.values()) {
            String terminalNom = couple[0];
            String traficCode = couple[1];
            Terminal terminal = terminauxByNom.get(terminalNom);
            Trafic trafic = traficsByCode.get(traficCode);
            if (terminal == null || trafic == null) {
                continue;
            }

            List<MainTheorique> existantes = mainTheoriqueRepository.findByTraficId(trafic.getId()).stream()
                    .filter(m -> m.getTerminal().getId().equals(terminal.getId()))
                    .collect(Collectors.toList());
            MainTheorique main;
            if (!existantes.isEmpty()) {
                main = existantes.get(0);
            } else {
                main = MainTheorique.builder()
                        .nom("Main " + TRAFIC_LIBELLES.getOrDefault(traficCode, traficCode) + " - " + terminalNom)
                        .trafic(trafic)
                        .terminal(terminal)
                        .build();
                main.setRessourcesHumaines(ressourcesHumainesPour(traficCode, fonctionsByLibelle, main));
                main.setRessourcesMaterielles(ressourcesMaterielsPour(traficCode, famillesByNom, main));
                main.setAccessoires(accessoiresPour(traficCode, accessoires));
                main = mainTheoriqueRepository.save(main);

                Map<Sens, List<Boolean>> suivi = suiviParTrafic.getOrDefault(traficCode, Map.of());
                for (Map.Entry<Sens, List<Boolean>> entry : suivi.entrySet()) {
                    boolean parShift = entry.getValue().stream().anyMatch(Boolean::booleanValue);
                    normeProductiviteRepository.save(NormeProductivite.builder()
                            .trafic(trafic)
                            .mainTheorique(main)
                            .mode(parShift ? "T/Shift" : "T/Escale")
                            .norme(normePour(traficCode))
                            .sens(entry.getKey())
                            .natureSuivi(parShift ? NatureSuivi.SHIFT : NatureSuivi.FIN_DU_TRAVAIL)
                            .build());
                }
            }
            result.put(terminalNom + "||" + traficCode, main);
        }
        return result;
    }

    private List<MainRessourceHumaine> ressourcesHumainesPour(String traficCode, Map<String, Fonction> f, MainTheorique main) {
        List<MainRessourceHumaine> list = new ArrayList<>();
        list.add(mrh(main, f.get("Chef d'équipe"), 1, Emplacement.QUAI, 1, true));
        switch (traficCode) {
            case "RORO":
                list.add(mrh(main, f.get("Conducteur d'engin RoRo"), 3, Emplacement.QUAI, null, false));
                list.add(mrh(main, f.get("Pointeur"), 1, Emplacement.QUAI, null, false));
                break;
            case "CONTENEURS":
                list.add(mrh(main, f.get("Grutier"), 2, Emplacement.BORD, 2, true));
                list.add(mrh(main, f.get("Signaleur"), 1, Emplacement.QUAI, null, false));
                break;
            case "VRAC_LIQUIDE":
            case "VRAC_SOLIDE":
                list.add(mrh(main, f.get("Opérateur chariot élévateur"), 2, Emplacement.QUAI, null, false));
                list.add(mrh(main, f.get("Docker"), 3, Emplacement.QUAI, null, false));
                break;
            default:
                list.add(mrh(main, f.get("Docker"), 3, Emplacement.QUAI, null, false));
                list.add(mrh(main, f.get("Élingueur"), 1, Emplacement.BORD, null, false));
        }
        return list.stream().filter(r -> r.getFonction() != null).collect(Collectors.toList());
    }

    private MainRessourceHumaine mrh(MainTheorique main, Fonction fonction, int nombre, Emplacement emplacement,
                                      Integer max, boolean obligatoire) {
        return MainRessourceHumaine.builder()
                .mainTheorique(main).fonction(fonction).nombreTotal(nombre)
                .emplacement(emplacement).maxNombre(max).maxObligatoire(obligatoire).build();
    }

    private List<MainRessourceMaterielle> ressourcesMaterielsPour(String traficCode, Map<String, EquipementFamille> familles, MainTheorique main) {
        List<String> noms;
        switch (traficCode) {
            case "RORO": noms = List.of("Tracteur RoRo"); break;
            case "CONTENEURS": noms = List.of("Portique", "Chariot élévateur"); break;
            case "VRAC_LIQUIDE":
            case "VRAC_SOLIDE": noms = List.of("Chargeuse", "Bande de convoyage"); break;
            default: noms = List.of("Grue mobile", "Chariot élévateur");
        }
        return noms.stream().filter(familles::containsKey)
                .map(n -> MainRessourceMaterielle.builder().mainTheorique(main).famille(familles.get(n)).build())
                .collect(Collectors.toList());
    }

    private List<Accessoire> accessoiresPour(String traficCode, List<Accessoire> accessoires) {
        return "CONTENEURS".equals(traficCode) || "DIVERS".equals(traficCode) ? accessoires : List.of(accessoires.get(0));
    }

    private int normePour(String traficCode) {
        switch (traficCode) {
            case "VRAC_SOLIDE": return 3000;
            case "VRAC_LIQUIDE": return 2500;
            case "CONTENEURS": return 800;
            case "RORO": return 100;
            default: return 500;
        }
    }

    // ---------------------------------------------------------------- Exploitation

    private Map<String, Escale> seedEscales(MockTosData data, Map<String, Terminal> terminauxByNom) {
        Map<String, Escale> result = new LinkedHashMap<>();
        for (MockTosData.EscaleJson e : data.getEscales()) {
            Escale escale = Escale.builder()
                    .id(e.getId())
                    .navire(e.getNavire())
                    .compagnie(e.getCompagnie())
                    .terminal(terminauxByNom.get(e.getTerminal()))
                    .dateArriveePrevue(e.getDateArriveePrevue())
                    .dateArriveeReelle(e.getDateArriveeReelle())
                    .dateDepartPrevue(e.getDateDepartPrevue())
                    .statut(StatutEscale.valueOf(e.getStatut()))
                    .build();
            escale = escaleRepository.save(escale);
            result.put(e.getId(), escale);
        }
        return result;
    }

    private void seedCommandes(MockTosData data, Map<String, Escale> escalesById, Map<String, Trafic> traficsByCode,
                                Map<String, MainTheorique> mainsParTerminalEtTrafic) {
        for (MockTosData.CommandeJson c : data.getCommandes()) {
            Escale escale = c.getEscaleId() != null ? escalesById.get(c.getEscaleId()) : null;
            Trafic trafic = traficsByCode.get(c.getTrafic());
            String terminalNom = escale != null ? escale.getTerminal().getNom() : null;
            MainTheorique main = terminalNom != null ? mainsParTerminalEtTrafic.get(terminalNom + "||" + c.getTrafic()) : null;

            Commande commande = Commande.builder()
                    .numero(c.getId())
                    .escale(escale)
                    .client(c.getClient())
                    .sens(Sens.valueOf(c.getSens()))
                    .trafic(trafic)
                    .mainTheorique(main)
                    .marchandise(c.getMarchandise())
                    .dateTravail(escale != null ? escale.getDateArriveePrevue().toLocalDate() : LocalDate.of(2026, 7, 1))
                    .shift(Shift.SHIFT_1)
                    .tonnagePrevu(c.getTonnagePrevu())
                    .tonnageRealiseParShift(c.isTonnageRealiseParShift())
                    .statut(escale != null ? StatutCommande.LIEE_ESCALE : StatutCommande.CREEE)
                    .build();

            List<CommandeShiftTonnage> shifts = new ArrayList<>();
            if (c.getShifts() != null) {
                for (MockTosData.CommandeShiftJson s : c.getShifts()) {
                    shifts.add(CommandeShiftTonnage.builder()
                            .commande(commande).shiftId(s.getShiftId()).tonnageRealise(s.getTonnageRealise()).build());
                }
            }
            commande.setShifts(shifts);
            commandeRepository.save(commande);
        }
    }

    private void seedArrets(MockTosData data, Map<String, Escale> escalesById) {
        for (MockTosData.ArretJson a : data.getArrets()) {
            arretRepository.save(Arret.builder()
                    .id(a.getId())
                    .equipement(equipementRepository.findById(a.getEquipementId()).orElse(null))
                    .escale(a.getEscaleId() != null ? escalesById.get(a.getEscaleId()) : null)
                    .codeArret(a.getMotif())
                    .description(a.getMotif())
                    .dateDebut(a.getDateDebut())
                    .dateFin(a.getDateFin())
                    .dureeMinutes(a.getDureeMinutes())
                    .build());
        }
    }

    private void seedAbsences(MockTosData data) {
        for (MockTosData.AbsenceJson a : data.getAbsences()) {
            Personnel personnel = personnelRepository.findById(a.getMatricule()).orElse(null);
            if (personnel == null) {
                continue;
            }
            absenceRepository.save(Absence.builder()
                    .id(a.getId())
                    .personnel(personnel)
                    .escale(null)
                    .dateDebut(a.getDateDebut())
                    .dateFin(a.getDateFin())
                    .shift(shiftDepuisId(a.getShiftId()))
                    .motif(a.getMotif())
                    .build());
        }
    }

    private Shift shiftDepuisId(String shiftId) {
        if (shiftId == null) {
            return null;
        }
        if (shiftId.endsWith("1")) return Shift.SHIFT_1;
        if (shiftId.endsWith("2")) return Shift.SHIFT_2;
        return Shift.SHIFT_3;
    }

    // ---------------------------------------------------------------- Administration

    private void seedAdministration(Map<String, Terminal> terminauxByNom) {
        Map<String, Droit> droits = new LinkedHashMap<>();
        List<String[]> droitDefs = List.of(
                new String[]{"PARAMETRAGE", "Paramétrage (mode de travail, équipes, mains théoriques...)"},
                new String[]{"AFFECTATION_PREVISIONNELLE", "Affectation prévisionnelle"},
                new String[]{"AFFECTATION_REELLE", "Affectation réelle"},
                new String[]{"VALIDATION", "Validation des affectations et clôture des escales"},
                new String[]{"GESTION_UTILISATEURS", "Gestion des comptes et profils utilisateurs"},
                new String[]{"CONSULTATION", "Consultation des données de l'application"}
        );
        for (String[] d : droitDefs) {
            Droit droit = droitRepository.findById(d[0]).orElseGet(() -> droitRepository.save(Droit.builder().code(d[0]).libelle(d[1]).build()));
            droits.put(d[0], droit);
        }

        Profil superAdmin = profilRepository.findByNom("Super Administrateur")
                .orElseGet(() -> profilRepository.save(Profil.builder().nom("Super Administrateur")
                        .droits(new ArrayList<>(droits.values())).build()));
        profilRepository.findByNom("Responsable de prévision")
                .orElseGet(() -> profilRepository.save(Profil.builder().nom("Responsable de prévision")
                        .droits(List.of(droits.get("AFFECTATION_PREVISIONNELLE"), droits.get("CONSULTATION"))).build()));
        profilRepository.findByNom("Responsable de réalisation")
                .orElseGet(() -> profilRepository.save(Profil.builder().nom("Responsable de réalisation")
                        .droits(List.of(droits.get("AFFECTATION_REELLE"), droits.get("CONSULTATION"))).build()));
        profilRepository.findByNom("Responsable de validation")
                .orElseGet(() -> profilRepository.save(Profil.builder().nom("Responsable de validation")
                        .droits(List.of(droits.get("VALIDATION"), droits.get("CONSULTATION"))).build()));
        profilRepository.findByNom("Consultation")
                .orElseGet(() -> profilRepository.save(Profil.builder().nom("Consultation")
                        .droits(List.of(droits.get("CONSULTATION"))).build()));

        if (!utilisateurRepository.existsById("ADMIN001")) {
            utilisateurRepository.save(Utilisateur.builder()
                    .matricule("ADMIN001")
                    .nom("Administrateur")
                    .prenom("Marsa Maroc")
                    .motDePasseHash(passwordEncoder.encode("MarsaMaroc2026!"))
                    .terminal(terminauxByNom.values().iterator().next())
                    .profils(new ArrayList<>(List.of(superAdmin)))
                    .actif(true)
                    .build());
            log.info("Utilisateur admin créé -> matricule=ADMIN001 mot de passe=MarsaMaroc2026!");
        }
    }

    private static String slug(String libelle) {
        String normalized = Normalizer.normalize(libelle, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_").replaceAll("^_|_$", "");
    }
}
