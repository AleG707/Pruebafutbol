package com.crudclubes_apirest.app.controladores;

import com.crudclubes_apirest.app.entidades.club;
import com.crudclubes_apirest.app.entidades.competicion;
import com.crudclubes_apirest.app.entidades.entrenador;
import com.crudclubes_apirest.app.entidades.jugador;
import com.crudclubes_apirest.app.repositorios.club_repositorio;
import com.crudclubes_apirest.app.servicios.SequenceGeneratorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controlador Web MVC para gestionar las vistas de Clubes, Entrenadores, Jugadores, Asociaciones y Competiciones con Thymeleaf y Bootstrap 5.
 */
@Controller
public class club_web {

    @Autowired
    private club_repositorio clubRepositorio;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    /**
     * Muestra el formulario principal para registrar un club (index.html).
     */
    @GetMapping({"/", "/index", "/club/nuevo"})
    public String mostrarFormulario(Model model) {
        if (!model.containsAttribute("club")) {
            club nuevoClub = new club();
            nuevoClub.setEntrenador(new entrenador());
            model.addAttribute("club", nuevoClub);
        }
        model.addAttribute("modoEdicion", false);
        model.addAttribute("totalClubes", clubRepositorio.count());
        model.addAttribute("paginaActiva", "formulario");
        return "index";
    }

    /**
     * Lista todos los clubes registrados en la base de datos (listar.html).
     */
    @GetMapping({"/listar", "/clubes", "/club/listar"})
    public String listarClubes(Model model) {
        List<club> listaClubes = clubRepositorio.findAll();
        model.addAttribute("clubes", listaClubes);
        model.addAttribute("totalClubes", listaClubes.size());
        model.addAttribute("paginaActiva", "listar");
        return "listar";
    }

    /**
     * Procesa el formulario para guardar o actualizar los datos básicos de un club, su entrenador y asociación.
     */
    @PostMapping({"/guardar", "/club/guardar"})
    public String guardarClub(@Valid @ModelAttribute("club") club clubObj,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        // Si los campos del entrenador se dejaron completamente vacíos, se considera nulo (opcional)
        if (clubObj.getEntrenador() != null &&
            (clubObj.getEntrenador().getNombre() == null || clubObj.getEntrenador().getNombre().trim().isEmpty()) &&
            (clubObj.getEntrenador().getApellido() == null || clubObj.getEntrenador().getApellido().trim().isEmpty()) &&
            clubObj.getEntrenador().getEdad() == null &&
            (clubObj.getEntrenador().getNacionalidad() == null || clubObj.getEntrenador().getNacionalidad().trim().isEmpty())) {
            clubObj.setEntrenador(null);
        }

        // Si los campos de asociación se dejaron completamente vacíos, se considera nula (opcional)
        if (clubObj.getAsociacion() != null &&
            (clubObj.getAsociacion().getNombre() == null || clubObj.getAsociacion().getNombre().trim().isEmpty()) &&
            (clubObj.getAsociacion().getPais() == null || clubObj.getAsociacion().getPais().trim().isEmpty()) &&
            (clubObj.getAsociacion().getPresidente() == null || clubObj.getAsociacion().getPresidente().trim().isEmpty())) {
            clubObj.setAsociacion(null);
        }

        if (result.hasErrors()) {
            model.addAttribute("modoEdicion", clubObj.getId() != null && clubObj.getId() > 0);
            model.addAttribute("totalClubes", clubRepositorio.count());
            model.addAttribute("paginaActiva", "formulario");
            return "index";
        }

        boolean esEdicion = (clubObj.getId() != null && clubObj.getId() > 0);
        if (esEdicion) {
            // Preservar listas embebidas de jugadores y competiciones existentes al editar datos generales
            Optional<club> clubExistente = clubRepositorio.findById(clubObj.getId());
            if (clubExistente.isPresent()) {
                clubObj.setJugadores(clubExistente.get().getJugadores());
                clubObj.setCompeticiones(clubExistente.get().getCompeticiones());
            }
            clubRepositorio.save(clubObj);
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Club #" + clubObj.getId() + " ('" + clubObj.getNombre() + "') actualizado con éxito!");
        } else {
            clubObj.setId(sequenceGeneratorService.generateSequence(club.SEQUENCE_NAME));
            club guardado = clubRepositorio.save(clubObj);
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Club '" + guardado.getNombre() + "' registrado exitosamente con ID #" + guardado.getId() + " en MongoDB Atlas!");
        }

        return "redirect:/listar";
    }

