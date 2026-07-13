package com.marsa.tos.web;

import com.marsa.tos.common.Enums.StatutCommande;
import com.marsa.tos.domain.exploitation.AffectationPrevisionnelle;
import com.marsa.tos.domain.exploitation.AffectationReelle;
import com.marsa.tos.domain.exploitation.Commande;
import com.marsa.tos.domain.exploitation.Escale;
import com.marsa.tos.domain.parametrage.MainTheorique;
import com.marsa.tos.domain.referentiel.Trafic;
import com.marsa.tos.repository.*;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Ecran "Affectation prévisionnelle" (en-tête commande) + "Validation" + "Lier commande à une escale". */
@RestController
@RequestMapping("/api/exploitation/commandes")
@RequiredArgsConstructor
public class CommandeController {

    private final CommandeRepository commandeRepository;
    private final EscaleRepository escaleRepository;
    private final TraficRepository traficRepository;
    private final MainTheoriqueRepository mainTheoriqueRepository;
    private final AffectationPrevisionnelleRepository affectationPrevisionnelleRepository;
    private final AffectationReelleRepository affectationReelleRepository;

    @GetMapping
    public List<Commande> all(@RequestParam(required = false) Boolean sansEscale,
                               @RequestParam(required = false) String escaleId) {
        if (Boolean.TRUE.equals(sansEscale)) {
            return commandeRepository.findByEscaleIsNull();
        }
        if (escaleId != null) {
            return commandeRepository.findByEscaleId(escaleId);
        }
        return commandeRepository.findAll();
    }

    @GetMapping("/{numero}")
    public ResponseEntity<Commande> get(@PathVariable String numero) {
        return commandeRepository.findById(numero).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Commande create(@Valid @RequestBody Commande body) {
        Trafic trafic = traficRepository.findById(body.getTrafic().getId())
                .orElseThrow(() -> new IllegalArgumentException("Trafic introuvable"));
        body.setTrafic(trafic);
        if (body.getMainTheorique() != null && body.getMainTheorique().getId() != null) {
            MainTheorique main = mainTheoriqueRepository.findById(body.getMainTheorique().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Main théorique introuvable"));
            body.setMainTheorique(main);
        }
        if (body.getEscale() != null && body.getEscale().getId() != null) {
            Escale escale = escaleRepository.findById(body.getEscale().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Escale introuvable"));
            body.setEscale(escale);
            body.setStatut(StatutCommande.LIEE_ESCALE);
        }
        return commandeRepository.save(body);
    }

    /** Ecran "Lier commande à une escale" : chaque commande ne peut être liée qu'à une seule escale. */
    @PostMapping("/{numero}/lier-escale/{escaleId}")
    public Commande lierEscale(@PathVariable String numero, @PathVariable String escaleId) {
        Commande commande = commandeRepository.findById(numero)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable : " + numero));
        Escale escale = escaleRepository.findById(escaleId)
                .orElseThrow(() -> new IllegalArgumentException("Escale introuvable : " + escaleId));
        commande.setEscale(escale);
        commande.setStatut(StatutCommande.LIEE_ESCALE);
        return commandeRepository.save(commande);
    }

    /** Ecran "Validation" : une commande validée bloque les changements sur les affectations réelles. */
    @GetMapping("/{numero}/validation")
    public Map<String, Object> validationView(@PathVariable String numero) {
        Commande commande = commandeRepository.findById(numero)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable : " + numero));
        List<AffectationPrevisionnelle> previsionnelles = affectationPrevisionnelleRepository.findByCommandeNumero(numero);
        List<AffectationReelle> reelles = affectationReelleRepository.findByCommandeNumero(numero);
        return Map.of("commande", commande, "previsionnelles", previsionnelles, "reelles", reelles);
    }

    @PostMapping("/{numero}/valider")
    public Commande valider(@PathVariable String numero) {
        Commande commande = commandeRepository.findById(numero)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable : " + numero));
        commande.setStatut(StatutCommande.VALIDEE);
        return commandeRepository.save(commande);
    }
}
