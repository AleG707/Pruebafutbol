package com.crudclubes_apirest.app.entidades;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Entidad 'asociacion' que representa la federación o entidad rectora asociada a un club.
 * 
 * COMENTARIO DE ARQUITECTURA JPA vs MONGODB:
 * - Equivalente JPA/Relacional: Anotación @ManyToOne con clave foránea en la tabla del club apuntando a una tabla 'asociacion'.
 * - Justificación en MongoDB: Se embebe dentro del documento raíz 'club' para disponer de la información de la federación o liga 
 *   de manera inmediata y desnormalizada, optimizando las lecturas frecuentes sin incurrir en costos de unión relacional ($lookup).
 */
public class asociacion implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "El nombre de la asociación es obligatorio")
    private String nombre;

    @NotBlank(message = "El país de la asociación es obligatorio")
    private String pais;

    @NotBlank(message = "El presidente de la asociación es obligatorio")
    private String presidente;

    public asociacion() {
    }

    public asociacion(String nombre, String pais, String presidente) {
        this.nombre = nombre;
        this.pais = pais;
        this.presidente = presidente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getPresidente() {
        return presidente;
    }

    public void setPresidente(String presidente) {
        this.presidente = presidente;
    }

    @Override
    public String toString() {
        return "asociacion{" +
                "nombre='" + nombre + '\'' +
                ", pais='" + pais + '\'' +
                ", presidente='" + presidente + '\'' +
                '}';
    }
}
