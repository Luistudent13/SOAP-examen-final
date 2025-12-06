package com.uav.unified_api.controller;

import com.uav.unified_api.model.Curso;
import com.uav.unified_api.repository.CursoRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cursos")
@CrossOrigin(origins = "*")
public class CursoController {

    private final CursoRepository cursoRepository;

    public CursoController(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    // ===== 1) Registrar curso =====
    // POST /api/cursos
    @PostMapping
    public ResponseEntity<Curso> registrarCurso(@RequestBody Curso curso) {
        Curso guardado = cursoRepository.save(curso);
        return ResponseEntity.ok(guardado);
    }

    // ===== 2) Consultar curso por ID =====
    // GET /api/cursos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> consultarPorId(@PathVariable Long id) {
        return cursoRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity
                        .status(404)
                        .body("Curso no encontrado con id: " + id));
    }

    // ===== 3) Eliminar curso por ID (parte de "registrar y eliminar") =====
    // DELETE /api/cursos/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPorId(@PathVariable Long id) {
        if (!cursoRepository.existsById(id)) {
            return ResponseEntity
                    .status(404)
                    .body("No existe curso con id: " + id);
        }
        cursoRepository.deleteById(id);
        return ResponseEntity.ok("Curso eliminado correctamente (id=" + id + ")");
    }

    // ===== 4) Eliminar curso por nombre =====
    // DELETE /api/cursos/nombre/{nombre}
    @DeleteMapping("/nombre/{nombre}")
    public ResponseEntity<?> eliminarPorNombre(@PathVariable String nombre) {
        long eliminados = cursoRepository.deleteByNombre(nombre);
        if (eliminados == 0) {
            return ResponseEntity
                    .status(404)
                    .body("No se encontraron cursos con nombre: " + nombre);
        }
        return ResponseEntity.ok("Cursos eliminados con nombre '" + nombre + "': " + eliminados);
    }

    // ===== 5) Eliminar curso por fecha de inicio =====
    // DELETE /api/cursos/fecha-inicio/{fecha}
    // Ejemplo: DELETE /api/cursos/fecha-inicio/2025-02-01
    @DeleteMapping("/fecha-inicio/{fecha}")
    public ResponseEntity<?> eliminarPorFechaInicio(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        long eliminados = cursoRepository.deleteByFechaInicio(fecha);
        if (eliminados == 0) {
            return ResponseEntity
                    .status(404)
                    .body("No se encontraron cursos con fecha de inicio: " + fecha);
        }
        return ResponseEntity.ok("Cursos eliminados con fecha de inicio " + fecha + ": " + eliminados);
    }

    // ===== 6) Listar todos (extra, útil para pruebas) =====
    // GET /api/cursos
    @GetMapping
    public List<Curso> listarCursos() {
        return cursoRepository.findAll();
    }
}
