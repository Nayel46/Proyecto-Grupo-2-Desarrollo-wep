
package com.healthnow.controller;

import com.healthnow.domain.Especialidad;
import com.healthnow.domain.Usuario;
import com.healthnow.service.EspecialidadService;
import com.healthnow.service.UsuarioService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/especialidad")
public class EspecialidadController {

    private final EspecialidadService especialidadService;
    private final UsuarioService usuarioService;

    public EspecialidadController(
            EspecialidadService especialidadService,
            UsuarioService usuarioService) {

        this.especialidadService = especialidadService;
        this.usuarioService = usuarioService;
    }


    @GetMapping("/listado")
    public String listado(Model model) {

        List<Especialidad> especialidades =
                especialidadService.getEspecialidades();

        model.addAttribute("especialidades", especialidades);

        model.addAttribute(
                "totalEspecialidades",
                especialidades.size()
        );

        return "especialidad/listado";
    }


    @GetMapping("/buscar")
    public String buscar(
            @RequestParam(required = false) String nombre,
            Model model) {

        List<Especialidad> especialidades =
                especialidadService.buscarEspecialidades(nombre);

        model.addAttribute("especialidades", especialidades);

        model.addAttribute(
                "totalEspecialidades",
                especialidades.size()
        );

        model.addAttribute("nombreBusqueda", nombre);

        return "especialidad/listado";
    }

    @GetMapping("/medicos/{id}")
    public String mostrarMedicos(
            @PathVariable Integer id,
            Model model) {

        Especialidad especialidad =
                especialidadService.getEspecialidad(id);

        // VALIDAR QUE LA ESPECIALIDAD EXISTA
        // Y ESTÉ ACTIVA
        if (especialidad == null || !especialidad.isActivo()) {
            return "redirect:/especialidad/listado";
        }


        List<Usuario> medicos =
                usuarioService.buscarMedicosPorEspecialidad(id);

        model.addAttribute("especialidad", especialidad);
        model.addAttribute("medicos", medicos);
        model.addAttribute("totalMedicos", medicos.size());


        return "especialidad/listado";
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        model.addAttribute(
                "especialidad",
                new Especialidad()
        );

        return "especialidad/modifica";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Especialidad especialidad,
            RedirectAttributes redirectAttributes) {

        especialidadService.guardar(especialidad);

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Especialidad guardada correctamente"
        );

        return "redirect:/especialidad/listado";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/editar/{id}")
    public String editar(
            @PathVariable Integer id,
            Model model) {

        Especialidad especialidad =
                especialidadService.getEspecialidad(id);

        if (especialidad == null) {
            return "redirect:/especialidad/listado";
        }

        model.addAttribute("especialidad", especialidad);

        return "especialidad/modifica";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/eliminar/{id}")
    public String eliminar(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        especialidadService.eliminar(id);

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Especialidad desactivada correctamente"
        );

        return "redirect:/especialidad/listado";
    }
}
