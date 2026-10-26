
package com.healthnow.controller;

import com.healthnow.domain.Usuario;
import com.healthnow.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String cargarPaginaLogin() {
        return "/login";
    }

    @GetMapping("/registro")
    public String cargarPaginaRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "/registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(
            @Valid @ModelAttribute("usuario") Usuario usuario,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "/registro";
        }

        try {
            usuario.setActivo(true);
            usuarioService.registrarUsuario(usuario);

            return "redirect:/login?registroExitoso";

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(
                    "error",
                    "El nombre de usuario, correo o cédula ya está registrado."
            );
            return "/registro";
        }
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(
            Authentication authentication,
            Model model) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Usuario usuario =
                usuarioService.getUsuarioPorUsername(
                        authentication.getName()
                );

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);

        if ("DOCTOR".equalsIgnoreCase(usuario.getRol())) {

            String especialidad =
                    usuarioService.obtenerEspecialidadDoctor(
                            usuario.getIdUsuario()
                    );

            model.addAttribute(
                    "especialidadMedico",
                    especialidad != null
                            ? especialidad
                            : "No asignada"
            );
        }

        return "usuario/perfil";
    }

    @GetMapping("/perfil/modificar")
    public String mostrarModificarPerfil(
            Authentication authentication,
            Model model) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Usuario usuario =
                usuarioService.getUsuarioPorUsername(
                        authentication.getName()
                );

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);

        return "usuario/modifica";
    }

    @PostMapping("/perfil/guardar")
    public String guardarPerfil(
            @ModelAttribute("usuario") Usuario datosActualizados,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();

        if (datosActualizados.getNombre() == null
                || datosActualizados.getNombre().isBlank()
                || datosActualizados.getApellidos() == null
                || datosActualizados.getApellidos().isBlank()
                || datosActualizados.getCorreo() == null
                || datosActualizados.getCorreo().isBlank()) {

            model.addAttribute(
                    "error",
                    "El nombre, los apellidos y el correo son obligatorios."
            );

            return "usuario/modifica";
        }

        if (!datosActualizados.getCorreo().matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            model.addAttribute(
                    "error",
                    "Ingrese un correo electrónico válido."
            );

            return "usuario/modifica";
        }

        if (datosActualizados.getNombre().length() > 60
                || datosActualizados.getApellidos().length() > 100
                || datosActualizados.getCorreo().length() > 150
                || (datosActualizados.getTelefono() != null
                && datosActualizados.getTelefono().length() > 25)) {

            model.addAttribute(
                    "error",
                    "Uno de los campos supera la longitud permitida."
            );

            return "usuario/modifica";
        }

        try {

            usuarioService.actualizarPerfil(
                    username,
                    datosActualizados
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "La información personal se modificó correctamente."
            );

            return "redirect:/perfil";

        } catch (DataIntegrityViolationException e) {

            model.addAttribute(
                    "error",
                    "El correo electrónico ya está registrado por otro usuario."
            );

            return "usuario/modifica";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "usuario/modifica";
        }
    }

    @GetMapping("/admin/usuarios")
    public String administrarUsuarios(
            Authentication authentication,
            Model model) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Usuario usuario =
                usuarioService.getUsuarioPorUsername(
                        authentication.getName()
                );

        if (usuario == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/perfil";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("pacientes", usuarioService.getPacientes());
        model.addAttribute("medicos", usuarioService.getMedicos());
        model.addAttribute(
                "especialidades",
                usuarioService.getEspecialidadesActivas()
        );
        model.addAttribute("mostrarAdministracion", true);

        return "usuario/perfil";
    }
}
