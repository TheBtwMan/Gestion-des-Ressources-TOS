package com.marsa.tos.domain.referentiel;

import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "ref_accessoire")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Accessoire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;
}
