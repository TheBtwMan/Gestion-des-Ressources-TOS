package com.marsa.tos.web.dto;

import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank
    private String matricule;

    @NotBlank
    private String motDePasse;
}
