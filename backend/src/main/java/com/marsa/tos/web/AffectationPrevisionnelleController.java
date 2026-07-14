package com.marsa.tos.web;

import com.marsa.tos.common.Enums.StatutCommande;
import com.marsa.tos.domain.exploitation.AffectationPrevisionnelle;
import com.marsa.tos.domain.exploitation.AffectationPrevisionnelleAccessoire;
import com.marsa.tos.domain.exploitation.AffectationPrevisionnelleMateriel;
import com.marsa.tos.domain.exploitation.AffectationPrevisionnelleRH;
import com.marsa.tos.domain.exploitation.Commande;
import com.marsa.tos.domain.parametrage.MainTheorique;
import com.marsa.tos.repository.AffectationPrevisionnelleRepository;
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
 * Ecran "Affectation prévisionnelle" (opération Manutention) : réalisée par date et par shift,
 * sur la base de la main théorique choisie.
 */
@RestController
@RequestMapping("/api/exploitation/affectations-previsionnelles")
@RequiredArgsConstructor
public class AffectationPrevisionnelleController {

    private final AffectationPrevisionnelleRepository repository;
    private final CommandeRepository commandeRepository;
    private final MainTheoriqueRepository mainTheoriqueRepository;

    @GetMapping
    public List<AffectationPrevisionnelle> byCommande(@RequestParam String commandeNumero) {
        return repository.findByCommandeNumero(commandeNumero);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AffectationPrevisionnelle> get(@PathVariable Long id) {
        return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('AFFECTATION_PREVISIONNELLE')")
    public AffectationPrevisionnelle create(@Valid @RequestBody AffectationPrevisionnelle body) {
        return save(body, null);
    }

    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('AFFECTATION_PREVISIONNELLE')")
    public AffectationPrevisionnelle update(@PathVariable Long id, @Valid @RequestBody AffectationPrevisionnelle body) {
        return save(body, id);
    }

    private AffectationPrevisionnelle save(AffectationPrevisionnelle body, Long id) {
        Commande commande = commandeRepository.findById(body.getCommande().getNumero())
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable"));
        if (commande.getStatut() == StatutCommande.VALIDEE) {
            throw new IllegalStateException("Commande validée : les affectations ne peuvent plus être modifiées.");
        }
        MainTheorique main = mainTheoriqueRepository.findById(body.getMainTheorique().getId())
                .orElseThrow(() -> new IllegalArgumentException("Main théorique introuvable"));

        AffectationPrevisionnelle affectation = id != null
                ? repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Affectation introuvable"))
                : new AffectationPrevisionnelle();

        affectation.setCommande(commande);
        affectation.setMainTheorique(main);
        affectation.setDateTravail(body.getDateTravail());
        affectation.setShift(body.getShift());

        affectation.getRessourcesHumaines().clear();
        if (body.getRessourcesHumaines() != null) {
            for (AffectationPrevisionnelleRH rh : body.getRessourcesHumaines()) {
                rh.setAffectation(affectation);
                affectation.getRessourcesHumaines().add(rh);
            }
        }

        affectation.getRessourcesMaterielles().clear();
        if (body.getRessourcesMaterielles() != null) {
            for (AffectationPrevisionnelleMateriel m : body.getRessourcesMaterielles()) {
                m.setAffectation(affectation);
                affectation.getRessourcesMaterielles().add(m);
            }
        }

        affectation.getAccessoires().clear();
        if (body.getAccessoires() != null) {
            for (AffectationPrevisionnelleAccessoire a : body.getAccessoires()) {
                a.setAffectation(affectation);
                affectation.getAccessoires().add(a);
            }
        }

        return repository.save(affectation);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AFFECTATION_PREVISIONNELLE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
