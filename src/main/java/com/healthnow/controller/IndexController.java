
package com.healthnow.controller;

import com.healthnow.domain.Especialidad;
import com.healthnow.service.EspecialidadService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private final EspecialidadService especialidadService;

    public IndexController(EspecialidadService especialidadService) {
        this.especialidadService = especialidadService;
    }

    @GetMapping("/")
    public String cargarPaginaInicio(Model model) {

        List<Especialidad> especialidades =
                especialidadService.getEspecialidades();

        model.addAttribute("especialidades", especialidades);

        model.addAttribute(
                "totalEspecialidades",
                especialidades.size()
        );

        return "index";
    }
}
