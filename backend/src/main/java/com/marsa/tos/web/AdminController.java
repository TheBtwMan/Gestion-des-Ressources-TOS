package com.marsa.tos.web;

import com.marsa.tos.domain.admin.Droit;
import com.marsa.tos.domain.admin.Profil;
import com.marsa.tos.domain.admin.Utilisateur;
import com.marsa.tos.domain.referentiel.Terminal;
import com.marsa.tos.repository.DroitRepository;
import com.marsa.tos.repository.ProfilRepository;
import com.marsa.tos.repository.TerminalRepository;
import com.marsa.tos.repository.UtilisateurRepository;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Module Administration : "Changement des profils par droits", "Création des utilisateurs".
 * L'écran "Authentification" est géré par {@link AuthController}.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DroitRepository droitRepository;
    private final ProfilRepository profilRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final TerminalRepository terminalRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/droits")
    public List<Droit> droits() {
        return droitRepository.findAll();
    }

    @GetMapping("/profils")
    public List<Profil> profils() {
        return profilRepository.findAll();
    }

    /** Ecran "Changement des profils par droits" : la liste des droits affectés doit contenir au moins un droit. */
    @PutMapping("/profils/{id}/droits")
    public Profil setDroits(@PathVariable Long id, @RequestBody List<String> droitCodes) {
        if (droitCodes.isEmpty()) {
            throw new IllegalArgumentException("Un profil doit contenir au moins un droit.");
        }
        Profil profil = profilRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profil introuvable : " + id));
        profil.setDroits(droitRepository.findAllById(droitCodes));
        return profilRepository.save(profil);
    }

    @PostMapping("/profils")
    public Profil createProfil(@Valid @RequestBody Profil body) {
        body.setId(null);
        if (body.getDroits() != null && !body.getDroits().isEmpty()) {
            List<String> codes = body.getDroits().stream().map(Droit::getCode).collect(Collectors.toList());
            body.setDroits(droitRepository.findAllById(codes));
        }
        return profilRepository.save(body);
    }

    @GetMapping("/utilisateurs")
    public List<Utilisateur> utilisateurs() {
        return utilisateurRepository.findAll();
    }

    /** Ecran "Création des utilisateurs" : au moins un profil doit être affecté. */
    @PostMapping("/utilisateurs")
    public Utilisateur createUtilisateur(@Valid @RequestBody UtilisateurRequest body) {
        if (body.profilIds == null || body.profilIds.isEmpty()) {
            throw new IllegalArgumentException("Un utilisateur doit avoir au moins un profil.");
        }
        Utilisateur utilisateur = Utilisateur.builder()
                .matricule(body.matricule)
                .nom(body.nom)
                .prenom(body.prenom)
                .motDePasseHash(passwordEncoder.encode(body.motDePasse))
                .profils(profilRepository.findAllById(body.profilIds))
                .actif(true)
                .build();
        if (body.terminalId != null) {
            Terminal terminal = terminalRepository.findById(body.terminalId)
                    .orElseThrow(() -> new IllegalArgumentException("Terminal introuvable"));
            utilisateur.setTerminal(terminal);
        }
        return utilisateurRepository.save(utilisateur);
    }

    @PutMapping("/utilisateurs/{matricule}")
    public Utilisateur updateUtilisateur(@PathVariable String matricule, @Valid @RequestBody UtilisateurRequest body) {
        Utilisateur utilisateur = utilisateurRepository.findById(matricule)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable : " + matricule));
        utilisateur.setNom(body.nom);
        utilisateur.setPrenom(body.prenom);
        if (body.motDePasse != null && !body.motDePasse.isBlank()) {
            utilisateur.setMotDePasseHash(passwordEncoder.encode(body.motDePasse));
        }
        if (body.profilIds != null && !body.profilIds.isEmpty()) {
            utilisateur.setProfils(profilRepository.findAllById(body.profilIds));
        }
        return utilisateurRepository.save(utilisateur);
    }

    /** Suppression = désactivation du compte (règle de gestion SFD). */
    @DeleteMapping("/utilisateurs/{matricule}")
    public Utilisateur deactivate(@PathVariable String matricule) {
        Utilisateur utilisateur = utilisateurRepository.findById(matricule)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable : " + matricule));
        utilisateur.setActif(false);
        return utilisateurRepository.save(utilisateur);
    }

    public static class UtilisateurRequest {
        public String matricule;
        public String nom;
        public String prenom;
        public String motDePasse;
        public Long terminalId;
        public List<Long> profilIds;
    }
}
