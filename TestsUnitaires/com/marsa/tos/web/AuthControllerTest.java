package com.marsa.tos.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marsa.tos.domain.admin.Utilisateur;
import com.marsa.tos.repository.UtilisateurRepository;
import com.marsa.tos.web.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UtilisateurRepository utilisateurRepository;

    @Test
    @WithMockUser
    void givenValidCredentials_whenLogin_thenReturnsTokenAndDetails() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setMatricule("ADMIN001");
        request.setMotDePasse("MarsaMaroc2026!");

        UserDetails userDetails = new User("ADMIN001", "password", Collections.emptyList());
        Utilisateur user = new Utilisateur();
        user.setMatricule("ADMIN001");
        user.setNom("Maroc");
        user.setPrenom("Marsa");
        user.setProfils(new java.util.ArrayList<>());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userDetailsService.loadUserByUsername("ADMIN001")).thenReturn(userDetails);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("fake-jwt-token");
        when(utilisateurRepository.findById("ADMIN001")).thenReturn(Optional.of(user));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.matricule").value("ADMIN001"))
                .andExpect(jsonPath("$.nom").value("Maroc"));
    }

    @Test
    @WithMockUser
    void givenInvalidCredentials_whenLogin_thenReturnsUnauthorized() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setMatricule("ADMIN001");
        request.setMotDePasse("wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Matricule ou mot de passe incorrect, ou compte désactivé."));
    }

    @Test
    @WithMockUser(username = "ADMIN001")
    void givenAuthenticatedUser_whenMe_thenReturnsUserDetails() throws Exception {
        // Given
        Utilisateur user = new Utilisateur();
        user.setMatricule("ADMIN001");
        user.setNom("Maroc");
        user.setPrenom("Marsa");
        user.setProfils(new java.util.ArrayList<>());

        when(utilisateurRepository.findById("ADMIN001")).thenReturn(Optional.of(user));

        // When & Then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricule").value("ADMIN001"))
                .andExpect(jsonPath("$.nom").value("Maroc"));
    }
}
