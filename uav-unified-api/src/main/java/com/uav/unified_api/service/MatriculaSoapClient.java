package com.uav.unified_api.service;
import com.uav.unified_api.model.Alumnomodel;

import tools.jackson.databind.ObjectMapper;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MatriculaSoapClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // URL del servicio SOAP en Python
    private static final String SOAP_URL = "http://localhost:8000";

    public MatriculaSoapClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        headers.add("SOAPAction", "obtenerAlumnoPorMatricula");

        HttpEntity<String> request = new HttpEntity<>(soapBody, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(SOAP_URL, request, String.class);

        try {
            String rawBody = response.getBody();
            if (rawBody == null) return null;

            // Buscar el JSON dentro del XML SOAP
            int start = rawBody.indexOf("{");
            int end = rawBody.lastIndexOf("}") + 1;
            if (start == -1 || end == -1) return null;

            String json = rawBody.substring(start, end);
            return objectMapper.readValue(json, Alumnomodel.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
