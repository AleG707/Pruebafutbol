package com.crudclubes_apirest.app;

import com.crudclubes_apirest.app.entidades.asociacion;
import com.crudclubes_apirest.app.entidades.club;
import com.crudclubes_apirest.app.entidades.competicion;
import com.crudclubes_apirest.app.entidades.entrenador;
import com.crudclubes_apirest.app.entidades.jugador;
import com.crudclubes_apirest.app.repositorios.club_repositorio;
import com.crudclubes_apirest.app.servicios.SequenceGeneratorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.Arrays;

@SpringBootApplication
public class CrudClubesApirestApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrudClubesApirestApplication.class, args);
    }

    /**
     * Semilla de datos de prueba para inicializar la colección 'clubesfutbol' en MongoDB Atlas
     * SOLO si la colección se encuentra vacía.
     */
    @Bean
    public CommandLineRunner inicializarDatos(club_repositorio clubRepositorio, SequenceGeneratorService sequenceGenerator) {
        return args -> {
            if (clubRepositorio.count() == 0) {
                System.out.println(">>> Colección 'clubesfutbol' vacía. Insertando datos de ejemplo (Millonarios y Santa Fe)...");

                // 1. Club Millonarios FC
                club millonarios = new club();
                millonarios.setId(sequenceGenerator.generateSequence(club.SEQUENCE_NAME));
                millonarios.setNombre("Millonarios Fútbol Club");
                millonarios.setEntrenador(new entrenador("Alberto", "Gamero", 60, "Colombiana"));
                millonarios.setAsociacion(new asociacion("Federación Colombiana de Fútbol (FCF)", "Colombia", "Ramón Jesurún"));

                millonarios.addJugador(new jugador(sequenceGenerator.generateSequence(jugador.SEQUENCE_NAME), "David", "Silva", 14, "Mediocampista"));
                millonarios.addJugador(new jugador(sequenceGenerator.generateSequence(jugador.SEQUENCE_NAME), "Leonardo", "Castro", 23, "Delantero"));
                millonarios.addJugador(new jugador(sequenceGenerator.generateSequence(jugador.SEQUENCE_NAME), "Álvaro", "Montero", 31, "Portero"));
                millonarios.addJugador(new jugador(sequenceGenerator.generateSequence(jugador.SEQUENCE_NAME), "Juan Pablo", "Vargas", 3, "Defensa"));

                millonarios.addCompeticion(new competicion(sequenceGenerator.generateSequence(competicion.SEQUENCE_NAME), "Superliga BetPlay", 500000000, LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 24)));
                millonarios.addCompeticion(new competicion(sequenceGenerator.generateSequence(competicion.SEQUENCE_NAME), "Copa Postobón (Copa Colombia)", 600000000, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 11, 20)));
                millonarios.addCompeticion(new competicion(sequenceGenerator.generateSequence(competicion.SEQUENCE_NAME), "Copa Libertadores de América", 3000000000L > Integer.MAX_VALUE ? 2000000000 : 2000000000, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 11, 30)));

                // 2. Club Independiente Santa Fe
                club santaFe = new club();
                santaFe.setId(sequenceGenerator.generateSequence(club.SEQUENCE_NAME));
                santaFe.setNombre("Independiente Santa Fe");
                santaFe.setEntrenador(new entrenador("Pablo", "Peirano", 49, "Uruguaya"));
                santaFe.setAsociacion(new asociacion("Federación Colombiana de Fútbol (FCF)", "Colombia", "Ramón Jesurún"));

                santaFe.addJugador(new jugador(sequenceGenerator.generateSequence(jugador.SEQUENCE_NAME), "Hugo", "Rodallega", 11, "Delantero"));
                santaFe.addJugador(new jugador(sequenceGenerator.generateSequence(jugador.SEQUENCE_NAME), "Daniel", "Torres", 16, "Mediocampista"));
                santaFe.addJugador(new jugador(sequenceGenerator.generateSequence(jugador.SEQUENCE_NAME), "Andrés", "Mosquera", 1, "Portero"));
                santaFe.addJugador(new jugador(sequenceGenerator.generateSequence(jugador.SEQUENCE_NAME), "Jhojan", "Torres", 8, "Defensa"));

                santaFe.addCompeticion(new competicion(sequenceGenerator.generateSequence(competicion.SEQUENCE_NAME), "Superliga BetPlay", 500000000, LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 24)));
                santaFe.addCompeticion(new competicion(sequenceGenerator.generateSequence(competicion.SEQUENCE_NAME), "Copa Postobón (Copa Colombia)", 600000000, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 11, 20)));
                santaFe.addCompeticion(new competicion(sequenceGenerator.generateSequence(competicion.SEQUENCE_NAME), "Copa Libertadores de América", 2000000000, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 11, 30)));

                clubRepositorio.saveAll(Arrays.asList(millonarios, santaFe));
                System.out.println(">>> Datos de ejemplo insertados exitosamente en MongoDB Atlas.");
            } else {
                System.out.println(">>> Colección 'clubesfutbol' ya contiene registros. No se insertaron datos duplicados.");
            }
        };
    }
}
