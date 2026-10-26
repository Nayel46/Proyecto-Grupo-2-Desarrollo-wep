package com.healthnow.service;

import com.healthnow.domain.Especialidad;
import com.healthnow.repository.EspecialidadRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EspecialidadService {

    private final EspecialidadRepository especialidadRepository;

    public EspecialidadService(EspecialidadRepository especialidadRepository) {
        this.especialidadRepository = especialidadRepository;
    }

    @Transactional(readOnly = true)
    public List<Especialidad> getEspecialidades() {
        return especialidadRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public List<Especialidad> buscarEspecialidades(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            return getEspecialidades();
        }

        List<Especialidad> activas = getEspecialidades();

        return activas.stream()
                .filter(especialidad
                        -> especialidad.getNombre()
                                .toLowerCase()
                                .contains(nombre.trim().toLowerCase()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Especialidad getEspecialidad(Integer idEspecialidad) {

        return especialidadRepository
                .findById(idEspecialidad)
                .orElse(null);
    }

    @Transactional
    public void guardar(Especialidad especialidad) {

        if (especialidad.getIdEspecialidad() == null) {

            // Nueva especialidad: activa por defecto
            especialidad.setActivo(true);

        } else {

            // Buscar la especialidad existente
            Especialidad existente = especialidadRepository
                    .findById(especialidad.getIdEspecialidad())
                    .orElseThrow(()
                            -> new IllegalArgumentException(
                                    "La especialidad no existe"
                            )
                    );

            // Conservar el estado actual
            especialidad.setActivo(existente.isActivo());
        }

        especialidadRepository.save(especialidad);
    }

    @Transactional
    public void eliminar(Integer idEspecialidad) {

        Optional<Especialidad> especialidad
                = especialidadRepository.findById(idEspecialidad);

        if (especialidad.isPresent()) {

            Especialidad registro = especialidad.get();

            registro.setActivo(false);

            especialidadRepository.save(registro);
        }
    }
}
