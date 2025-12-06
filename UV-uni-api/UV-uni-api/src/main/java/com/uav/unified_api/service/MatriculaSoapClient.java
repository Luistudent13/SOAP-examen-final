package com.uav.unified_api.service;

import com.uav.unified_api.model.Alumnomodel;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class MatriculaSoapClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Ajusta si tu SOAP corre en otra URL
    private static final String SOAP_URL = "http://127.0.0.1:8000";

    public MatriculaSoapClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ========= CONSULTAR alumno (si lo usas) =========
    public Alumnomodel obtenerAlumnoPorMatricula(String matricula) {
        String soapBody = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:mat="http://uav.mx/matriculas">
                   <soapenv:Header/>
                   <soapenv:Body>
                      <mat:obtenerAlumnoPorMatricula>
                         <mat:matricula>%s</mat:matricula>
                      </mat:obtenerAlumnoPorMatricula>
                   </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(matricula);

        return enviarSolicitudSoap(soapBody, "obtenerAlumnoPorMatricula");
    }

    // ========= REGISTRAR alumno (lo que usa tu POST /api/alumnos) =========
    public Alumnomodel registrarAlumno(Alumnomodel alumno) {

        String soapBody = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:mat="http://uav.mx/matriculas">
                   <soapenv:Header/>
                   <soapenv:Body>
                      <mat:registrarAlumno>
                         <mat:matricula>%s</mat:matricula>
                         <mat:nombre>%s</mat:nombre>
                         <mat:programa>%s</mat:programa>
                      </mat:registrarAlumno>
                   </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(
                        alumno.getMatricula(),
                        alumno.getNombre(),
                        alumno.getPrograma()
                );

        return enviarSolicitudSoap(soapBody, "registrarAlumno");
    }

    // ========= MÉTODO COMÚN PARA ENVIAR Y PARSEAR =========
    private Alumnomodel enviarSolicitudSoap(String soapBody, String soapAction) {

        HttpHeaders headers = new HttpHeaders();
        // MUY IMPORTANTE: forzar UTF-8
        MediaType mediaType = new MediaType("text", "xml", StandardCharsets.UTF_8);
        headers.setContentType(mediaType);
        headers.setAccept(List.of(mediaType));
        headers.add("SOAPAction", soapAction);

        HttpEntity<String> request = new HttpEntity<>(soapBody, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(SOAP_URL, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Respuesta SOAP no exitosa: " + response.getStatusCode());
                return null;
            }

            return parseAlumnoFromSoap(response.getBody());

        } catch (HttpStatusCodeException ex) {
            // Aquí puedes ver exactamente qué regresó el SOAP
            System.out.println("ERROR SOAP (" + ex.getStatusCode() + "):");
            System.out.println(ex.getResponseBodyAsString());
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // ========= EXTRAER JSON DEL SOAP Y MAPEAR A Alumnomodel =========
    private Alumnomodel parseAlumnoFromSoap(String rawBody) {
        if (rawBody == null) return null;

        try {
            int start = rawBody.indexOf("{");
            int end = rawBody.lastIndexOf("}") + 1;

            if (start == -1 || end == 0) {
                // No hay JSON en la respuesta
                return null;
            }

            String json = rawBody.substring(start, end);
            System.out.println("JSON devuelto por SOAP: " + json);

            // Si es un JSON de error, no intentamos mapearlo a Alumnomodel
            if (json.contains("\"error\"")) {
                return null;
            }

            return objectMapper.readValue(json, Alumnomodel.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
