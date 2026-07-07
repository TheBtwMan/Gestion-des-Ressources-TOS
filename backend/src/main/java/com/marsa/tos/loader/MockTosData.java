package com.marsa.tos.loader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** Structure du fichier mock-tos-data-marsa-maroc.json (simulation TOS / interface Trafic). */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MockTosData {

    private Meta _meta;
    private List<String> terminaux;
    private List<EquipementJson> equipements;
    private List<PersonnelJson> personnel;
    private List<EquipeJson> equipes;
    private List<ShiftJson> shifts;
    private List<EscaleJson> escales;
    private List<CommandeJson> commandes;
    private List<ArretJson> arrets;
    private List<AbsenceJson> absences;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        private String port;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EquipementJson {
        private String id;
        private String type;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PersonnelJson {
        private String matricule;
        private String nom;
        private String fonction;
        private String equipe;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EquipeJson {
        private String id;
        private String nom;
        private String responsable;
        private Integer effectif;
        private List<String> membres;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ShiftJson {
        private String id;
        private LocalDate date;
        private String periode;
        private String type;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EscaleJson {
        private String id;
        private String navire;
        private String compagnie;
        private String port;
        private String terminal;
        private LocalDateTime dateArriveePrevue;
        private LocalDateTime dateArriveeReelle;
        private LocalDateTime dateDepartPrevue;
        private String statut;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommandeShiftJson {
        private String shiftId;
        private Integer tonnageRealise;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommandeJson {
        private String id;
        private String escaleId;
        private String client;
        private String sens;
        private String trafic;
        private String marchandise;
        private Integer tonnagePrevu;
        private boolean tonnageRealiseParShift;
        private List<CommandeShiftJson> shifts;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ArretJson {
        private String id;
        private String equipementId;
        private String typeEquipement;
        private LocalDateTime dateDebut;
        private LocalDateTime dateFin;
        private Integer dureeMinutes;
        private String motif;
        private String escaleId;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AbsenceJson {
        private String id;
        private String matricule;
        private String nom;
        private String fonction;
        private LocalDateTime dateDebut;
        private LocalDateTime dateFin;
        private String motif;
        private String shiftId;
    }
}
