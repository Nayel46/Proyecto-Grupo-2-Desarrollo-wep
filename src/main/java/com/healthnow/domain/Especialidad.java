
package com.healthnow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;

@Entity
@Table(name = "especialidad")
public class Especialidad implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especialidad")
    private Integer idEspecialidad;

    @NotBlank(message = "El nombre de la especialidad es obligatorio.")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
    @Column(nullable = false, length = 100, unique = true)
    private String nombre;

    @Size(max = 500, message = "Los comentarios no pueden superar 500 caracteres.")
    @Column(length = 500)
    private String comentarios;

    @Column(nullable = false)
    private boolean activo = true;

    // CONSTRUCTOR
    public Especialidad() {
    }

    // ID ESPECIALIDAD
    public Integer getIdEspecialidad() {
        return idEspecialidad;
    }

    public void setIdEspecialidad(Integer idEspecialidad) {
        this.idEspecialidad = idEspecialidad;
    }

    // NOMBRE
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // COMENTARIOS
    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    // ESTADO
    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
