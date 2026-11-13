package com.healthnow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    @NotBlank(message = "El nombre de usuario es obligatorio.")
    @Size(max = 30, message = "El usuario no puede superar 30 caracteres.")
    @Column(nullable = false, length = 30, unique = true)
    private String username;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(max = 255, message = "La contraseña no puede superar 255 caracteres.")
    @Column(nullable = false, length = 255)
    private String password;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 60, message = "El nombre no puede superar 60 caracteres.")
    @Column(nullable = false, length = 60)
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios.")
    @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String apellidos;

    @NotBlank(message = "La cédula es obligatoria.")
    @Size(max = 30, message = "La cédula no puede superar 30 caracteres.")
    @Column(nullable = false, length = 30, unique = true)
    private String cedula;

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "Ingrese un correo electrónico válido.")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres.")
    @Column(nullable = false, length = 150, unique = true)
    private String correo;

    @Size(max = 25, message = "El teléfono no puede superar 25 caracteres.")
    @Column(length = 25)
    private String telefono;

    @Column(nullable = false)
    private boolean activo = true;

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @NotBlank(message = "El rol es obligatorio.")
    @Column(name = "rol", nullable = false, length = 20)
    private String rol = "PACIENTE";

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

}
