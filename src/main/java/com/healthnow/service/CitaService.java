
package com.healthnow.service;

import com.healthnow.domain.Cita;
import com.healthnow.domain.Usuario;
import com.healthnow.repository.CitaRepository;
import com.healthnow.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;

    public CitaService(CitaRepository citaRepository,
                       UsuarioRepository usuarioRepository) {
        this.citaRepository = citaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Cita> listarPorPaciente(Integer idPaciente) {
        return citaRepository
                .findByPaciente_IdUsuarioOrderByFechaDescHoraDesc(idPaciente);
    }

    @Transactional(readOnly = true)
    public List<Cita> listarPorDoctor(Integer idDoctor) {
        return citaRepository
                .findByDoctor_IdUsuarioOrderByFechaAscHoraAsc(idDoctor);
    }

    @Transactional(readOnly = true)
    public List<Cita> listarPorDoctorYFecha(Integer idDoctor,
                                            LocalDate fecha) {
        return citaRepository
                .findByDoctor_IdUsuarioAndFechaOrderByHoraAsc(
                        idDoctor, fecha);
    }

    @Transactional(readOnly = true)
    public Cita buscarPorId(Integer idCita) {
        return citaRepository.findById(idCita)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La cita no existe."));
    }

    @Transactional(readOnly = true)
    public boolean horarioDisponible(Integer idDoctor,
                                     LocalDate fecha,
                                     LocalTime hora) {
        return !citaRepository
                .existsByDoctor_IdUsuarioAndFechaAndHoraAndEstadoNot(
                        idDoctor, fecha, hora, "CANCELADA");
    }

    @Transactional
    public Cita agendar(Integer idPaciente,
                        Integer idDoctor,
                        LocalDate fecha,
                        LocalTime hora,
                        String motivo,
                        String modalidad) {

        Usuario paciente = obtenerUsuario(idPaciente);
        Usuario doctor = obtenerUsuario(idDoctor);

        validarRol(paciente, "PACIENTE");
        validarRol(doctor, "DOCTOR");
        validarFechaHora(fecha, hora);
        validarMotivo(motivo);
        validarModalidad(modalidad);

        if (!horarioDisponible(idDoctor, fecha, hora)) {
            throw new IllegalArgumentException(
                    "El doctor ya tiene una cita en ese horario.");
        }

        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setDoctor(doctor);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setMotivo(motivo.trim());
        cita.setModalidad(modalidad.trim().toUpperCase());
        cita.setEstado("PENDIENTE");

        return citaRepository.save(cita);
    }

    @Transactional
    public Cita editar(Integer idCita,
                       Integer idPaciente,
                       Integer idDoctor,
                       LocalDate fecha,
                       LocalTime hora,
                       String motivo,
                       String modalidad) {

        Cita cita = buscarPorId(idCita);

        validarPropietario(cita, idPaciente);
        validarEstadoEditable(cita);
        validarFechaHora(fecha, hora);
        validarMotivo(motivo);
        validarModalidad(modalidad);

        Usuario doctor = obtenerUsuario(idDoctor);
        validarRol(doctor, "DOCTOR");

        boolean ocupado = citaRepository
                .existsByDoctor_IdUsuarioAndFechaAndHoraAndEstadoNotAndIdCitaNot(
                        idDoctor, fecha, hora, "CANCELADA", idCita);

        if (ocupado) {
            throw new IllegalArgumentException(
                    "El doctor ya tiene una cita en ese horario.");
        }

        cita.setDoctor(doctor);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setMotivo(motivo.trim());
        cita.setModalidad(modalidad.trim().toUpperCase());
        cita.setEstado("PENDIENTE");

        return citaRepository.save(cita);
    }

    @Transactional
    public void cancelar(Integer idCita, Integer idPaciente) {

        Cita cita = buscarPorId(idCita);

        validarPropietario(cita, idPaciente);
        validarEstadoEditable(cita);

        cita.setEstado("CANCELADA");
        citaRepository.save(cita);
    }

    private Usuario obtenerUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El usuario no existe."));
    }

    private void validarRol(Usuario usuario, String rol) {

        if (usuario.getRol() == null
                || !rol.equalsIgnoreCase(
                        usuario.getRol().toString())) {
            throw new IllegalArgumentException(
                    "El usuario no tiene el rol requerido: " + rol);
        }

        if (!Boolean.TRUE.equals(usuario.isActivo())) {
            throw new IllegalArgumentException(
                    "El usuario está inactivo.");
        }
    }

    private void validarFechaHora(LocalDate fecha, LocalTime hora) {

        if (fecha == null || hora == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una fecha y hora.");
        }

        if (!LocalDateTime.of(fecha, hora)
                .isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "La fecha y hora deben ser futuras.");
        }
    }

    private void validarMotivo(String motivo) {

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe ingresar el motivo de la cita.");
        }

        if (motivo.length() > 500) {
            throw new IllegalArgumentException(
                    "El motivo no puede superar 500 caracteres.");
        }
    }

    private void validarModalidad(String modalidad) {

        if (modalidad == null
                || (!modalidad.trim().equalsIgnoreCase("PRESENCIAL")
                && !modalidad.trim().equalsIgnoreCase("VIRTUAL"))) {
            throw new IllegalArgumentException(
                    "Seleccione una modalidad válida.");
        }
    }

    private void validarPropietario(Cita cita, Integer idPaciente) {

        if (idPaciente == null
                || !cita.getPaciente().getIdUsuario()
                        .equals(idPaciente)) {
            throw new IllegalArgumentException(
                    "No tiene permiso para modificar esta cita.");
        }
    }

    private void validarEstadoEditable(Cita cita) {

        if ("CANCELADA".equalsIgnoreCase(cita.getEstado())
                || "ATENDIDA".equalsIgnoreCase(cita.getEstado())) {
            throw new IllegalArgumentException(
                    "Esta cita ya no puede modificarse.");
        }
    }
}