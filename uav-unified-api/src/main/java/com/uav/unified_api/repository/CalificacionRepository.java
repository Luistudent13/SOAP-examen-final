package com.uav.unified_api.repository;
import com.uav.unified_api.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    List<Calificacion> findByMatricula(String matricula);
}
