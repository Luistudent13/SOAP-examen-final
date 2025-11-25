package com.uav.unified_api.controller;


import com.uav.unified_api.model.Alumnomodel;
import com.uav.unified_api.model.Calificacion;
import com.uav.unified_api.repository.CalificacionRepository;
import com.uav.unified_api.service.MatriculaSoapClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // para que pueda entrar tu frontend
public class AlumnoCalificacionController {

    private final MatriculaSoapClient matriculaSoapClient;
    private final CalificacionRepository calificacionRepository;

    public AlumnoCalificacionController(MatriculaSoapClient matriculaSoapClient,
                                        CalificacionRepository calificacionRepository) {
        this.matriculaSoapClient = matriculaSoapClient;
        this.calificacionRepository = calificacionRepository;
    }

    // Endpoint unificado: alumno + calificaciones
    @GetMapping("/alumnos/{matricula}")
    public ResponseEntity<?> obtenerInfoCompleta(@PathVariable String matricula) {
        Alumnomodel alumno = matriculaSoapClient.obtenerAlumnoPorMatricula(matricula);
        if (alumno == null) {
            return ResponseEntity.notFound().build();
        }

        List<Calificacion> calificaciones = calificacionRepository.findByMatricula(matricula);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("alumno", alumno);
        respuesta.put("calificaciones", calificaciones);

        return ResponseEntity.ok(respuesta);
    }

    // CRUD simple de calificaciones (para pruebas)
    @PostMapping("/calificaciones")
    public Calificacion crearCalificacion(@RequestBody Calificacion calificacion) {
        return calificacionRepository.save(calificacion);
    }

    @GetMapping("/calificaciones")
    public List<Calificacion> listarCalificaciones() {
        return calificacionRepository.findAll();
    }
}
