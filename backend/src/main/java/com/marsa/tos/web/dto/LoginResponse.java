package com.marsa.tos.web.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String matricule;
    private String nom;
    private String prenom;
    private List<String> profils;
    private List<String> droits;
}
