
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;

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
    
    @GetMapping("/admin/usuarios/{id}/editar")
    public String mostrarEditarUsuario(
            @PathVariable Integer id,
            Authentication authentication,
            Model model) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Usuario administrador =
                usuarioService.getUsuarioPorUsername(
                        authentication.getName()
                );

        if (administrador == null
                || !"ADMIN".equalsIgnoreCase(administrador.getRol())) {
            return "redirect:/perfil";
        }

        Usuario usuario = usuarioService.getUsuario(id).orElse(null);

        if (usuario == null
                || (!"PACIENTE".equalsIgnoreCase(usuario.getRol())
                && !"DOCTOR".equalsIgnoreCase(usuario.getRol()))) {
            return "redirect:/admin/usuarios";
        }

        model.addAttribute("usuario", usuario);
        
        if("DOCTOR".equalsIgnoreCase(usuario.getRol())){
            model.addAttribute("especialidades", usuarioService.getEspecialidadesActivas());
            model.addAttribute("idEspecialidad", usuarioService.obtenerIdEspecialidadMedico(id));
        }
        return "usuario/editar";
    }

    @PostMapping("/admin/usuarios/{id}/editar")
    public String guardarEdicionUsuario(
            @PathVariable Integer id,
            @ModelAttribute("usuario") Usuario datosActualizados,
            @RequestParam(required = false) Integer idEspecialidad,
            Authentication authentication,Model model, RedirectAttributes redirectAttributes) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Usuario administrador =
                usuarioService.getUsuarioPorUsername(
                        authentication.getName()
                );

        if (administrador == null
                || !"ADMIN".equalsIgnoreCase(administrador.getRol())) {
            return "redirect:/perfil";
        }

        try {
            Usuario usuarioOriginal = usuarioService.getUsuario(id).orElse(null);
            
         if(usuarioOriginal == null){
             return "redirect:/admin/usuarios";
         }
         
         usuarioService.modificarUsuarioAdministracion(id, datosActualizados);
         
         if("DOCTOR".equalsIgnoreCase(usuarioOriginal.getRol())){
             usuarioService.actualizarEspecialidadMedico(id, idEspecialidad);
            }
            
         redirectAttributes.addFlashAttribute("mensaje", "El usuario se modifico corrrectamente.");
         
         return "redirect:/admin/usuarios";
        } catch (DataIntegrityViolationException e){
            datosActualizados.setIdUsuario(id); 
            model.addAttribute("usuario", datosActualizados);
            model.addAttribute("error", "El correo electrónico ya esta registrado.");
            
            cargarDatosEdicion(id, datosActualizados, model);
            
            return "usuario/editar";
        }
    }

    @GetMapping("/admin/usuarios/nuevo-paciente")
    public String mostrarFormularioPaciente(Authentication authentication, Model model){
    
        if(authentication == null || !authentication.isAuthenticated()){
            return "redirect:/login";
        }
        
        Usuario administrador = usuarioService.getUsuarioPorUsername(authentication.getName());
        
        if(administrador == null || !"ADMIN".equalsIgnoreCase(administrador.getRol())){
            return "redirect:/perfil";
        }
        model.addAttribute("usuario", new Usuario());
        
        return "usuario/agregar-paciente";
    }
    
    @PostMapping("/admin/usuarios/nuevo-paciente")
    public String guardarNuevoPaciente(
        @ModelAttribute("usuario") Usuario usuario,
        Authentication authentication,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Usuario administrador =
                usuarioService.getUsuarioPorUsername(
                        authentication.getName()
                );

        if (administrador == null
                || !"ADMIN".equalsIgnoreCase(administrador.getRol())) {
            return "redirect:/perfil";
        }

        try {

            usuarioService.registrarUsuarioAdministracion(
                    usuario,
                    "PACIENTE",
                    null,
                    null
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "El paciente se registró correctamente."
            );

            return "redirect:/admin/usuarios";

        } catch (IllegalArgumentException
                | DataIntegrityViolationException e) {

            model.addAttribute("error", e.getMessage());
            return "usuario/agregar-paciente";
        }
    }
    
    @GetMapping("/admin/usuarios/nuevo-doctor")
    public String mostrarFormularioDoctor(
            Authentication authentication,
            Model model) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Usuario administrador =
                usuarioService.getUsuarioPorUsername(
                        authentication.getName()
                );

        if (administrador == null
                || !"ADMIN".equalsIgnoreCase(administrador.getRol())) {
            return "redirect:/perfil";
        }

        model.addAttribute("usuario", new Usuario());

        model.addAttribute(
                "especialidades",
                usuarioService.getEspecialidadesActivas()
        );

        return "usuario/agregar-doctor";
    }
    
    @PostMapping("/admin/usuarios/nuevo-doctor")
    public String guardarNuevoDoctor(
            @ModelAttribute("usuario") Usuario usuario,
            @RequestParam Integer idEspecialidad,
            @RequestParam(required = false) String descripcion,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Usuario administrador =
                usuarioService.getUsuarioPorUsername(
                        authentication.getName()
                );

        if (administrador == null
                || !"ADMIN".equalsIgnoreCase(administrador.getRol())) {
            return "redirect:/perfil";
        }

        try {

            usuarioService.registrarUsuarioAdministracion(
                    usuario,
                    "DOCTOR",
                    idEspecialidad,
                    descripcion
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "El médico se registró correctamente."
            );

            return "redirect:/admin/usuarios";

        } catch (IllegalArgumentException
                | DataIntegrityViolationException e) {

            model.addAttribute("error", e.getMessage());

            model.addAttribute(
                    "especialidades",
                    usuarioService.getEspecialidadesActivas()
            );

            return "usuario/agregar-doctor";
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
        model.addAttribute("especialidades",usuarioService.getEspecialidadesActivas());
        model.addAttribute("mostrarAdministracion", true);

        return "usuario/perfil";
    }
    
    private void cargarDatosEdicion(Integer id, Usuario usuario, Model model){
        if("DOCTOR".equalsIgnoreCase(usuario.getRol())){
            model.addAttribute("especialidades", usuarioService.getEspecialidadesActivas());
            model.addAttribute("idEspecialidad", usuarioService.obtenerIdEspecialidadMedico(id));
            }
   
    }
}
