package org.viniciusvirgilli.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.viniciusvirgilli.service.MetricasService;
import org.viniciusvirgilli.dto.TelemetriaResponseDTO;

@Path("/api/telemetria")
public class TelemetriaController {

    @Inject
    MetricasService metricasService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTelemetria() {
        try {
            TelemetriaResponseDTO telemetria = metricasService.coletarMetricas();
            return Response.ok(telemetria).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("Erro ao coletar telemetria: " + e.getMessage())
                    .build();
        }
    }
}
