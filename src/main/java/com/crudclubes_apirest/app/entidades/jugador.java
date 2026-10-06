package com.crudclubes_apirest.app.entidades;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Transient;
import java.io.Serializable;

/**
 * Entidad 'jugador' que representa a un futbolista perteneciente al plantel de un club.
 * 
 * COMENTARIO DE ARQUITECTURA JPA vs MONGODB:
 * - Equivalente JPA/Relacional: Anotación @OneToMany con relación bidireccional @ManyToOne y tabla propia 'jugadores' vinculada por 'club_id'.
 * - Justificación en MongoDB: Se modela como un arreglo embebido dentro de 'club' porque un club contiene un número acotado de jugadores (~25-40).
 *   Esto mantiene la coherencia y localidad espacial de los datos, eliminando la sobrecarga de consultas adicionales y simplificando el ciclo de vida.
 */
public class jugador implements Serializable {

    private static final long serialVersionUID = 1L;

    @Transient
    public static final String SEQUENCE_NAME = "jugador_sequence";

    private Long id;

    @NotBlank(message = "El nombre del jugador es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido del jugador es obligatorio")
    private String apellido;

    @NotNull(message = "El número de camiseta es obligatorio")
    @Min(value = 1, message = "El número de camiseta debe ser mínimo 1")
    @Max(value = 99, message = "El número de camiseta debe ser máximo 99")
    private Integer numero;

    @NotBlank(message = "La posición del jugador es obligatoria")
    private String posicion;

    public jugador() {
    }

    public jugador(String nombre, String apellido, Integer numero, String posicion) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.numero = numero;
        this.posicion = posicion;
    }

    public jugador(Long id, String nombre, String apellido, Integer numero, String posicion) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.numero = numero;
        this.posicion = posicion;
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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getPosicion() {
        return posicion;
    }

    public void setPosicion(String posicion) {
        this.posicion = posicion;
    }

    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }

    @Override
    public String toString() {
        return "jugador{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", numero=" + numero +
                ", posicion='" + posicion + '\'' +
                '}';
    }
}
