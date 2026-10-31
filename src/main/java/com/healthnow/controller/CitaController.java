
package com.healthnow.controller;

import com.healthnow.domain.Cita;
import com.healthnow.domain.Usuario;
import com.healthnow.repository.EspecialidadRepository;
import com.healthnow.repository.UsuarioRepository;
import com.healthnow.service.CitaService;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cita")
public class CitaController {

    private final CitaService citaService;
    private final UsuarioRepository usuarioRepository;
    private final EspecialidadRepository especialidadRepository;

    public CitaController(
            CitaService citaService,
            UsuarioRepository usuarioRepository,
            EspecialidadRepository especialidadRepository) {

        this.citaService = citaService;
        this.usuarioRepository = usuarioRepository;
        this.especialidadRepository = especialidadRepository;
    }

    @GetMapping("/listado")
    @PreAuthorize("hasAnyRole('PACIENTE', 'DOCTOR')")
    public String listado(Model model, Principal principal) {

        Usuario usuario = obtenerUsuarioActual(principal);

        if ("DOCTOR".equalsIgnoreCase(usuario.getRol())) {

            model.addAttribute(
                    "citas",
                    citaService.listarPorDoctor(usuario.getIdUsuario())
            );

        } else {

            model.addAttribute(
                    "citas",
                    citaService.listarPorPaciente(usuario.getIdUsuario())
            );
        }

        model.addAttribute("rolActual", usuario.getRol());

        return "cita/listado";
    }

    @GetMapping("/agendar")
    @PreAuthorize("hasRole('PACIENTE')")
    public String formularioAgendar(
            Model model,
            Principal principal) {

        Usuario paciente = obtenerUsuarioActual(principal);

        model.addAttribute("cita", new Cita());
        model.addAttribute("paciente", paciente);
        model.addAttribute(
                "especialidades",
                especialidadRepository.findAll()
        );
        model.addAttribute("doctores", List.of());
        model.addAttribute("modoEdicion", false);

        return "cita/modifica";
    }

    @GetMapping("/doctores")
    @PreAuthorize("hasRole('PACIENTE')")
    @ResponseBody
    public List<Map<String, Object>> obtenerDoctores(
            @RequestParam Integer idEspecialidad) {

        return usuarioRepository
                .buscarMedicosPorEspecialidad(idEspecialidad)
                .stream()
                .map(usuario -> Map.<String, Object>of(
                        "idUsuario", usuario.getIdUsuario(),
                        "nombreCompleto",
                        usuario.getNombre() + " " + usuario.getApellidos()
                ))
                .toList();
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('PACIENTE')")
    public String guardar(
            @RequestParam Integer idEspecialidad,
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

            validarEspecialidadDoctor(idEspecialidad, idDoctor);

            citaService.agendar(
                    paciente.getIdUsuario(),
                    idDoctor,
                    fecha,
                    hora,
                    motivo,
                    modalidad
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Cita agendada correctamente."
            );

            return "redirect:/cita/listado";

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );

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
                        "No tiene permiso para editar esta cita."
                );
            }

            if ("CANCELADA".equalsIgnoreCase(cita.getEstado())
                    || "ATENDIDA".equalsIgnoreCase(cita.getEstado())) {

                throw new IllegalArgumentException(
                        "Esta cita ya no puede editarse."
                );
            }

            Integer idEspecialidad =
                    usuarioRepository.obtenerIdEspecialidadMedico(
                            cita.getDoctor().getIdUsuario()
                    );

            if (idEspecialidad == null) {

                throw new IllegalArgumentException(
                        "No se encontró la especialidad del doctor."
                );
            }

            model.addAttribute("cita", cita);
            model.addAttribute("paciente", paciente);

            model.addAttribute(
                    "especialidades",
                    especialidadRepository.findAll()
            );

            model.addAttribute(
                    "doctores",
                    usuarioRepository.buscarMedicosPorEspecialidad(
                            idEspecialidad
                    )
            );

            model.addAttribute(
                    "idEspecialidadSeleccionada",
                    idEspecialidad
            );

            model.addAttribute("modoEdicion", true);

            return "cita/modifica";

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );

            return "redirect:/cita/listado";
        }
    }

    @PostMapping("/actualizar/{idCita}")
    @PreAuthorize("hasRole('PACIENTE')")
    public String actualizar(
            @PathVariable Integer idCita,
            @RequestParam Integer idEspecialidad,
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

            validarEspecialidadDoctor(idEspecialidad, idDoctor);

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
                    "mensaje",
                    "Cita actualizada correctamente."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
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

            citaService.cancelar(
                    idCita,
                    paciente.getIdUsuario()
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Cita cancelada correctamente."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/cita/listado";
    }

    @PostMapping("/confirmar/{idCita}")
    @PreAuthorize("hasRole('DOCTOR')")
    public String confirmarCita(
            @PathVariable Integer idCita,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Usuario doctor = obtenerUsuarioActual(principal);

        try {

            citaService.confirmarCita(
                    idCita,
                    doctor.getIdUsuario()
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Cita confirmada correctamente."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/";
    }

    @PostMapping("/cambiar-estado/{idCita}")
    @PreAuthorize("hasRole('DOCTOR')")
    public String cambiarEstadoCita(
            @PathVariable Integer idCita,
            @RequestParam String nuevoEstado,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Usuario doctor = obtenerUsuarioActual(principal);

        try {

            citaService.cambiarEstadoCita(
                    idCita,
                    doctor.getIdUsuario(),
                    nuevoEstado
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "El estado de la cita se actualizó correctamente."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/";
    }

    private void validarEspecialidadDoctor(
            Integer idEspecialidad,
            Integer idDoctor) {

        Usuario doctor = usuarioRepository.findById(idDoctor)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El doctor seleccionado no existe."
                        )
                );

        if (!doctor.isActivo()
                || !"DOCTOR".equalsIgnoreCase(doctor.getRol())) {

            throw new IllegalArgumentException(
                    "El doctor seleccionado no está disponible."
            );
        }

        Integer especialidadDoctor =
                usuarioRepository.obtenerIdEspecialidadMedico(idDoctor);

        if (especialidadDoctor == null
                || !especialidadDoctor.equals(idEspecialidad)) {

            throw new IllegalArgumentException(
                    "El doctor no pertenece a la especialidad seleccionada."
            );
        }

        if (especialidadRepository.findById(idEspecialidad)
                .filter(especialidad -> especialidad.isActivo())
                .isEmpty()) {

            throw new IllegalArgumentException(
                    "La especialidad seleccionada no está disponible."
            );
        }
    }

    private Usuario obtenerUsuarioActual(Principal principal) {

        if (principal == null) {

            throw new IllegalArgumentException(
                    "Debe iniciar sesión."
            );
        }

        Usuario usuario =
                usuarioRepository.findByUsername(principal.getName());

        if (usuario == null) {

            throw new IllegalArgumentException(
                    "No se encontró el usuario autenticado."
            );
        }

        return usuario;
    }
}
