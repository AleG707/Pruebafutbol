package com.crudclubes_apirest.app.repositorios;

import com.crudclubes_apirest.app.entidades.club;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data MongoDB para la entidad 'club'.
 * Proporciona métodos CRUD estándar y consultas de filtrado por nombre de club.
 */
@Repository
public interface club_repositorio extends MongoRepository<club, Long> {

    /**
     * Búsqueda por coincidencia parcial en el nombre del club (case-insensitive).
     * @param nombre texto a buscar
     * @return lista de clubes coincidentes
     */
    List<club> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Búsqueda por coincidencia exacta del nombre del club (case-insensitive).
     * @param nombre nombre exacto del club
     * @return Optional con el club si existe
     */
    Optional<club> findByNombreIgnoreCase(String nombre);

    /**
     * Comprueba si ya existe un club con el nombre especificado.
     * @param nombre nombre a validar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreIgnoreCase(String nombre);
}
