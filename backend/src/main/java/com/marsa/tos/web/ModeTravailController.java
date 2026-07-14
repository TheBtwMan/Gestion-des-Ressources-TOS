package com.marsa.tos.web;

import com.marsa.tos.domain.parametrage.ModeTravail;
import com.marsa.tos.domain.referentiel.Terminal;
import com.marsa.tos.repository.ModeTravailRepository;
import com.marsa.tos.repository.TerminalRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Ecran "Mode de travail" (opération Manutention), paramétré par terminal. */
@RestController
@RequestMapping("/api/parametrage/mode-travail")
@RequiredArgsConstructor
public class ModeTravailController {

    private final ModeTravailRepository modeTravailRepository;
    private final TerminalRepository terminalRepository;

    @GetMapping("/terminal/{terminalId}")
    public ResponseEntity<ModeTravail> getByTerminal(@PathVariable Long terminalId) {
        return modeTravailRepository.findByTerminalId(terminalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/terminal/{terminalId}")
    @PreAuthorize("hasRole('PARAMETRAGE')")
    public ModeTravail upsert(@PathVariable Long terminalId, @Valid @RequestBody ModeTravail body) {
        Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new IllegalArgumentException("Terminal introuvable : " + terminalId));
        ModeTravail modeTravail = modeTravailRepository.findByTerminalId(terminalId).orElseGet(ModeTravail::new);
        modeTravail.setTerminal(terminal);
        modeTravail.setSemaine(body.getSemaine());
        modeTravail.setJour(body.getJour());
        return modeTravailRepository.save(modeTravail);
    }
}
