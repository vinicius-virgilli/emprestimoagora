package org.viniciusvirgilli.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.viniciusvirgilli.dto.HealthResponseDTO;
import org.viniciusvirgilli.dto.TelemetriaResponseDTO;
import org.viniciusvirgilli.service.MetricasService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Controller REST para operações de telemetria
 */
@Path("/api/telemetria")
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Telemetria", description = "Operações de telemetria e métricas da aplicação")
public class TelemetriaController {

    @Inject
    MetricasService metricasService;

    @GET
    @Operation(
        summary = "Coletar métricas de telemetria",
        description = "Retorna as métricas de telemetria para uma data específica ou data atual"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Métricas coletadas com sucesso"
        ),
        @APIResponse(
            responseCode = "400",
            description = "Formato de data inválido"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Erro interno do servidor"
        )
    })
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

    /**
     * Endpoint para verificar saúde do serviço de telemetria
     */
    @GET
    @Path("/health")
    @Operation(
        summary = "Verificar saúde do serviço de telemetria",
        description = "Endpoint para verificar se o serviço de telemetria está funcionando"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Serviço funcionando corretamente"
        ),
        @APIResponse(
            responseCode = "503",
            description = "Serviço indisponível"
        )
    })
    public Response health() {
        try {
            // Testa conectividade com banco de dados fazendo uma consulta simples
            metricasService.coletarMetricas(LocalDate.now());
            
            return Response.ok(HealthResponseDTO.builder()
                    .status("OK")
                    .mensagem("Serviço de telemetria funcionando")
                    .build()).build();
            
        } catch (Exception e) {
            log.error("Erro no health check de telemetria: " + e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(HealthResponseDTO.builder()
                        .status("ERROR")
                        .mensagem("Serviço de telemetria indisponível: " + e.getMessage())
                        .build())
                .build();
        }
    }
}
