package com.marsa.tos.common;

/**
 * Regroupe les énumérations métier issues du SFD "Gestion des ressources" (opération Manutention).
 */
public final class Enums {

    private Enums() {
    }

    public enum SemaineType {
        SIX_SUR_SEPT, SEPT_SUR_SEPT
    }

    public enum JourType {
        DEUX_SHIFTS, TROIS_SHIFTS
    }

    public enum Shift {
        SHIFT_1, SHIFT_2, SHIFT_3
    }

    public enum Emplacement {
        BORD, QUAI, ARRIERE
    }

    public enum Vacation {
        VACATION_1, VACATION_2
    }

    public enum Sens {
        IMPORT, EXPORT
    }

    public enum NatureSuivi {
        SHIFT, FIN_DU_TRAVAIL
    }

    public enum TypeRoulement {
        MOIS, SEMAINE
    }

    public enum StatutEscale {
        PREVU, EN_COURS, CLOTUREE
    }

    public enum StatutCommande {
        CREEE, LIEE_ESCALE, EN_COURS, VALIDEE
    }

    public enum TypeContrat {
        CDI, CDD, SOUS_TRAITANT
    }
}
