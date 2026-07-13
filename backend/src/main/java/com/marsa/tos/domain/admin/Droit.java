package com.marsa.tos.domain.admin;

import jakarta.persistence.*;
import lombok.*;

/** Droit unitaire (ex. AFFECTATION_PREVISIONNELLE, VALIDATION, GESTION_UTILISATEURS...). */
@Entity
@Table(name = "admin_droit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Droit {

    @Id
    private String code;

    @Column(nullable = false)
    private String libelle;
}
