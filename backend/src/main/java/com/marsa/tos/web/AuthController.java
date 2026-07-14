package com.marsa.tos.web;

import com.marsa.tos.domain.admin.Utilisateur;
import com.marsa.tos.repository.UtilisateurRepository;
import com.marsa.tos.security.JwtService;
import com.marsa.tos.web.dto.LoginRequest;
import com.marsa.tos.web.dto.LoginResponse;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UtilisateurRepository utilisateurRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getMatricule(), request.getMotDePasse()));
        } catch (BadCredentialsException | org.springframework.security.authentication.DisabledException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("message", "Matricule ou mot de passe incorrect, ou compte désactivé."));
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getMatricule());
        String token = jwtService.generateToken(userDetails);

        Utilisateur utilisateur = utilisateurRepository.findById(request.getMatricule()).orElseThrow();
        List<String> profils = utilisateur.getProfils().stream().map(p -> p.getNom()).collect(Collectors.toList());
        List<String> droits = utilisateur.getProfils().stream()
                .flatMap(p -> p.getDroits().stream())
                .map(d -> d.getCode())
                .distinct()
                .collect(Collectors.toList());

        Long termId = utilisateur.getTerminal() != null ? utilisateur.getTerminal().getId() : null;
        String termNom = utilisateur.getTerminal() != null ? utilisateur.getTerminal().getNom() : null;
        return ResponseEntity.ok(new LoginResponse(token, utilisateur.getMatricule(), utilisateur.getNom(),
                utilisateur.getPrenom(), profils, droits, termId, termNom));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@org.springframework.security.core.annotation.AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Utilisateur utilisateur = utilisateurRepository.findById(principal.getUsername()).orElseThrow();
        List<String> profils = utilisateur.getProfils().stream().map(p -> p.getNom()).collect(Collectors.toList());
        List<String> droits = utilisateur.getProfils().stream()
                .flatMap(p -> p.getDroits().stream())
                .map(d -> d.getCode())
                .distinct()
                .collect(Collectors.toList());
        Long termId = utilisateur.getTerminal() != null ? utilisateur.getTerminal().getId() : null;
        String termNom = utilisateur.getTerminal() != null ? utilisateur.getTerminal().getNom() : null;
        return ResponseEntity.ok(new LoginResponse(null, utilisateur.getMatricule(), utilisateur.getNom(),
                utilisateur.getPrenom(), profils, droits, termId, termNom));
    }
}
