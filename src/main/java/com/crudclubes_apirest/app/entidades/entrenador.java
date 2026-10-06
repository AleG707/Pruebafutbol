package com.crudclubes_apirest.app.entidades;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Entidad 'entrenador' que representa al director técnico de un club de fútbol.
 * 
 * COMENTARIO DE ARQUITECTURA JPA vs MONGODB:
 * - Equivalente JPA/Relacional: Anotación @OneToOne (o @Embedded / @Embeddable) con clave foránea en la tabla del club.
 * - Justificación en MongoDB: Se embebe como un subdocumento dentro del documento 'club' porque la relación es de cardinalidad 1 a 1 
 *   y un entrenador pertenece directamente al ciclo de vida del club. Al estar embebido, se evita realizar un $lookup (JOIN) y 
 *   se obtienen todos los datos técnicos en una única consulta de alto rendimiento con garantía de atomicidad.
 */
public class entrenador implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "El nombre del entrenador es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido del entrenador es obligatorio")
    private String apellido;

    @NotNull(message = "La edad del entrenador es obligatoria")
    @Min(value = 18, message = "La edad del entrenador debe ser mínimo 18 años")
    @Max(value = 99, message = "La edad del entrenador debe ser máximo 99 años")
    private Integer edad;

    @NotBlank(message = "La nacionalidad del entrenador es obligatoria")
    private String nacionalidad;

    public entrenador() {
    }

    public entrenador(String nombre, String apellido, Integer edad, String nacionalidad) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.nacionalidad = nacionalidad;
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

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }

    @Override
    public String toString() {
        return "entrenador{" +
                "nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", edad=" + edad +
                ", nacionalidad='" + nacionalidad + '\'' +
                '}';
    }
}
