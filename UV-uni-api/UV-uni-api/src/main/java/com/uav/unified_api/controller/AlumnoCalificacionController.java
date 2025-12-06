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

    // ====== 1) Registrar alumno (consumiendo SOAP) ======
    @PostMapping("/alumnos")
    public ResponseEntity<?> registrarAlumno(@RequestBody Alumnomodel alumno) {

        // Llama al cliente SOAP que tienes en MatriculaSoapClient
        Alumnomodel resultado = matriculaSoapClient.registrarAlumno(alumno);

        if (resultado == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("message", "No se pudo registrar el alumno en el servicio SOAP");
            return ResponseEntity.status(500).body(error);
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("ok", true);
        respuesta.put("alumno", resultado);

        return ResponseEntity.ok(respuesta);
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
