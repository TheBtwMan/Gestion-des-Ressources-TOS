package com.marsa.tos.web;

import com.marsa.tos.domain.parametrage.MainTheorique;
import com.marsa.tos.domain.parametrage.MainRessourceHumaine;
import com.marsa.tos.domain.parametrage.MainRessourceMaterielle;
import com.marsa.tos.domain.referentiel.Accessoire;
import com.marsa.tos.domain.referentiel.Terminal;
import com.marsa.tos.domain.referentiel.Trafic;
import com.marsa.tos.repository.AccessoireRepository;
import com.marsa.tos.repository.MainTheoriqueRepository;
import com.marsa.tos.repository.TerminalRepository;
import com.marsa.tos.repository.TraficRepository;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Ecran "Main théorique" (opération Manutention) : ressources humaines et matérielles théoriques
 * associées à un trafic, pour un terminal donné.
 */
@RestController
@RequestMapping("/api/parametrage/mains-theoriques")
@RequiredArgsConstructor
public class MainTheoriqueController {

    private final MainTheoriqueRepository mainTheoriqueRepository;
    private final TraficRepository traficRepository;
    private final TerminalRepository terminalRepository;
    private final AccessoireRepository accessoireRepository;

    @GetMapping
    public List<MainTheorique> all(@RequestParam(required = false) Long traficId,
                                    @RequestParam(required = false) Long terminalId) {
        if (traficId != null) {
            return mainTheoriqueRepository.findByTraficId(traficId);
        }
        if (terminalId != null) {
            return mainTheoriqueRepository.findByTerminalId(terminalId);
        }
        return mainTheoriqueRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MainTheorique> get(@PathVariable Long id) {
        return mainTheoriqueRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public MainTheorique create(@Valid @RequestBody MainTheorique body) {
        return save(body, null);
    }

    @PutMapping("/{id}")
    @Transactional
    public MainTheorique update(@PathVariable Long id, @Valid @RequestBody MainTheorique body) {
        return save(body, id);
    }

    private MainTheorique save(MainTheorique body, Long id) {
        Trafic trafic = traficRepository.findById(body.getTrafic().getId())
                .orElseThrow(() -> new IllegalArgumentException("Trafic introuvable"));
        Terminal terminal = terminalRepository.findById(body.getTerminal().getId())
                .orElseThrow(() -> new IllegalArgumentException("Terminal introuvable"));

        MainTheorique main = id != null
                ? mainTheoriqueRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Main théorique introuvable"))
                : new MainTheorique();

        main.setNom(body.getNom());
        main.setTrafic(trafic);
        main.setTerminal(terminal);

        main.getRessourcesHumaines().clear();
        if (body.getRessourcesHumaines() != null) {
            for (MainRessourceHumaine rh : body.getRessourcesHumaines()) {
                rh.setMainTheorique(main);
                main.getRessourcesHumaines().add(rh);
            }
        }

        main.getRessourcesMaterielles().clear();
        if (body.getRessourcesMaterielles() != null) {
            for (MainRessourceMaterielle rm : body.getRessourcesMaterielles()) {
                rm.setMainTheorique(main);
                main.getRessourcesMaterielles().add(rm);
            }
        }

        if (body.getAccessoires() != null) {
            List<Long> accessoireIds = body.getAccessoires().stream().map(Accessoire::getId).collect(Collectors.toList());
            main.setAccessoires(accessoireRepository.findAllById(accessoireIds));
        }

        return mainTheoriqueRepository.save(main);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mainTheoriqueRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
