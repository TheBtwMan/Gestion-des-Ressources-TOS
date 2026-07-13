package com.marsa.tos.web;

import com.marsa.tos.domain.exploitation.Absence;
import com.marsa.tos.domain.exploitation.Escale;
import com.marsa.tos.domain.referentiel.Personnel;
import com.marsa.tos.repository.AbsenceRepository;
import com.marsa.tos.repository.EscaleRepository;
import com.marsa.tos.repository.PersonnelRepository;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Ecran "Suivi des absences et arrêts" (volet Absences, lié au personnel). */
@RestController
@RequestMapping("/api/exploitation/absences")
@RequiredArgsConstructor
public class AbsenceController {

    private final AbsenceRepository absenceRepository;
    private final EscaleRepository escaleRepository;
    private final PersonnelRepository personnelRepository;

    @GetMapping
    public List<Absence> all(@RequestParam(required = false) String escaleId) {
        return escaleId != null ? absenceRepository.findByEscaleId(escaleId) : absenceRepository.findAll();
    }

    @PostMapping
    public Absence create(@Valid @RequestBody Absence body) {
        if (body.getId() == null) {
            body.setId("ABS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        Personnel personnel = personnelRepository.findById(body.getPersonnel().getMatricule())
                .orElseThrow(() -> new IllegalArgumentException("Personnel introuvable"));
        body.setPersonnel(personnel);
        if (body.getEscale() != null && body.getEscale().getId() != null) {
            Escale escale = escaleRepository.findById(body.getEscale().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Escale introuvable"));
            body.setEscale(escale);
        }
        return absenceRepository.save(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        absenceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
