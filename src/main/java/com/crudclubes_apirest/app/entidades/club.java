package com.crudclubes_apirest.app.entidades;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Entidad 'club' que actúa como Documento Raíz (Aggregate Root) en la colección 'clubesfutbol'.
 * 
 * COMENTARIO DE ARQUITECTURA JPA vs MONGODB:
 * - Equivalente JPA/Relacional: En una base de datos relacional (RDBMS), 'club' sería una tabla principal
 *   asociada con múltiples tablas secundarias mediante claves foráneas y relaciones:
 *     * @OneToOne(cascade = CascadeType.ALL) con Entrenador
 *     * @ManyToOne con Asociacion
 *     * @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true) con Jugadores
 *     * @ManyToMany con Competiciones mediante una tabla intermedia (@JoinTable)
 * - Justificación en MongoDB: MongoDB es un motor NoSQL orientado a documentos. Al modelar 'club' como 
 *   un documento raíz con subdocumentos y arreglos embebidos, se aprovecha el principio de 'Data that is 
 *   accessed together should be stored together'. Esto proporciona lecturas y escrituras atómicas en una 
 *   única operación I/O, sin requerir costosos JOINs ($lookup) entre múltiples colecciones y manteniendo 
 *   un modelo flexible y altamente escalable.
 */
@Document(collection = "clubesfutbol")
public class club implements Serializable {

    private static final long serialVersionUID = 1L;

    @Transient
    public static final String SEQUENCE_NAME = "club_sequence";

    @Id
    private Long id;

    @NotBlank(message = "El nombre del club es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre del club debe tener entre 2 y 100 caracteres")
    private String nombre;

    @Valid
    private entrenador entrenador;

    @Valid
    private asociacion asociacion;

    @Valid
    private List<jugador> jugadores = new ArrayList<>();

    @Valid
    private List<competicion> competiciones = new ArrayList<>();

    public club() {
        this.jugadores = new ArrayList<>();
        this.competiciones = new ArrayList<>();
    }

    public club(String nombre) {
        this();
        this.nombre = nombre;
    }

    public club(Long id, String nombre, entrenador entrenador, asociacion asociacion) {
        this();
        this.id = id;
        this.nombre = nombre;
        this.entrenador = entrenador;
        this.asociacion = asociacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public entrenador getEntrenador() {
        return entrenador;
    }

    public void setEntrenador(entrenador entrenador) {
        this.entrenador = entrenador;
    }

    public asociacion getAsociacion() {
        return asociacion;
    }

    public void setAsociacion(asociacion asociacion) {
        this.asociacion = asociacion;
    }

    public List<jugador> getJugadores() {
        if (jugadores == null) {
            jugadores = new ArrayList<>();
        }
        return jugadores;
    }

    public void setJugadores(List<jugador> jugadores) {
        this.jugadores = jugadores != null ? jugadores : new ArrayList<>();
    }

    public List<competicion> getCompeticiones() {
        if (competiciones == null) {
            competiciones = new ArrayList<>();
        }
        return competiciones;
    }

    public void setCompeticiones(List<competicion> competiciones) {
        this.competiciones = competiciones != null ? competiciones : new ArrayList<>();
    }

    // --- Métodos de utilidad para gestión de jugadores ---
    public void addJugador(jugador j) {
        if (this.jugadores == null) {
            this.jugadores = new ArrayList<>();
        }
        this.jugadores.add(j);
    }

    public boolean removeJugador(Long jugadorId) {
        if (this.jugadores == null || jugadorId == null) {
            return false;
        }
        return this.jugadores.removeIf(j -> Objects.equals(j.getId(), jugadorId));
    }

    public Optional<jugador> getJugadorById(Long jugadorId) {
        if (this.jugadores == null || jugadorId == null) {
            return Optional.empty();
        }
        return this.jugadores.stream().filter(j -> Objects.equals(j.getId(), jugadorId)).findFirst();
    }

    // --- Métodos de utilidad para gestión de competiciones ---
    public void addCompeticion(competicion c) {
        if (this.competiciones == null) {
            this.competiciones = new ArrayList<>();
        }
        this.competiciones.add(c);
    }

    public boolean removeCompeticion(Long competicionId) {
        if (this.competiciones == null || competicionId == null) {
            return false;
        }
        return this.competiciones.removeIf(c -> Objects.equals(c.getId(), competicionId));
    }

    public Optional<competicion> getCompeticionById(Long competicionId) {
        if (this.competiciones == null || competicionId == null) {
            return Optional.empty();
        }
        return this.competiciones.stream().filter(c -> Objects.equals(c.getId(), competicionId)).findFirst();
    }

    @Override
    public String toString() {
        return "club{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", entrenador=" + entrenador +
                ", asociacion=" + asociacion +
                ", cantidadJugadores=" + (jugadores != null ? jugadores.size() : 0) +
                ", cantidadCompeticiones=" + (competiciones != null ? competiciones.size() : 0) +
                '}';
    }
}
