package com.healthnow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "medico")
public class Doctor {
 @Id
 @Column(name = "id_usuario")
 private Integer idUsuario;
 
 @OneToOne(fetch = FetchType.LAZY)
 @MapsId
 @JoinColumn(name = "id_usuario")
 private Usuario usuario; 
 
 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "id_especialidad", nullable = false)
 private Especialidad especialidad;
 
 @Column(name = "descripcion", length = 500)
 private String descripcion; 
 
 public Doctor(){
 }
 
 public Integer getIdUsuario(){
     return idUsuario;
 }
 
 public void setIdUsuario(Integer idUsuario){
     this.idUsuario = idUsuario;
 }
 
 public Usuario getUsuario(){
     return usuario;
 }
 
 public void setUsuario(Usuario usuario){
     this.usuario = usuario; 
 }
 
 public Especialidad getEspecialidad(){
     return especialidad;
 }
 
 public void setEspecialidad(Especialidad especialidad){
     this.especialidad = especialidad;
 }
 
 public String getDescripcion(){
     return descripcion;
 }
 
 public void setDescripcion(String descripcion){
     this.descripcion = descripcion;
 }
 
}