    /**
     * Carga los datos de un club en el formulario para editarlo.
     */
    @GetMapping({"/editar/{id}", "/club/editar/{id}"})
    public String editarClub(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isPresent()) {
            club c = clubOpt.get();
            model.addAttribute("club", c);
            model.addAttribute("modoEdicion", true);
            model.addAttribute("totalClubes", clubRepositorio.count());
            model.addAttribute("paginaActiva", "formulario");
            return "index";
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "El club con ID #" + id + " no fue encontrado.");
            return "redirect:/listar";
        }
    }

    /**
     * Elimina un club completo por su ID.
     */
    @GetMapping({"/eliminar/{id}", "/club/eliminar/{id}"})
    public String eliminarClub(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        if (clubRepositorio.existsById(id)) {
            clubRepositorio.deleteById(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Club #" + id + " eliminado con éxito de MongoDB Atlas.");
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo eliminar: el club #" + id + " no existe.");
        }
        return "redirect:/listar";
    }

    /**
     * Muestra la vista detallada de un club con su entrenador, asociación, jugadores y competiciones.
     */
    @GetMapping({"/detalle/{id}", "/club/{id}"})
    public String verDetalle(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isPresent()) {
            model.addAttribute("club", clubOpt.get());
            model.addAttribute("paginaActiva", "detalle");
            return "detalle";
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Club con ID #" + id + " no encontrado.");
            return "redirect:/listar";
        }
    }

    /**
     * Busca clubes por coincidencia de nombre.
     */
    @GetMapping({"/buscar", "/club/buscar"})
    public String buscarClubes(@RequestParam(value = "nombre", required = false) String nombre, Model model) {
        List<club> resultados;
        if (nombre != null && !nombre.trim().isEmpty()) {
            resultados = clubRepositorio.findByNombreContainingIgnoreCase(nombre.trim());
        } else {
            resultados = clubRepositorio.findAll();
        }
        model.addAttribute("clubes", resultados);
        model.addAttribute("totalClubes", resultados.size());
        model.addAttribute("terminoBusqueda", nombre);
        model.addAttribute("paginaActiva", "listar");
        return "listar";
    }

    // =========================================================================
    // SECCIÓN JUGADORES
    // =========================================================================

    /**
     * Muestra el formulario para registrar un nuevo jugador en un club.
     */
    @GetMapping("/club/{id}/jugador/nuevo")
    public String nuevoJugador(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Club #" + id + " no encontrado.");
            return "redirect:/listar";
        }
        model.addAttribute("clubId", id);
        model.addAttribute("club", clubOpt.get());
        model.addAttribute("jugador", new jugador());
        model.addAttribute("modoEdicion", false);
        return "jugador_form";
    }

    /**
     * Muestra el formulario para editar un jugador existente de un club.
     */
    @GetMapping("/club/{id}/jugador/editar/{jugadorId}")
    public String editarJugador(@PathVariable("id") Long id,
                                @PathVariable("jugadorId") Long jugadorId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Club #" + id + " no encontrado.");
            return "redirect:/listar";
        }
        club c = clubOpt.get();
        Optional<jugador> jugadorOpt = c.getJugadorById(jugadorId);
        if (jugadorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Jugador #" + jugadorId + " no encontrado en el club.");
            return "redirect:/detalle/" + id;
        }

        model.addAttribute("clubId", id);
        model.addAttribute("club", c);
        model.addAttribute("jugador", jugadorOpt.get());
        model.addAttribute("modoEdicion", true);
        return "jugador_form";
    }

    /**
     * Guarda o actualiza un jugador dentro de la lista embebida del club.
     */
    @PostMapping("/club/{id}/jugador/guardar")
    public String guardarJugador(@PathVariable("id") Long id,
                                 @Valid @ModelAttribute("jugador") jugador jugadorObj,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Club #" + id + " no encontrado.");
            return "redirect:/listar";
        }
        club c = clubOpt.get();

        if (result.hasErrors()) {
            model.addAttribute("clubId", id);
            model.addAttribute("club", c);
            model.addAttribute("modoEdicion", jugadorObj.getId() != null && jugadorObj.getId() > 0);
            return "jugador_form";
        }

        if (jugadorObj.getId() == null || jugadorObj.getId() <= 0) {
            jugadorObj.setId(sequenceGeneratorService.generateSequence(jugador.SEQUENCE_NAME));
            c.addJugador(jugadorObj);
            redirectAttributes.addFlashAttribute("mensajeExito", "Jugador '" + jugadorObj.getNombreCompleto() + "' agregado con éxito al plantel.");
        } else {
            // Actualizar jugador en lista
            for (int i = 0; i < c.getJugadores().size(); i++) {
                if (Objects.equals(c.getJugadores().get(i).getId(), jugadorObj.getId())) {
                    c.getJugadores().set(i, jugadorObj);
                    break;
                }
            }
            redirectAttributes.addFlashAttribute("mensajeExito", "Jugador #" + jugadorObj.getId() + " actualizado correctamente.");
        }

        clubRepositorio.save(c);
        return "redirect:/detalle/" + id;
    }

    /**
     * Elimina un jugador de la lista embebida del club.
     */
    @GetMapping("/club/{id}/jugador/eliminar/{jugadorId}")
    public String eliminarJugador(@PathVariable("id") Long id,
                                  @PathVariable("jugadorId") Long jugadorId,
                                  RedirectAttributes redirectAttributes) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isPresent()) {
            club c = clubOpt.get();
            boolean eliminado = c.removeJugador(jugadorId);
            if (eliminado) {
                clubRepositorio.save(c);
                redirectAttributes.addFlashAttribute("mensajeExito", "Jugador #" + jugadorId + " removido del plantel.");
            } else {
                redirectAttributes.addFlashAttribute("mensajeError", "No se encontró el jugador #" + jugadorId + " en el club.");
            }
        }
        return "redirect:/detalle/" + id;
    }

    // =========================================================================
    // SECCIÓN COMPETICIONES
    // =========================================================================

    /**
     * Muestra el formulario para inscribir una nueva competición al club.
     */
    @GetMapping("/club/{id}/competicion/nuevo")
    public String nuevaCompeticion(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Club #" + id + " no encontrado.");
            return "redirect:/listar";
        }
        model.addAttribute("clubId", id);
        model.addAttribute("club", clubOpt.get());
        model.addAttribute("competicion", new competicion());
        model.addAttribute("modoEdicion", false);
        return "competicion_form";
    }

    /**
     * Muestra el formulario para editar una competición asignada a un club.
     */
    @GetMapping("/club/{id}/competicion/editar/{competicionId}")
    public String editarCompeticion(@PathVariable("id") Long id,
                                    @PathVariable("competicionId") Long competicionId,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Club #" + id + " no encontrado.");
            return "redirect:/listar";
        }
        club c = clubOpt.get();
        Optional<competicion> competicionOpt = c.getCompeticionById(competicionId);
        if (competicionOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Competición #" + competicionId + " no encontrada en el club.");
            return "redirect:/detalle/" + id;
        }

        model.addAttribute("clubId", id);
        model.addAttribute("club", c);
        model.addAttribute("competicion", competicionOpt.get());
        model.addAttribute("modoEdicion", true);
        return "competicion_form";
    }

    /**
     * Guarda o actualiza una competición dentro de la lista embebida del club.
     */
    @PostMapping("/club/{id}/competicion/guardar")
    public String guardarCompeticion(@PathVariable("id") Long id,
                                     @Valid @ModelAttribute("competicion") competicion competicionObj,
                                     BindingResult result,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Club #" + id + " no encontrado.");
            return "redirect:/listar";
        }
        club c = clubOpt.get();

        // Validación de coherencia de fechas: fechaFin no anterior a fechaInicio
        if (competicionObj.getFechaInicio() != null && competicionObj.getFechaFin() != null &&
                competicionObj.getFechaFin().isBefore(competicionObj.getFechaInicio())) {
            result.rejectValue("fechaFin", "error.competicion", "La fecha de finalización no puede ser anterior a la fecha de inicio");
        }

        if (result.hasErrors()) {
            model.addAttribute("clubId", id);
            model.addAttribute("club", c);
            model.addAttribute("modoEdicion", competicionObj.getId() != null && competicionObj.getId() > 0);
            return "competicion_form";
        }

        if (competicionObj.getId() == null || competicionObj.getId() <= 0) {
            competicionObj.setId(sequenceGeneratorService.generateSequence(competicion.SEQUENCE_NAME));
            c.addCompeticion(competicionObj);
            redirectAttributes.addFlashAttribute("mensajeExito", "Competición '" + competicionObj.getNombre() + "' vinculada con éxito al club.");
        } else {
            // Actualizar competición en lista
            for (int i = 0; i < c.getCompeticiones().size(); i++) {
                if (Objects.equals(c.getCompeticiones().get(i).getId(), competicionObj.getId())) {
                    c.getCompeticiones().set(i, competicionObj);
                    break;
                }
            }
            redirectAttributes.addFlashAttribute("mensajeExito", "Competición #" + competicionObj.getId() + " actualizada correctamente.");
        }

        clubRepositorio.save(c);
        return "redirect:/detalle/" + id;
    }

    /**
     * Elimina una competición de la lista embebida del club.
     */
    @GetMapping("/club/{id}/competicion/eliminar/{competicionId}")
    public String eliminarCompeticion(@PathVariable("id") Long id,
                                      @PathVariable("competicionId") Long competicionId,
                                      RedirectAttributes redirectAttributes) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isPresent()) {
            club c = clubOpt.get();
            boolean eliminada = c.removeCompeticion(competicionId);
            if (eliminada) {
                clubRepositorio.save(c);
                redirectAttributes.addFlashAttribute("mensajeExito", "Competición #" + competicionId + " desvinculada del club.");
            } else {
                redirectAttributes.addFlashAttribute("mensajeError", "No se encontró la competición #" + competicionId + " en el club.");
            }
        }
        return "redirect:/detalle/" + id;
    }

    // =========================================================================
    // DESVINCULAR ENTRENADOR / ASOCIACIÓN
    // =========================================================================

    /**
     * Quita el entrenador asignado a un club.
     */
    @GetMapping("/club/{id}/entrenador/eliminar")
    public String eliminarEntrenador(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isPresent()) {
            club c = clubOpt.get();
            c.setEntrenador(null);
            clubRepositorio.save(c);
            redirectAttributes.addFlashAttribute("mensajeExito", "Entrenador desvinculado del club.");
        }
        return "redirect:/detalle/" + id;
    }

    /**
     * Quita la asociación asignada a un club.
     */
    @GetMapping("/club/{id}/asociacion/eliminar")
    public String eliminarAsociacion(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<club> clubOpt = clubRepositorio.findById(id);
        if (clubOpt.isPresent()) {
            club c = clubOpt.get();
            c.setAsociacion(null);
            clubRepositorio.save(c);
            redirectAttributes.addFlashAttribute("mensajeExito", "Asociación desvinculada del club.");
        }
        return "redirect:/detalle/" + id;
    }
}
