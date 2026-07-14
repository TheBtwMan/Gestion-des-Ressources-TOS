package com.marsa.tos.web;

import com.marsa.tos.common.Enums.StatutCommande;
import com.marsa.tos.domain.exploitation.AffectationReelle;
import com.marsa.tos.domain.exploitation.AffectationReelleAccessoire;
import com.marsa.tos.domain.exploitation.AffectationReelleMateriel;
import com.marsa.tos.domain.exploitation.AffectationReelleRH;
import com.marsa.tos.domain.exploitation.Commande;
import com.marsa.tos.domain.parametrage.MainTheorique;
import com.marsa.tos.repository.AffectationReelleRepository;
import com.marsa.tos.repository.CommandeRepository;
import com.marsa.tos.repository.MainTheoriqueRepository;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Ecran "Affectation réelle" (opération Manutention) : saisie en fin de shift, sur la base de la
 * commande créée. Si le trafic est suivi par shift, le tonnage réalisé est saisi ici.
 */
@RestController
@RequestMapping("/api/exploitation/affectations-reelles")
@RequiredArgsConstructor
public class AffectationReelleController {

    private final AffectationReelleRepository repository;
    private final CommandeRepository commandeRepository;
    private final MainTheoriqueRepository mainTheoriqueRepository;

    @GetMapping
    public List<AffectationReelle> byCommande(@RequestParam String commandeNumero) {
        return repository.findByCommandeNumero(commandeNumero);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AffectationReelle> get(@PathVariable Long id) {
        return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('AFFECTATION_REELLE')")
    public AffectationReelle create(@Valid @RequestBody AffectationReelle body) {
        return save(body, null);
    }

    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('AFFECTATION_REELLE')")
    public AffectationReelle update(@PathVariable Long id, @Valid @RequestBody AffectationReelle body) {
        return save(body, id);
    }

    private AffectationReelle save(AffectationReelle body, Long id) {
        Commande commande = commandeRepository.findById(body.getCommande().getNumero())
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable"));
        if (commande.getStatut() == StatutCommande.VALIDEE) {
            throw new IllegalStateException("Commande validée : les affectations ne peuvent plus être modifiées.");
        }

        AffectationReelle affectation = id != null
                ? repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Affectation introuvable"))
                : new AffectationReelle();

        affectation.setCommande(commande);
        if (body.getMainTheorique() != null && body.getMainTheorique().getId() != null) {
            MainTheorique main = mainTheoriqueRepository.findById(body.getMainTheorique().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Main théorique introuvable"));
            affectation.setMainTheorique(main);
        }
        affectation.setDateTravail(body.getDateTravail());
        affectation.setShift(body.getShift());
        affectation.setTonnageRealise(commande.isTonnageRealiseParShift() ? body.getTonnageRealise() : null);
        affectation.setCdi(body.getCdi());
        affectation.setCdd(body.getCdd());
        affectation.setSousTraitant(body.getSousTraitant());

        affectation.getRessourcesHumaines().clear();
        if (body.getRessourcesHumaines() != null) {
            for (AffectationReelleRH rh : body.getRessourcesHumaines()) {
                rh.setAffectation(affectation);
                affectation.getRessourcesHumaines().add(rh);
            }
        }

        affectation.getRessourcesMaterielles().clear();
        if (body.getRessourcesMaterielles() != null) {
            for (AffectationReelleMateriel m : body.getRessourcesMaterielles()) {
                m.setAffectation(affectation);
                affectation.getRessourcesMaterielles().add(m);
            }
        }

        affectation.getAccessoires().clear();
        if (body.getAccessoires() != null) {
            for (AffectationReelleAccessoire a : body.getAccessoires()) {
                a.setAffectation(affectation);
                affectation.getAccessoires().add(a);
            }
        }

        return repository.save(affectation);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AFFECTATION_REELLE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
