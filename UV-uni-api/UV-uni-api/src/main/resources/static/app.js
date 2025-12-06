// ========================== CONFIG ==========================
const API_BASE = "http://localhost:8081/api";

// Mensajes
const mensajeGlobal = document.getElementById("mensaje-global");

function mostrarMensaje(texto, tipo = "ok") {
    mensajeGlobal.textContent = texto;
    mensajeGlobal.className = tipo === "ok" ? "ok" : "error";
}

function limpiarMensaje() {
    mensajeGlobal.textContent = "";
    mensajeGlobal.className = "";
}

// Visualización de respuestas JSON
function manejarRespuestaJson(data, preElement) {
    preElement.style.display = "block";
    preElement.textContent = JSON.stringify(data, null, 2);
}

// ========================== ALUMNOS ==========================

const inputMatricula = document.getElementById("alumno-matricula");
const inputNombre = document.getElementById("alumno-nombre");
const inputPrograma = document.getElementById("alumno-programa");
const preAlumno = document.getElementById("alumno-respuesta");

// Registrar alumno (SOAP vía REST)
document.getElementById("btn-registrar-alumno").addEventListener("click", async () => {
    limpiarMensaje();

    const payload = {
        matricula: inputMatricula.value.trim(),
        nombre: inputNombre.value.trim(),
        programa: inputPrograma.value.trim()
    };

    if (!payload.matricula || !payload.nombre || !payload.programa) {
        mostrarMensaje("Completa todos los campos del alumno.", "error");
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/alumnos`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) mostrarMensaje("Error al registrar alumno", "error");
        else mostrarMensaje("Alumno registrado correctamente");

        manejarRespuestaJson(data, preAlumno);

    } catch (err) {
        mostrarMensaje("No se pudo conectar con el servidor.", "error");
    }
});

// Editar alumno (SOAP vía REST)
document.getElementById("btn-editar-alumno").addEventListener("click", async () => {
    limpiarMensaje();

    const matricula = inputMatricula.value.trim();
    const payload = {
        nombre: inputNombre.value.trim(),
        programa: inputPrograma.value.trim()
    };

    if (!matricula || !payload.nombre || !payload.programa) {
        mostrarMensaje("Completa matrícula, nombre y programa.", "error");
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/alumnos/${encodeURIComponent(matricula)}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) mostrarMensaje("Error al editar alumno", "error");
        else mostrarMensaje("Alumno editado correctamente");

        manejarRespuestaJson(data, preAlumno);

    } catch (err) {
        mostrarMensaje("No se pudo conectar con el servidor.", "error");
    }
});

// ========================== CURSOS ==========================

const inputCursoNombre = document.getElementById("curso-nombre");
const inputCursoFecha = document.getElementById("curso-fecha-inicio");
const inputCursoMatricula = document.getElementById("curso-matricula");
const inputCursoIdEliminar = document.getElementById("curso-id-eliminar");
const preCurso = document.getElementById("curso-respuesta");

// Registrar curso
document.getElementById("btn-registrar-curso").addEventListener("click", async () => {
    limpiarMensaje();

    const payload = {
        nombre: inputCursoNombre.value.trim(),
        fechaInicio: inputCursoFecha.value,
        matricula: inputCursoMatricula.value.trim()
    };

    if (!payload.nombre || !payload.fechaInicio || !payload.matricula) {
        mostrarMensaje("Completa todos los campos del curso.", "error");
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/cursos`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) mostrarMensaje("Error al registrar curso", "error");
        else mostrarMensaje("Curso registrado correctamente");

        manejarRespuestaJson(data, preCurso);

    } catch (err) {
        mostrarMensaje("Error al conectar con el servidor.", "error");
    }
});

// Eliminar curso por ID
document.getElementById("btn-eliminar-curso-id").addEventListener("click", async () => {
    limpiarMensaje();

    const id = inputCursoIdEliminar.value;

    if (!id) {
        mostrarMensaje("Ingresa el ID del curso.", "error");
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/cursos/${encodeURIComponent(id)}`, {
            method: "DELETE"
        });

        const text = await res.text();
        let data;

        try { data = JSON.parse(text); }
        catch { data = { mensaje: text }; }

        if (!res.ok) mostrarMensaje("Error al eliminar curso", "error");
        else mostrarMensaje("Curso eliminado correctamente");

        manejarRespuestaJson(data, preCurso);

    } catch (err) {
        mostrarMensaje("Error al conectar con el servidor.", "error");
    }
});
