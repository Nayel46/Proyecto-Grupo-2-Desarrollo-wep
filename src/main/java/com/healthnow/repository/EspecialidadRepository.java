package com.healthnow.repository;

import com.healthnow.domain.Especialidad;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadRepository
        extends JpaRepository<Especialidad, Integer> {

    List<Especialidad> findByActivoTrue();

    List<Especialidad> findByNombreContainingIgnoreCase(String nombre);

    Optional<Especialidad> findByNombreIgnoreCase(String nombre);
}