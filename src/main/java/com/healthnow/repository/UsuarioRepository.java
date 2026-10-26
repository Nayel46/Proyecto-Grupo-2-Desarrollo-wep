
package com.healthnow.repository;

import com.healthnow.domain.Usuario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    public Usuario findByUsername(String username);

    public Usuario findByCorreo(String correo);

    public Usuario findByCedula(String cedula);

    public List<Usuario> findByActivoTrue();

    public List<Usuario> findByRolOrderByNombreAscApellidosAsc(String rol);

    public List<Usuario> findByRolAndActivoTrueOrderByNombreAscApellidosAsc(
            String rol
    );

    @Query(value = """
        SELECT u.*
        FROM usuario u
        INNER JOIN medico m
            ON u.id_usuario = m.id_usuario
        INNER JOIN especialidad e
            ON m.id_especialidad = e.id_especialidad
        WHERE u.rol = 'DOCTOR'
          AND u.activo = b'1'
          AND e.activo = b'1'
          AND m.id_especialidad = :idEspecialidad
        ORDER BY u.nombre, u.apellidos
        """, nativeQuery = true)
    public List<Usuario> buscarMedicosPorEspecialidad(
            @Param("idEspecialidad") Integer idEspecialidad
    );

    @Query(value = """
        SELECT e.nombre
        FROM medico m
        INNER JOIN especialidad e
            ON m.id_especialidad = e.id_especialidad
        INNER JOIN usuario u
            ON m.id_usuario = u.id_usuario
        WHERE u.id_usuario = :idUsuario
          AND u.rol = 'DOCTOR'
        """, nativeQuery = true)
    public String obtenerEspecialidadDoctor(
            @Param("idUsuario") Integer idUsuario
    );

    @Query(value = """
        SELECT m.id_especialidad
        FROM medico m
        WHERE m.id_usuario = :idUsuario
        """, nativeQuery = true)
    public Integer obtenerIdEspecialidadMedico(
            @Param("idUsuario") Integer idUsuario
    );

    @Modifying
    @Query(value = """
        INSERT INTO medico (
            id_usuario,
            id_especialidad,
            descripcion
        )
        VALUES (
            :idUsuario,
            :idEspecialidad,
            :descripcion
        )
        """, nativeQuery = true)
    public void registrarEspecialidadMedico(
            @Param("idUsuario") Integer idUsuario,
            @Param("idEspecialidad") Integer idEspecialidad,
            @Param("descripcion") String descripcion
    );

    @Modifying
    @Query(value = """
        UPDATE medico
        SET id_especialidad = :idEspecialidad,
            descripcion = :descripcion
        WHERE id_usuario = :idUsuario
        """, nativeQuery = true)
    public int modificarEspecialidadMedico(
            @Param("idUsuario") Integer idUsuario,
            @Param("idEspecialidad") Integer idEspecialidad,
            @Param("descripcion") String descripcion
    );

}
