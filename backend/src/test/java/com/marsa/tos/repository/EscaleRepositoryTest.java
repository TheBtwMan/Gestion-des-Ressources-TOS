package com.marsa.tos.repository;

import com.marsa.tos.common.Enums.StatutEscale;
import com.marsa.tos.domain.exploitation.Escale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EscaleRepositoryTest {

    @Autowired
    private EscaleRepository escaleRepository;

    @Test
    void givenEscalesWithDifferentStatus_whenFindByStatut_thenReturnsOnlyMatchingEscales() {
        // Given
        Escale escale1 = Escale.builder()
                .id("ESC-101")
                .navire("Navire A")
                .dateArriveePrevue(LocalDateTime.now())
                .dateDepartPrevue(LocalDateTime.now().plusDays(2))
                .statut(StatutEscale.PREVU)
                .build();

        Escale escale2 = Escale.builder()
                .id("ESC-102")
                .navire("Navire B")
                .dateArriveePrevue(LocalDateTime.now())
                .dateDepartPrevue(LocalDateTime.now().plusDays(2))
                .statut(StatutEscale.EN_COURS)
                .build();

        escaleRepository.save(escale1);
        escaleRepository.save(escale2);

        // When
        List<Escale> prevues = escaleRepository.findByStatut(StatutEscale.PREVU);

        // Then
        assertThat(prevues).hasSize(1);
        assertThat(prevues.get(0).getId()).isEqualTo("ESC-101");
        assertThat(prevues.get(0).getNavire()).isEqualTo("Navire A");
    }
}
