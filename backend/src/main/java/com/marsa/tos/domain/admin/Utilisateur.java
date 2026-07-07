package com.marsa.tos.domain.admin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marsa.tos.domain.referentiel.Terminal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import lombok.*;

/** Ecran "Création des utilisateurs" + "Authentification" (login = matricule). */
@Entity
@Table(name = "admin_utilisateur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    @Id
    private String matricule;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    @JsonIgnore
    private String motDePasseHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id")
    private Terminal terminal;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "admin_utilisateur_profil",
            joinColumns = @JoinColumn(name = "utilisateur_matricule"),
            inverseJoinColumns = @JoinColumn(name = "profil_id"))
    private List<Profil> profils = new ArrayList<>();

    @Builder.Default
    private boolean actif = true;
}
