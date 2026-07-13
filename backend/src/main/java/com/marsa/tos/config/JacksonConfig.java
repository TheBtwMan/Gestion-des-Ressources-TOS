package com.marsa.tos.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Permet à Jackson de sérialiser les proxies Hibernate (associations @ManyToOne LAZY) : les
 * associations non initialisées sont chargées à la volée (open-in-view garde la session ouverte
 * pendant le rendu de la réponse) plutôt que de faire échouer la sérialisation.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate6Module hibernate6Module() {
        Hibernate6Module module = new Hibernate6Module();
        module.enable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
        return module;
    }
}
