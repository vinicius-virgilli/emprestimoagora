package org.viniciusvirgilli.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.viniciusvirgilli.service.MetricasService;
import org.viniciusvirgilli.dto.TelemetriaResponseDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Path("/api/telemetria")
public class TelemetriaController {

    @Inject
    MetricasService metricasService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTelemetria(@QueryParam("dataReferencia") String dataReferenciaParam) {
        try {
            LocalDate dataReferencia;
            
            // Se o parâmetro não foi fornecido, usa a data atual
            if (dataReferenciaParam == null || dataReferenciaParam.trim().isEmpty()) {
                dataReferencia = LocalDate.now();
            } else {
                // Tenta fazer o parse da data fornecida
                try {
                    dataReferencia = LocalDate.parse(dataReferenciaParam, DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (DateTimeParseException e) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Formato de data inválido. Use o formato yyyy-MM-dd (ex: 2024-01-15)")
                            .build();
                }
            }
            
            TelemetriaResponseDTO telemetria = metricasService.coletarMetricas(dataReferencia);
            return Response.ok(telemetria).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("Erro ao coletar telemetria: " + e.getMessage())
                    .build();
        }
    }
}
