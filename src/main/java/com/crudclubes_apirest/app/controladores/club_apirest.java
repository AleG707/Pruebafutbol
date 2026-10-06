package com.crudclubes_apirest.app.controladores;

import com.crudclubes_apirest.app.entidades.asociacion;
import com.crudclubes_apirest.app.entidades.club;
import com.crudclubes_apirest.app.entidades.competicion;
import com.crudclubes_apirest.app.entidades.entrenador;
import com.crudclubes_apirest.app.entidades.jugador;
import com.crudclubes_apirest.app.repositorios.club_repositorio;
import com.crudclubes_apirest.app.servicios.SequenceGeneratorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Controlador API REST para la gestión integral de Clubes, Entrenadores, Jugadores, Asociaciones y Competiciones.
 * Implementa códigos de estado HTTP estándar (201 Created, 204 No Content, 404 Not Found, 400 Bad Request, 200 OK).
 */
@RestController
@RequestMapping("/api/clubes")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class club_apirest {

    @Autowired
    private club_repositorio clubRepositorio;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    // =========================================================================
    // ENDPOINTS CRUD CLUBES (DOCUMENTO RAÍZ)
    // =========================================================================

    /**
     * GET /api/clubes
     * Retorna la lista completa de clubes con todas sus entidades embebidas en formato JSON.
     */
    @GetMapping
    public ResponseEntity<List<club>> listarTodos() {
        List<club> clubes = clubRepositorio.findAll();
        return ResponseEntity.ok(clubes);
    }

    /**
     * GET /api/clubes/{id}
     * Retorna un club específico por su ID o 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable("id") Long id) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isPresent()) {
            return ResponseEntity.ok(clubOpt.get());
        } else {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Club con ID #" + id + " no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * POST /api/clubes
     * Crea un nuevo club con ID incremental generado por secuencia en MongoDB Atlas.
     * Genera automáticamente secuencias para jugadores y competiciones si se envían en el cuerpo.
     */
    @PostMapping
    public ResponseEntity<?> crearClub(@Valid @RequestBody club nuevoClub, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
        }

        // Asignar ID autoincremental al club raíz
        nuevoClub.setId(sequenceGeneratorService.generateSequence(club.SEQUENCE_NAME));

        // Asignar IDs a jugadores embebidos si no tienen
        if (nuevoClub.getJugadores() != null) {
            for (jugador j : nuevoClub.getJugadores()) {
                if (j.getId() == null || j.getId() <= 0) {
                    j.setId(sequenceGeneratorService.generateSequence(jugador.SEQUENCE_NAME));
                }
            }
        }

        // Validar y asignar IDs a competiciones embebidas
        if (nuevoClub.getCompeticiones() != null) {
            for (competicion c : nuevoClub.getCompeticiones()) {
                if (!c.isRangoFechasValido()) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("status", HttpStatus.BAD_REQUEST.value());
                    error.put("mensaje", "Error en competición '" + c.getNombre() + "': la fecha de fin no puede ser anterior a la de inicio");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
                if (c.getId() == null || c.getId() <= 0) {
                    c.setId(sequenceGeneratorService.generateSequence(competicion.SEQUENCE_NAME));
                }
            }
        }

        club guardado = clubRepositorio.save(nuevoClub);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    /**
     * PUT /api/clubes/{id}
     * Actualiza la información completa de un club existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarClub(@PathVariable("id") Long id,
                                            @Valid @RequestBody club clubData,
                                            BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
        }

        Optional<club> clubExistente = clubRepositorio.findById(id);
        if (clubExistente.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "No se puede actualizar. Club con ID #" + id + " no existe");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        club c = clubExistente.get();
        c.setNombre(clubData.getNombre());
        if (clubData.getEntrenador() != null) {
            c.setEntrenador(clubData.getEntrenador());
        }
        if (clubData.getAsociacion() != null) {
            c.setAsociacion(clubData.getAsociacion());
        }
        if (clubData.getJugadores() != null && !clubData.getJugadores().isEmpty()) {
            c.setJugadores(clubData.getJugadores());
        }
        if (clubData.getCompeticiones() != null && !clubData.getCompeticiones().isEmpty()) {
            c.setCompeticiones(clubData.getCompeticiones());
        }

        club actualizado = clubRepositorio.save(c);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * DELETE /api/clubes/{id}
     * Elimina un club completo de MongoDB Atlas y devuelve código HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarClub(@PathVariable("id") Long id) {
        if (!clubRepositorio.existsById(id)) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Club con ID #" + id + " no encontrado para eliminar");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        clubRepositorio.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/clubes/buscar?nombre=...
     * Filtra clubes por coincidencia en el nombre.
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<club>> buscarPorNombre(@RequestParam(value = "nombre", defaultValue = "") String nombre) {
        List<club> resultados = clubRepositorio.findByNombreContainingIgnoreCase(nombre);
        return ResponseEntity.ok(resultados);
    }

    // =========================================================================
    // ENDPOINTS GESTIÓN DEL ENTRENADOR (@OneToOne)
    // =========================================================================

    /**
     * PUT /api/clubes/{id}/entrenador
     * Asigna o actualiza el entrenador del club.
     */
    @PutMapping("/{id}/entrenador")
    public ResponseEntity<?> actualizarEntrenador(@PathVariable("id") Long id,
                                                  @Valid @RequestBody entrenador entrenadorData,
                                                  BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
        }

        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Club con ID #" + id + " no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        club c = clubOpt.get();
        c.setEntrenador(entrenadorData);
        clubRepositorio.save(c);
        return ResponseEntity.ok(c);
    }

    /**
     * DELETE /api/clubes/{id}/entrenador
     * Desvincula el entrenador del club y devuelve 204 No Content.
     */
    @DeleteMapping("/{id}/entrenador")
    public ResponseEntity<?> eliminarEntrenador(@PathVariable("id") Long id) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Club con ID #" + id + " no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        club c = clubOpt.get();
        c.setEntrenador(null);
        clubRepositorio.save(c);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // ENDPOINTS GESTIÓN DE LA ASOCIACIÓN (@ManyToOne)
    // =========================================================================

    /**
     * PUT /api/clubes/{id}/asociacion
     * Asigna o actualiza la asociación rectora del club.
     */
    @PutMapping("/{id}/asociacion")
    public ResponseEntity<?> actualizarAsociacion(@PathVariable("id") Long id,
                                                  @Valid @RequestBody asociacion asociacionData,
                                                  BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
        }

        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Club con ID #" + id + " no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        club c = clubOpt.get();
        c.setAsociacion(asociacionData);
        clubRepositorio.save(c);
        return ResponseEntity.ok(c);
    }

    /**
     * DELETE /api/clubes/{id}/asociacion
     * Desvincula la asociación del club y devuelve 204 No Content.
     */
    @DeleteMapping("/{id}/asociacion")
    public ResponseEntity<?> eliminarAsociacion(@PathVariable("id") Long id) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Club con ID #" + id + " no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        club c = clubOpt.get();
        c.setAsociacion(null);
        clubRepositorio.save(c);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // ENDPOINTS GESTIÓN DE JUGADORES (@OneToMany Embebido)
    // =========================================================================

    /**
     * GET /api/clubes/{id}/jugadores
     * Retorna la lista de jugadores de un club.
     */
    @GetMapping("/{id}/jugadores")
    public ResponseEntity<?> listarJugadores(@PathVariable("id") Long id) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Club con ID #" + id + " no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(clubOpt.get().getJugadores());
    }

    /**
     * POST /api/clubes/{id}/jugadores
     * Agrega un nuevo jugador al plantel del club con ID incremental generado automáticamente.
     */
    @PostMapping("/{id}/jugadores")
    public ResponseEntity<?> agregarJugador(@PathVariable("id") Long id,
                                            @Valid @RequestBody jugador jugadorData,
                                            BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
        }

        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Club con ID #" + id + " no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        club c = clubOpt.get();
        jugadorData.setId(sequenceGeneratorService.generateSequence(jugador.SEQUENCE_NAME));
        c.addJugador(jugadorData);
        clubRepositorio.save(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(jugadorData);
    }

    /**
     * PUT /api/clubes/{id}/jugadores/{jugadorId}
     * Actualiza los datos de un jugador dentro del plantel del club.
     */
    @PutMapping("/{id}/jugadores/{jugadorId}")
    public ResponseEntity<?> actualizarJugador(@PathVariable("id") Long id,
                                               @PathVariable("jugadorId") Long jugadorId,
                                               @Valid @RequestBody jugador jugadorData,
                                               BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
        }

        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Club con ID #" + id + " no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        club c = clubOpt.get();
        Optional<jugador> jugadorExistente = c.getJugadorById(jugadorId);
        if (jugadorExistente.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Jugador con ID #" + jugadorId + " no encontrado en el club");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        jugador j = jugadorExistente.get();
        j.setNombre(jugadorData.getNombre());
        j.setApellido(jugadorData.getApellido());
        j.setNumero(jugadorData.getNumero());
        j.setPosicion(jugadorData.getPosicion());

        clubRepositorio.save(c);
        return ResponseEntity.ok(j);
    }

    /**
     * DELETE /api/clubes/{id}/jugadores/{jugadorId}
     * Elimina un jugador del club y devuelve 204 No Content.
     */
    @DeleteMapping("/{id}/jugadores/{jugadorId}")
    public ResponseEntity<?> eliminarJugador(@PathVariable("id") Long id,
                                            @PathVariable("jugadorId") Long jugadorId) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Club con ID #" + id + " no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        club c = clubOpt.get();
        boolean eliminado = c.removeJugador(jugadorId);
        if (!eliminado) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Jugador con ID #" + jugadorId + " no encontrado en el club");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        clubRepositorio.save(c);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // ENDPOINTS GESTIÓN DE COMPETICIONES (@ManyToMany Embebido)
    // =========================================================================

    /**
     * GET /api/clubes/{id}/competiciones
     * Retorna la lista de competiciones en las que participa el club.
     */
    @GetMapping("/{id}/competiciones")
    public ResponseEntity<?> listarCompeticiones(@PathVariable("id") Long id) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Club con ID #" + id + " no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(clubOpt.get().getCompeticiones());
    }

    /**
     * POST /api/clubes/{id}/competiciones
     * Registra una nueva competición para el club con ID incremental.
     */
    @PostMapping("/{id}/competiciones")
    public ResponseEntity<?> agregarCompeticion(@PathVariable("id") Long id,
                                                @Valid @RequestBody competicion competicionData,
                                                BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
        }

        if (!competicionData.isRangoFechasValido()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("mensaje", "La fecha de fin no puede ser cronológicamente anterior a la fecha de inicio");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Club con ID #" + id + " no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        club c = clubOpt.get();
        competicionData.setId(sequenceGeneratorService.generateSequence(competicion.SEQUENCE_NAME));
        c.addCompeticion(competicionData);
        clubRepositorio.save(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(competicionData);
    }

    /**
     * PUT /api/clubes/{id}/competiciones/{competicionId}
     * Actualiza los datos de una competición vinculada a un club.
     */
    @PutMapping("/{id}/competiciones/{competicionId}")
    public ResponseEntity<?> actualizarCompeticion(@PathVariable("id") Long id,
                                                   @PathVariable("competicionId") Long competicionId,
                                                   @Valid @RequestBody competicion competicionData,
                                                   BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
        }

        if (!competicionData.isRangoFechasValido()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("mensaje", "La fecha de fin no puede ser cronológicamente anterior a la fecha de inicio");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Club con ID #" + id + " no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        club c = clubOpt.get();
        Optional<competicion> compExistente = c.getCompeticionById(competicionId);
        if (compExistente.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Competición con ID #" + competicionId + " no encontrada en el club");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        competicion comp = compExistente.get();
        comp.setNombre(competicionData.getNombre());
        comp.setMontoPremio(competicionData.getMontoPremio());
        comp.setFechaInicio(competicionData.getFechaInicio());
        comp.setFechaFin(competicionData.getFechaFin());

        clubRepositorio.save(c);
        return ResponseEntity.ok(comp);
    }

    /**
     * DELETE /api/clubes/{id}/competiciones/{competicionId}
     * Elimina una competición asignada a un club y retorna 204 No Content.
     */
    @DeleteMapping("/{id}/competiciones/{competicionId}")
    public ResponseEntity<?> eliminarCompeticion(@PathVariable("id") Long id,
                                                 @PathVariable("competicionId") Long competicionId) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Club con ID #" + id + " no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        club c = clubOpt.get();
        boolean eliminada = c.removeCompeticion(competicionId);
        if (!eliminada) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("mensaje", "Competición con ID #" + competicionId + " no encontrada en el club");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        clubRepositorio.save(c);
        return ResponseEntity.noContent().build();
    }
}
