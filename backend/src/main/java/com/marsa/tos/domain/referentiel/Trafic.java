package com.marsa.tos.domain.referentiel;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ref_trafic")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trafic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    /** Code technique utilisé pour rapprocher les commandes du mock TOS (ex. VRAC_SOLIDE, RORO...). */
    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_trafic_id", nullable = false)
    private TypeTrafic typeTrafic;
}
