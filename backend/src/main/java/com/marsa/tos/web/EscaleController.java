package com.marsa.tos.web;

import com.marsa.tos.common.Enums.StatutEscale;
import com.marsa.tos.domain.exploitation.Arret;
import com.marsa.tos.domain.exploitation.Commande;
import com.marsa.tos.domain.exploitation.Escale;
import com.marsa.tos.repository.ArretRepository;
import com.marsa.tos.repository.CommandeRepository;
import com.marsa.tos.repository.EscaleRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Escales (interface Trafic/TOS) + écran "Clôturer une escale". */
@RestController
@RequestMapping("/api/exploitation/escales")
@RequiredArgsConstructor
public class EscaleController {

    private final EscaleRepository escaleRepository;
    private final CommandeRepository commandeRepository;
    private final ArretRepository arretRepository;

    @GetMapping
    public List<Escale> all(@RequestParam(required = false) StatutEscale statut) {
        return statut != null ? escaleRepository.findByStatut(statut) : escaleRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Escale> get(@PathVariable String id) {
        return escaleRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/commandes")
    public List<Commande> commandes(@PathVariable String id) {
        return commandeRepository.findByEscaleId(id);
    }

    /** Ecran "Clôturer une escale" : cumul du tonnage par produit + dates début/fin de travail. */
    @GetMapping("/{id}/cloture")
    public Map<String, Object> preparerCloture(@PathVariable String id) {
        Escale escale = escaleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Escale introuvable : " + id));
        List<Commande> commandes = commandeRepository.findByEscaleId(id);

        Map<String, Integer> tonnageParProduit = commandes.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getMarchandise() != null ? c.getMarchandise() : c.getTrafic().getNom(),
                        Collectors.summingInt(c -> {
                            if (c.isTonnageRealiseParShift()) {
                                return c.getShifts().stream()
                                        .mapToInt(s -> s.getTonnageRealise() != null ? s.getTonnageRealise() : 0)
                                        .sum();
                            }
                            return c.getTonnagePrevu() != null ? 0 : 0;
                        })));

        List<Arret> arretsTerminaison = arretRepository.findByEscaleId(id).stream()
                .filter(a -> a.getDateFin() != null)
                .sorted(Comparator.comparing(Arret::getDateFin).reversed())
                .collect(Collectors.toList());

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("escale", escale);
        result.put("commandes", commandes);
        result.put("tonnageParProduit", tonnageParProduit);
        result.put("dateDebutTravailSuggeree", escale.getDateArriveeReelle());
        result.put("dateFinTravailSuggeree", arretsTerminaison.isEmpty() ? null : arretsTerminaison.get(0).getDateFin());
        return result;
    }

    @PostMapping("/{id}/cloturer")
    @PreAuthorize("hasRole('VALIDATION')")
    public Escale cloturer(@PathVariable String id,
                            @RequestBody Map<String, String> body) {
        Escale escale = escaleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Escale introuvable : " + id));
        if (body.get("dateDebutTravail") != null) {
            escale.setDateDebutTravail(LocalDateTime.parse(body.get("dateDebutTravail")));
        }
        if (body.get("dateFinTravail") != null) {
            escale.setDateFinTravail(LocalDateTime.parse(body.get("dateFinTravail")));
        }
        escale.setStatut(StatutEscale.CLOTUREE);
        return escaleRepository.save(escale);
    }
}
