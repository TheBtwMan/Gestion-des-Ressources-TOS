package com.marsa.tos.domain.parametrage;

import com.marsa.tos.domain.referentiel.Terminal;
import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.persistence.*;
import lombok.*;

/** Ecran "Période Shift" : 3 shifts fixes, horaires normaux et Ramadan, par terminal. */
@Entity
@Table(name = "param_periode_shift")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodeShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", nullable = false, unique = true)
    private Terminal terminal;

    private LocalTime shift1NormalDebut;
    private LocalTime shift1NormalFin;
    private LocalTime shift2NormalDebut;
    private LocalTime shift2NormalFin;
    private LocalTime shift3NormalDebut;
    private LocalTime shift3NormalFin;

    private LocalTime shift1RamadanDebut;
    private LocalTime shift1RamadanFin;
    private LocalTime shift2RamadanDebut;
    private LocalTime shift2RamadanFin;
    private LocalTime shift3RamadanDebut;
    private LocalTime shift3RamadanFin;

    private LocalDate ramadanDateDebut;
    private LocalDate ramadanDateFin;
}
