package com.marsa.tos.security;

import com.marsa.tos.domain.admin.Utilisateur;
import com.marsa.tos.repository.UtilisateurRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Transactionnel car appelé depuis {@link JwtAuthFilter}, un Filter Servlet qui s'exécute
     * avant l'interception OpenEntityManagerInView de Spring MVC : sans transaction explicite ici,
     * l'accès paresseux à profils/droits lèverait une LazyInitializationException.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String matricule) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findById(matricule)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur inconnu : " + matricule));

        List<SimpleGrantedAuthority> authorities = utilisateur.getProfils().stream()
                .flatMap(profil -> profil.getDroits().stream())
                .map(droit -> new SimpleGrantedAuthority("ROLE_" + droit.getCode()))
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        return User.builder()
                .username(utilisateur.getMatricule())
                .password(utilisateur.getMotDePasseHash())
                .disabled(!utilisateur.isActif())
                .authorities(authorities)
                .build();
    }
}
