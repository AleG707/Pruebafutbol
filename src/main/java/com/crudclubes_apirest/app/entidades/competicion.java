package com.crudclubes_apirest.app.entidades;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Transient;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Entidad 'competicion' que representa los torneos, copas y campeonatos disputados por un club.
 * 
 * COMENTARIO DE ARQUITECTURA JPA vs MONGODB:
 * - Equivalente JPA/Relacional: Anotación @ManyToMany mapeada mediante una tabla intermedia de unión (@JoinTable) entre 'club' y 'competicion'.
 * - Justificación en MongoDB: Se modela embebida como una lista de documentos dentro de 'club' para registrar el historial y participación 
 *   específica del club de forma autocontenida y desnormalizada, optimizando la lectura inmediata de las fechas de participación y premios.
 */
public class competicion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Transient
    public static final String SEQUENCE_NAME = "competicion_sequence";

    private Long id;

    @NotBlank(message = "El nombre de la competición es obligatorio")
    private String nombre;

    @NotNull(message = "El monto del premio es obligatorio")
    @Min(value = 0, message = "El monto del premio no puede ser negativo (mínimo 0)")
    private Integer montoPremio;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFin;

    public competicion() {
    }

    public competicion(String nombre, Integer montoPremio, LocalDate fechaInicio, LocalDate fechaFin) {
        this.nombre = nombre;
        this.montoPremio = montoPremio;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public competicion(Long id, String nombre, Integer montoPremio, LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = id;
        this.nombre = nombre;
        this.montoPremio = montoPremio;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
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

    public Integer getMontoPremio() {
        return montoPremio;
    }

    public void setMontoPremio(Integer montoPremio) {
        this.montoPremio = montoPremio;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    /**
     * Valida que la fecha de finalización no sea cronológicamente anterior a la fecha de inicio.
     * @return true si el rango es válido o incompleto, false si fechaFin < fechaInicio
     */
    public boolean isRangoFechasValido() {
        if (fechaInicio != null && fechaFin != null) {
            return !fechaFin.isBefore(fechaInicio);
        }
        return true;
    }

    @Override
    public String toString() {
        return "competicion{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", montoPremio=" + montoPremio +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                '}';
    }
}
