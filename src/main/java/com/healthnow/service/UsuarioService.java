
package com.healthnow.service;

import com.healthnow.domain.Especialidad;
import com.healthnow.domain.Usuario;
import com.healthnow.repository.EspecialidadRepository;
import com.healthnow.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EspecialidadRepository especialidadRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            EspecialidadRepository especialidadRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.especialidadRepository = especialidadRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> getUsuarios(boolean activo) {
        if (activo) {
            return usuarioRepository.findByActivoTrue();
        }
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario);
    }

    @Transactional(readOnly = true)
    public Usuario getUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {

        usuario.setRol("PACIENTE");
        usuario.setActivo(true);

        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );

        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscarMedicosPorEspecialidad(
            Integer idEspecialidad) {

        return usuarioRepository.buscarMedicosPorEspecialidad(
                idEspecialidad
        );
    }

    @Transactional(readOnly = true)
    public String obtenerEspecialidadDoctor(Integer idUsuario) {

        return usuarioRepository.obtenerEspecialidadDoctor(
                idUsuario
        );
    }

    @Transactional
    public Usuario actualizarPerfil(
            String username,
            Usuario datosActualizados) {

        Usuario usuario = usuarioRepository.findByUsername(username);

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "No se encontró el usuario."
            );
        }

        validarDatosPersonales(datosActualizados);

        Usuario correoExistente =
                usuarioRepository.findByCorreo(
                        datosActualizados.getCorreo()
                );

        if (correoExistente != null
                && !correoExistente.getIdUsuario()
                        .equals(usuario.getIdUsuario())) {

            throw new IllegalArgumentException(
                    "El correo electrónico ya está registrado."
            );
        }

        usuario.setNombre(datosActualizados.getNombre().trim());
        usuario.setApellidos(datosActualizados.getApellidos().trim());
        usuario.setCorreo(datosActualizados.getCorreo().trim());
        usuario.setTelefono(datosActualizados.getTelefono());

        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public List<Usuario> getPacientes() {

        return usuarioRepository
                .findByRolAndActivoTrueOrderByNombreAscApellidosAsc(
                        "PACIENTE"
                );
    }

    @Transactional(readOnly = true)
    public List<Usuario> getMedicos() {

        return usuarioRepository
                .findByRolAndActivoTrueOrderByNombreAscApellidosAsc(
                        "DOCTOR"
                );
    }

    @Transactional(readOnly = true)
    public List<Usuario> getUsuariosPorRol(String rol) {

        return usuarioRepository
                .findByRolOrderByNombreAscApellidosAsc(rol);
    }

    @Transactional(readOnly = true)
    public Integer obtenerIdEspecialidadMedico(Integer idUsuario) {

        return usuarioRepository.obtenerIdEspecialidadMedico(
                idUsuario
        );
    }

    @Transactional(readOnly = true)
    public List<Especialidad> getEspecialidadesActivas() {

        return especialidadRepository.findByActivoTrue();
    }

    @Transactional
    public Usuario registrarUsuarioAdministracion(
            Usuario usuario,
            String rol) {

        if (!"PACIENTE".equals(rol)) {
            throw new IllegalArgumentException(
                    "Para registrar un médico debe seleccionar una especialidad."
            );
        }

        validarNuevoUsuario(usuario);

        usuario.setIdUsuario(null);
        usuario.setRol("PACIENTE");
        usuario.setActivo(true);

        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario registrarUsuarioAdministracion(
            Usuario usuario,
            String rol,
            Integer idEspecialidad,
            String descripcion) {

        if (!"PACIENTE".equals(rol)
                && !"DOCTOR".equals(rol)) {

            throw new IllegalArgumentException(
                    "El rol seleccionado no es válido."
            );
        }

        validarNuevoUsuario(usuario);

        if ("DOCTOR".equals(rol)) {
            validarEspecialidad(idEspecialidad);
        }

        usuario.setIdUsuario(null);
        usuario.setRol(rol);
        usuario.setActivo(true);

        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );

        Usuario usuarioGuardado =
                usuarioRepository.saveAndFlush(usuario);

        if ("DOCTOR".equals(rol)) {

            usuarioRepository.registrarEspecialidadMedico(
                    usuarioGuardado.getIdUsuario(),
                    idEspecialidad,
                    descripcion
            );
        }

        return usuarioGuardado;
    }

    @Transactional
    public Usuario modificarUsuarioAdministracion(
            Integer idUsuario,
            Usuario datosActualizados) {

        Usuario usuario = obtenerUsuarioAdministrable(idUsuario);

        validarDatosPersonales(datosActualizados);
        validarCorreoDisponible(
                datosActualizados.getCorreo(),
                idUsuario
        );

        usuario.setNombre(datosActualizados.getNombre().trim());
        usuario.setApellidos(datosActualizados.getApellidos().trim());
        usuario.setCorreo(datosActualizados.getCorreo().trim());
        usuario.setTelefono(datosActualizados.getTelefono());

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario modificarUsuarioAdministracion(
            Integer idUsuario,
            Usuario datosActualizados,
            Integer idEspecialidad,
            String descripcion) {

        Usuario usuario = obtenerUsuarioAdministrable(idUsuario);

        validarDatosPersonales(datosActualizados);
        validarCorreoDisponible(
                datosActualizados.getCorreo(),
                idUsuario
        );

        if ("DOCTOR".equals(usuario.getRol())) {
            validarEspecialidad(idEspecialidad);
        }

        usuario.setNombre(datosActualizados.getNombre().trim());
        usuario.setApellidos(datosActualizados.getApellidos().trim());
        usuario.setCorreo(datosActualizados.getCorreo().trim());
        usuario.setTelefono(datosActualizados.getTelefono());

        Usuario usuarioGuardado =
                usuarioRepository.save(usuario);

        if ("DOCTOR".equals(usuario.getRol())) {

            int actualizados =
                    usuarioRepository.modificarEspecialidadMedico(
                            idUsuario,
                            idEspecialidad,
                            descripcion
                    );

            if (actualizados == 0) {

                usuarioRepository.registrarEspecialidadMedico(
                        idUsuario,
                        idEspecialidad,
                        descripcion
                );
            }
        }

        return usuarioGuardado;
    }

    @Transactional
    public void desactivarUsuario(Integer idUsuario) {

        Usuario usuario = obtenerUsuarioAdministrable(idUsuario);

        usuario.setActivo(false);

        usuarioRepository.save(usuario);
    }

    private Usuario obtenerUsuarioAdministrable(Integer idUsuario) {

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el usuario."
                        )
                );

        if (!"PACIENTE".equals(usuario.getRol())
                && !"DOCTOR".equals(usuario.getRol())) {

            throw new IllegalArgumentException(
                    "Este usuario no puede administrarse desde esta sección."
            );
        }

        return usuario;
    }

    private void validarEspecialidad(Integer idEspecialidad) {

        if (idEspecialidad == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una especialidad."
            );
        }

        Especialidad especialidad =
                especialidadRepository.findById(idEspecialidad)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "La especialidad seleccionada no existe."
                                )
                        );

        if (!especialidad.isActivo()) {
            throw new IllegalArgumentException(
                    "La especialidad seleccionada no está activa."
            );
        }
    }

    private void validarNuevoUsuario(Usuario usuario) {

        validarDatosPersonales(usuario);

        if (usuario.getUsername() == null
                || usuario.getUsername().isBlank()
                || usuario.getUsername().length() > 30) {

            throw new IllegalArgumentException(
                    "Ingrese un nombre de usuario válido."
            );
        }

        if (usuario.getCedula() == null
                || usuario.getCedula().isBlank()
                || usuario.getCedula().length() > 30) {

            throw new IllegalArgumentException(
                    "Ingrese una cédula válida."
            );
        }

        if (usuario.getPassword() == null
                || usuario.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "La contraseña es obligatoria."
            );
        }

        if (usuarioRepository.findByUsername(
                usuario.getUsername()) != null) {

            throw new IllegalArgumentException(
                    "El nombre de usuario ya está registrado."
            );
        }

        if (usuarioRepository.findByCedula(
                usuario.getCedula()) != null) {

            throw new IllegalArgumentException(
                    "La cédula ya está registrada."
            );
        }

        validarCorreoDisponible(usuario.getCorreo(), null);
    }

    private void validarDatosPersonales(Usuario usuario) {

        if (usuario == null
                || usuario.getNombre() == null
                || usuario.getNombre().isBlank()
                || usuario.getApellidos() == null
                || usuario.getApellidos().isBlank()
                || usuario.getCorreo() == null
                || usuario.getCorreo().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre, los apellidos y el correo son obligatorios."
            );
        }

        if (usuario.getNombre().length() > 60
                || usuario.getApellidos().length() > 100
                || usuario.getCorreo().length() > 150
                || (usuario.getTelefono() != null
                && usuario.getTelefono().length() > 25)) {

            throw new IllegalArgumentException(
                    "Uno de los campos supera la longitud permitida."
            );
        }

        if (!usuario.getCorreo().matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            throw new IllegalArgumentException(
                    "Ingrese un correo electrónico válido."
            );
        }
    }

    private void validarCorreoDisponible(
            String correo,
            Integer idUsuario) {

        Usuario existente =
                usuarioRepository.findByCorreo(correo);

        if (existente != null
                && (idUsuario == null
                || !existente.getIdUsuario().equals(idUsuario))) {

            throw new IllegalArgumentException(
                    "El correo electrónico ya está registrado."
            );
        }
    }
}
