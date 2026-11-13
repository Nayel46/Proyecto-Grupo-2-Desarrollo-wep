
package com.healthnow.repository;

import com.healthnow.domain.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {

    List<Cita> findByPaciente_IdUsuarioOrderByFechaDescHoraDesc(
            Integer idPaciente
    );

    List<Cita> findByDoctor_IdUsuarioOrderByFechaAscHoraAsc(
            Integer idDoctor
    );

    List<Cita> findByDoctor_IdUsuarioAndFechaOrderByHoraAsc(
            Integer idDoctor,
            LocalDate fecha
    );

    List<Cita> findByPaciente_IdUsuarioAndEstadoOrderByFechaDescHoraDesc(
            Integer idPaciente,
            String estado
    );

    // CITAS PENDIENTES DE UN DOCTOR

    List<Cita> findByDoctor_IdUsuarioAndEstadoOrderByFechaAscHoraAsc(
            Integer idDoctor,
            String estado
    );

    boolean existsByDoctor_IdUsuarioAndFechaAndHoraAndEstadoNot(
            Integer idDoctor,
            LocalDate fecha,
            LocalTime hora,
            String estado
    );

    boolean existsByDoctor_IdUsuarioAndFechaAndHoraAndEstadoNotAndIdCitaNot(
            Integer idDoctor,
            LocalDate fecha,
            LocalTime hora,
            String estado,
            Integer idCita
    );

    List<Cita> findByEstadoOrderByFechaAscHoraAsc(
            String estado
    );
}

