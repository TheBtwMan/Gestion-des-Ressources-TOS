package com.marsa.tos.web;

import com.marsa.tos.domain.parametrage.Equipe;
import com.marsa.tos.domain.referentiel.Personnel;
import com.marsa.tos.repository.EquipeRepository;
import com.marsa.tos.repository.PersonnelRepository;
import com.marsa.tos.repository.TerminalRepository;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Ecran "Equipe" : création des équipes et affectation du personnel par fonction. */
@RestController
@RequestMapping("/api/parametrage/equipes")
@RequiredArgsConstructor
public class EquipeController {

    private final EquipeRepository equipeRepository;
    private final PersonnelRepository personnelRepository;
    private final TerminalRepository terminalRepository;

    @GetMapping
    public List<Equipe> all(@RequestParam(required = false) Long terminalId) {
        return terminalId != null ? equipeRepository.findByTerminalId(terminalId) : equipeRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Equipe> get(@PathVariable String id) {
        return equipeRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/membres")
    public List<Personnel> membres(@PathVariable String id) {
        return personnelRepository.findByEquipeId(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('PARAMETRAGE')")
    public Equipe create(@Valid @RequestBody Equipe equipe) {
        equipe.setTerminal(terminalRepository.findById(equipe.getTerminal().getId())
                .orElseThrow(() -> new IllegalArgumentException("Terminal introuvable")));
        return equipeRepository.save(equipe);
    }

    /** Affecte (ou retire) le personnel donné à cette équipe. */
    @PutMapping("/{id}/membres")
    @PreAuthorize("hasRole('PARAMETRAGE')")
    public List<Personnel> setMembres(@PathVariable String id, @RequestBody List<String> matricules) {
        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Equipe introuvable : " + id));
        personnelRepository.findByEquipeId(id).forEach(p -> p.setEquipe(null));
        List<Personnel> membres = personnelRepository.findAllById(matricules);
        membres.forEach(p -> p.setEquipe(equipe));
        return personnelRepository.saveAll(membres);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PARAMETRAGE')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        equipeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
