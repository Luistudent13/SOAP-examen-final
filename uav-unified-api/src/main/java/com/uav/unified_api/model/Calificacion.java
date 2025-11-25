package com.uav.unified_api.model;
import jakarta.persistence.*;

@Entity
@Table(name = "calificaciones")
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String matricula;

    @Column(nullable = false, length = 100)
    private String materia;

    @Column(nullable = false)
    private Double calificacion;

    public Calificacion() {
    }

    public Calificacion(String matricula, String materia, Double calificacion) {
        this.matricula = matricula;
        this.materia = materia;
        this.calificacion = calificacion;
    }

    public Long getId() {
        return id;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public Double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Double calificacion) {
        this.calificacion = calificacion;
    }
}
