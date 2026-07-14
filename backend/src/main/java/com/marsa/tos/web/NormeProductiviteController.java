package com.marsa.tos.web;

import com.marsa.tos.domain.parametrage.MainTheorique;
import com.marsa.tos.domain.parametrage.NormeProductivite;
import com.marsa.tos.domain.referentiel.Trafic;
import com.marsa.tos.repository.MainTheoriqueRepository;
import com.marsa.tos.repository.NormeProductiviteRepository;
import com.marsa.tos.repository.TraficRepository;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Ecran "Norme de productivité". */
@RestController
@RequestMapping("/api/parametrage/normes-productivite")
@RequiredArgsConstructor
public class NormeProductiviteController {

    private final NormeProductiviteRepository normeProductiviteRepository;
    private final TraficRepository traficRepository;
    private final MainTheoriqueRepository mainTheoriqueRepository;

    @GetMapping
    public List<NormeProductivite> all(@RequestParam(required = false) Long traficId) {
        return traficId != null ? normeProductiviteRepository.findByTraficId(traficId) : normeProductiviteRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('PARAMETRAGE')")
    public NormeProductivite create(@Valid @RequestBody NormeProductivite body) {
        Trafic trafic = traficRepository.findById(body.getTrafic().getId())
                .orElseThrow(() -> new IllegalArgumentException("Trafic introuvable"));
        MainTheorique main = mainTheoriqueRepository.findById(body.getMainTheorique().getId())
                .orElseThrow(() -> new IllegalArgumentException("Main théorique introuvable"));
        body.setId(null);
        body.setTrafic(trafic);
        body.setMainTheorique(main);
        return normeProductiviteRepository.save(body);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PARAMETRAGE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        normeProductiviteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
