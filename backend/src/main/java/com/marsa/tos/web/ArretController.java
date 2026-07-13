package com.marsa.tos.web;

import com.marsa.tos.domain.exploitation.Arret;
import com.marsa.tos.domain.exploitation.Escale;
import com.marsa.tos.domain.referentiel.Equipement;
import com.marsa.tos.repository.ArretRepository;
import com.marsa.tos.repository.EquipementRepository;
import com.marsa.tos.repository.EscaleRepository;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Ecran "Suivi des absences et arrêts" (volet Arrêts, lié à un équipement). */
@RestController
@RequestMapping("/api/exploitation/arrets")
@RequiredArgsConstructor
public class ArretController {

    private final ArretRepository arretRepository;
    private final EscaleRepository escaleRepository;
    private final EquipementRepository equipementRepository;

    @GetMapping
    public List<Arret> all(@RequestParam(required = false) String escaleId) {
        return escaleId != null ? arretRepository.findByEscaleId(escaleId) : arretRepository.findAll();
    }

    @PostMapping
    public Arret create(@Valid @RequestBody Arret body) {
        if (body.getId() == null) {
            body.setId("ARR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (body.getEscale() != null && body.getEscale().getId() != null) {
            Escale escale = escaleRepository.findById(body.getEscale().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Escale introuvable"));
            body.setEscale(escale);
        }
        if (body.getEquipement() != null && body.getEquipement().getCode() != null) {
            Equipement equipement = equipementRepository.findById(body.getEquipement().getCode())
                    .orElseThrow(() -> new IllegalArgumentException("Equipement introuvable"));
            body.setEquipement(equipement);
        }
        if (body.getDateDebut() != null && body.getDateFin() != null) {
            body.setDureeMinutes((int) Duration.between(body.getDateDebut(), body.getDateFin()).toMinutes());
        }
        return arretRepository.save(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        arretRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
