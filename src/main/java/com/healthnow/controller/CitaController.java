package com.healthnow.controller;

import com.healthnow.domain.Cita;
import com.healthnow.domain.Usuario;
import com.healthnow.repository.UsuarioRepository;
import com.healthnow.service.CitaService;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cita")
public class CitaController {

    private final CitaService citaService;
    private final UsuarioRepository usuarioRepository;

    public CitaController(CitaService citaService,
                          UsuarioRepository usuarioRepository) {
        this.citaService = citaService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/listado")
    @PreAuthorize("hasAnyRole('PACIENTE', 'DOCTOR')")
    public String listado(Model model, Principal principal) {

        Usuario usuario = obtenerUsuarioActual(principal);

        if ("DOCTOR".equalsIgnoreCase(usuario.getRol())) {
            model.addAttribute("citas",
                    citaService.listarPorDoctor(usuario.getIdUsuario()));
        } else {
            model.addAttribute("citas",
                    citaService.listarPorPaciente(usuario.getIdUsuario()));
        }

        model.addAttribute("rolActual", usuario.getRol());

        return "cita/listado";
    }

    @GetMapping("/agendar")
    @PreAuthorize("hasRole('PACIENTE')")
    public String formularioAgendar(Model model) {

        model.addAttribute("cita", new Cita());
        model.addAttribute("doctores",
                usuarioRepository
                        .findByRolAndActivoTrueOrderByNombreAscApellidosAsc(
                                "DOCTOR"));
        model.addAttribute("modoEdicion", false);

        return "cita/formulario";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('PACIENTE')")
    public String guardar(
            @RequestParam Integer idDoctor,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime hora,
            @RequestParam String motivo,
            @RequestParam String modalidad,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Usuario paciente = obtenerUsuarioActual(principal);

        try {
            citaService.agendar(
                    paciente.getIdUsuario(),
                    idDoctor,
                    fecha,
                    hora,
                    motivo,
                    modalidad
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje", "Cita agendada correctamente.");

            return "redirect:/cita/listado";

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error", ex.getMessage());

            return "redirect:/cita/agendar";
        }
    }

    @GetMapping("/editar/{idCita}")
    @PreAuthorize("hasRole('PACIENTE')")
    public String formularioEditar(
            @PathVariable Integer idCita,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Usuario paciente = obtenerUsuarioActual(principal);

        try {
            Cita cita = citaService.buscarPorId(idCita);

            if (!cita.getPaciente().getIdUsuario()
                    .equals(paciente.getIdUsuario())) {
                throw new IllegalArgumentException(
                        "No tiene permiso para editar esta cita.");
            }

            if ("CANCELADA".equalsIgnoreCase(cita.getEstado())
                    || "ATENDIDA".equalsIgnoreCase(cita.getEstado())) {
                throw new IllegalArgumentException(
                        "Esta cita ya no puede editarse.");
            }

            model.addAttribute("cita", cita);
            model.addAttribute("doctores",
                    usuarioRepository
                            .findByRolAndActivoTrueOrderByNombreAscApellidosAsc(
                                    "DOCTOR"));
            model.addAttribute("modoEdicion", true);

            return "cita/formulario";

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error", ex.getMessage());

            return "redirect:/cita/listado";
        }
    }

    @PostMapping("/actualizar/{idCita}")
    @PreAuthorize("hasRole('PACIENTE')")
    public String actualizar(
            @PathVariable Integer idCita,
            @RequestParam Integer idDoctor,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime hora,
            @RequestParam String motivo,
            @RequestParam String modalidad,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Usuario paciente = obtenerUsuarioActual(principal);

        try {
            citaService.editar(
                    idCita,
                    paciente.getIdUsuario(),
                    idDoctor,
                    fecha,
                    hora,
                    motivo,
                    modalidad
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje", "Cita actualizada correctamente.");

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error", ex.getMessage());
        }

        return "redirect:/cita/listado";
    }

    @PostMapping("/cancelar/{idCita}")
    @PreAuthorize("hasRole('PACIENTE')")
    public String cancelar(
            @PathVariable Integer idCita,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Usuario paciente = obtenerUsuarioActual(principal);

        try {
            citaService.cancelar(idCita, paciente.getIdUsuario());

            redirectAttributes.addFlashAttribute(
                    "mensaje", "Cita cancelada correctamente.");

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error", ex.getMessage());
        }

        return "redirect:/cita/listado";
    }

    private Usuario obtenerUsuarioActual(Principal principal) {

        if (principal == null) {
            throw new IllegalArgumentException(
                    "Debe iniciar sesión.");
        }

        Usuario usuario =
                usuarioRepository.findByUsername(principal.getName());

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "No se encontró el usuario autenticado.");
        }

        return usuario;
    }
}