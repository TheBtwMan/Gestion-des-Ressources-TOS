package com.marsa.tos.web;

import com.marsa.tos.domain.parametrage.PeriodeShift;
import com.marsa.tos.domain.referentiel.Terminal;
import com.marsa.tos.repository.PeriodeShiftRepository;
import com.marsa.tos.repository.TerminalRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Ecran "Période Shift" (opération Manutention), paramétré par terminal. */
@RestController
@RequestMapping("/api/parametrage/periode-shift")
@RequiredArgsConstructor
public class PeriodeShiftController {

    private final PeriodeShiftRepository periodeShiftRepository;
    private final TerminalRepository terminalRepository;

    @GetMapping("/terminal/{terminalId}")
    public ResponseEntity<PeriodeShift> getByTerminal(@PathVariable Long terminalId) {
        return periodeShiftRepository.findByTerminalId(terminalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/terminal/{terminalId}")
    @PreAuthorize("hasRole('PARAMETRAGE')")
    public PeriodeShift upsert(@PathVariable Long terminalId, @Valid @RequestBody PeriodeShift body) {
        Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new IllegalArgumentException("Terminal introuvable : " + terminalId));
        PeriodeShift periodeShift = periodeShiftRepository.findByTerminalId(terminalId).orElseGet(PeriodeShift::new);
        Long id = periodeShift.getId();
        BeanUtils.copyProperties(body, periodeShift);
        periodeShift.setId(id);
        periodeShift.setTerminal(terminal);
        return periodeShiftRepository.save(periodeShift);
    }
}
