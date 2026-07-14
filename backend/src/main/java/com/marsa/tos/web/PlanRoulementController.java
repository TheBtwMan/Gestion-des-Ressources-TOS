package com.marsa.tos.web;

import com.marsa.tos.domain.parametrage.Equipe;
import com.marsa.tos.domain.parametrage.PlanRoulement;
import com.marsa.tos.repository.EquipeRepository;
import com.marsa.tos.repository.PlanRoulementRepository;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Ecran "Plan de roulement" : association équipe <-> shift, par mois ou par semaine. */
@RestController
@RequestMapping("/api/parametrage/plan-roulement")
@RequiredArgsConstructor
public class PlanRoulementController {

    private final PlanRoulementRepository planRoulementRepository;
    private final EquipeRepository equipeRepository;

    @GetMapping
    public List<PlanRoulement> all(@RequestParam(required = false) String equipeId) {
        return equipeId != null ? planRoulementRepository.findByEquipeId(equipeId) : planRoulementRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('PARAMETRAGE')")
    public PlanRoulement create(@Valid @RequestBody PlanRoulement body) {
        Equipe equipe = equipeRepository.findById(body.getEquipe().getId())
                .orElseThrow(() -> new IllegalArgumentException("Equipe introuvable"));
        body.setId(null);
        body.setEquipe(equipe);
        return planRoulementRepository.save(body);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PARAMETRAGE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        planRoulementRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
