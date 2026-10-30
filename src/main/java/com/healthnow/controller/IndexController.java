
package com.healthnow.controller;

import com.healthnow.domain.Cita;
import com.healthnow.domain.Especialidad;
import com.healthnow.domain.Usuario;
import com.healthnow.repository.UsuarioRepository;
import com.healthnow.service.CitaService;
import com.healthnow.service.EspecialidadService;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private final EspecialidadService especialidadService;
    private final CitaService citaService;
    private final UsuarioRepository usuarioRepository;

    public IndexController(
            EspecialidadService especialidadService,
            CitaService citaService,
            UsuarioRepository usuarioRepository) {

        this.especialidadService = especialidadService;
        this.citaService = citaService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/")
    public String cargarPaginaInicio(
            Model model,
            Principal principal) {

        // ESPECIALIDADES
        List<Especialidad> especialidades =
                especialidadService.getEspecialidades();

        model.addAttribute("especialidades", especialidades);
        model.addAttribute(
                "totalEspecialidades",
                especialidades.size()
        );

        // USUARIO QUE INICIÓ SESIÓN
        if (principal != null) {

            Usuario usuario =
                    usuarioRepository.findByUsername(
                            principal.getName());

            if (usuario != null) {

                model.addAttribute("usuarioActual", usuario);
                model.addAttribute("rolActual", usuario.getRol());

                // INFORMACIÓN DEL PANEL MÉDICO
                if ("DOCTOR".equalsIgnoreCase(usuario.getRol())) {

                    Integer idDoctor = usuario.getIdUsuario();

                    List<Cita> citasDelDia =
                            citaService.listarPorDoctorYFecha(
                                    idDoctor,
                                    LocalDate.now()
                            );

                    List<Cita> citasPendientes =
                            citaService.listarPendientesPorDoctor(
                                    idDoctor
                            );

                    model.addAttribute(
                            "citasDelDia",
                            citasDelDia
                    );

                    model.addAttribute(
                            "citasPendientes",
                            citasPendientes
                    );

                    model.addAttribute(
                            "totalCitasDelDia",
                            citasDelDia.size()
                    );

                    model.addAttribute(
                            "totalCitasPendientes",
                            citasPendientes.size()
                    );
                }
            }
        }

        return "index";
    }
}

