package com.uav.unified_api.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "cursos")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del curso (para buscar y eliminar por nombre)
    @Column(nullable = false, length = 150)
    private String nombre;

    // Fecha de inicio (para buscar y eliminar por fecha)
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    // Matricula del alumno dueño del curso (para la unificación con SOAP)
    @Column(nullable = false, length = 20)
    private String matricula;

    public Curso() {
    }

    public Curso(Long id, String nombre, LocalDate fechaInicio, String matricula) {
        this.id = id;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.matricula = matricula;
    }

    // ===== Getters y Setters =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }
}
