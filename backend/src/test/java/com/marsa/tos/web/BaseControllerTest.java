package com.marsa.tos.web;

import com.marsa.tos.config.SecurityConfig;
import com.marsa.tos.security.JwtService;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(SecurityConfig.class)
public abstract class BaseControllerTest {

    @MockitoBean
    protected UserDetailsService userDetailsService;

    @MockitoBean
    protected JwtService jwtService;
}
