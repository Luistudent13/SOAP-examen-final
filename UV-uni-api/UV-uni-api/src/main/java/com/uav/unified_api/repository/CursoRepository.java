package com.uav.unified_api.repository;

import com.uav.unified_api.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    // Consultar cursos por nombre
    List<Curso> findByNombre(String nombre);

    // Consultar cursos por fecha de inicio
    List<Curso> findByFechaInicio(LocalDate fechaInicio);

    // Para la unificación con Alumno (por matrícula)
    List<Curso> findByMatricula(String matricula);

    // Eliminar por nombre
    long deleteByNombre(String nombre);

    // Eliminar por fecha de inicio
    long deleteByFechaInicio(LocalDate fechaInicio);
}
