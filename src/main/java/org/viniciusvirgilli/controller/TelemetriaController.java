package org.viniciusvirgilli.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.viniciusvirgilli.dto.TelemetriaResponseDTO;
import org.viniciusvirgilli.dto.TelemetriaEndpointDTO;
import org.viniciusvirgilli.service.PersistenciaService;
import org.viniciusvirgilli.service.EventHubService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import java.util.ArrayList;

/**
 * Controller REST para consulta de dados de telemetria
 */
@Path("/api/telemetria")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Telemetria", description = "Operações de consulta de dados de telemetria e métricas")
public class TelemetriaController {

    private static final Logger LOGGER = Logger.getLogger(TelemetriaController.class.getName());
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Inject
    PersistenciaService persistenciaService;

    @Inject
    EventHubService eventHubService;

    /**
     * Endpoint para consultar dados de telemetria geral
     * 
     * @return dados de telemetria com volumes e tempos de resposta
     */
    @GET
    @Operation(
        summary = "Consultar telemetria geral",
        description = "Retorna dados de telemetria com volumes, tempos de resposta e estatísticas dos serviços"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Dados de telemetria retornados com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = TelemetriaResponseDTO.class)
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Erro interno do servidor"
        )
    })
    public Response consultarTelemetria() {
        try {
            long startTime = System.currentTimeMillis();
            
            LOGGER.info("Iniciando consulta de telemetria geral");
            
            // Buscar dados de telemetria
            TelemetriaResponseDTO telemetria = persistenciaService.buscarTelemetria(LocalDate.now());
            
            // Adicionar estatísticas do Event Hub
            EventHubService.EventHubStats eventHubStats = eventHubService.obterEstatisticas();
            
            // Enriquecer com dados do Event Hub
            telemetria.getListaEndpoints().add(new TelemetriaEndpointDTO(
                "event-hub",
                eventHubStats.eventosEnviados != null ? eventHubStats.eventosEnviados.intValue() : 0,
                eventHubStats.tempoMedioEnvio != null ? eventHubStats.tempoMedioEnvio.intValue() : 0,
                50, // tempo mínimo simulado
                200, // tempo máximo simulado
                java.math.BigDecimal.valueOf(0.95) // percentual de sucesso simulado
            ));
            
            long endTime = System.currentTimeMillis();
            long tempoResposta = endTime - startTime;
            
            // Atualizar data de referência
            telemetria.setDataReferencia(LocalDate.now());
            
            LOGGER.info(String.format("Telemetria consultada com sucesso em %d ms", tempoResposta));
            
            return Response.ok(telemetria).build();
            
        } catch (Exception e) {
            LOGGER.severe("Erro ao consultar telemetria: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("TELEMETRY_ERROR", "Erro ao consultar dados de telemetria"))
                .build();
        }
    }

    /**
     * Endpoint para consultar telemetria de um serviço específico
     * 
     * @param nomeServico nome do serviço para consultar telemetria
     * @return dados de telemetria do serviço específico
     */
    @GET
    @Path("/servico/{nomeServico}")
    @Operation(
        summary = "Consultar telemetria de serviço específico",
        description = "Retorna dados de telemetria de um serviço específico"
    )
    @APIResponse(
        responseCode = "200",
        description = "Telemetria do serviço retornada com sucesso"
    )
    @APIResponse(
        responseCode = "404",
        description = "Serviço não encontrado"
    )
    @APIResponse(
        responseCode = "500",
        description = "Erro interno do servidor"
    )
    public Response consultarTelemetriaServico(
            @Parameter(
                description = "Nome do serviço", 
                example = "simulacao"
            )
            @PathParam("nomeServico") String nomeServico) {
        
        try {
            LOGGER.info("Consultando telemetria do serviço: " + nomeServico);
            
            // Buscar telemetria geral
            TelemetriaResponseDTO telemetriaGeral = persistenciaService.buscarTelemetria(LocalDate.now());
            
            // Filtrar por serviço específico
            TelemetriaEndpointDTO endpointEspecifico = null;
            
            for (TelemetriaEndpointDTO endpoint : telemetriaGeral.getListaEndpoints()) {
                if (endpoint.getNomeApi().equalsIgnoreCase(nomeServico)) {
                    endpointEspecifico = endpoint;
                    break;
                }
            }
            
            // Verificar se encontrou o serviço
            if (endpointEspecifico == null) {
                LOGGER.warning("Serviço não encontrado: " + nomeServico);
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("SERVICE_NOT_FOUND", 
                        "Serviço '" + nomeServico + "' não encontrado"))
                    .build();
            }
            
            // Criar resposta específica
            TelemetriaResponseDTO resposta = new TelemetriaResponseDTO();
            resposta.setDataReferencia(LocalDate.now());
            resposta.setListaEndpoints(new ArrayList<>());
            resposta.getListaEndpoints().add(endpointEspecifico);
            
            LOGGER.info("Telemetria do serviço '" + nomeServico + "' retornada com sucesso");
            
            return Response.ok(resposta).build();
            
        } catch (Exception e) {
            LOGGER.severe("Erro ao consultar telemetria do serviço: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("SERVICE_TELEMETRY_ERROR", 
                    "Erro ao consultar telemetria do serviço"))
                .build();
        }
    }

    /**
     * Endpoint para consultar métricas de performance
     * 
     * @return métricas de performance do sistema
     */
    @GET
    @Path("/performance")
    @Operation(
        summary = "Consultar métricas de performance",
        description = "Retorna métricas detalhadas de performance do sistema"
    )
    @APIResponse(
        responseCode = "200",
        description = "Métricas de performance retornadas com sucesso"
    )
    public Response consultarPerformance() {
        try {
            LOGGER.info("Consultando métricas de performance");
            
            // Obter informações de runtime
            Runtime runtime = Runtime.getRuntime();
            long memoriaTotal = runtime.totalMemory();
            long memoriaLivre = runtime.freeMemory();
            long memoriaUsada = memoriaTotal - memoriaLivre;
            long memoriaMaxima = runtime.maxMemory();
            
            // Criar resposta de performance
            PerformanceResponse performance = new PerformanceResponse();
            performance.dataHoraConsulta = LocalDateTime.now().format(DATETIME_FORMATTER);
            performance.memoriaUsadaMB = memoriaUsada / (1024 * 1024);
            performance.memoriaLivreMB = memoriaLivre / (1024 * 1024);
            performance.memoriaTotalMB = memoriaTotal / (1024 * 1024);
            performance.memoriaMaximaMB = memoriaMaxima / (1024 * 1024);
            performance.percentualUsoMemoria = (double) memoriaUsada / memoriaTotal * 100;
            performance.processadoresDisponiveis = runtime.availableProcessors();
            
            // Adicionar estatísticas do Event Hub
            EventHubService.EventHubStats eventHubStats = eventHubService.obterEstatisticas();
            performance.eventHubEventosEnviados = eventHubStats.eventosEnviados != null ? eventHubStats.eventosEnviados : 0L;
            performance.eventHubTempoMedio = eventHubStats.tempoMedioEnvio != null ? eventHubStats.tempoMedioEnvio : 0.0;
            performance.eventHubErros = eventHubStats.eventosComErro != null ? eventHubStats.eventosComErro : 0L;
            
            LOGGER.info(String.format("Performance consultada - Memória: %.1f%% usada", 
                performance.percentualUsoMemoria));
            
            return Response.ok(performance).build();
            
        } catch (Exception e) {
            LOGGER.severe("Erro ao consultar performance: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("PERFORMANCE_ERROR", "Erro ao consultar métricas de performance"))
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
    public Response health() {
        try {
            // Fazer uma consulta simples para verificar conectividade
            persistenciaService.buscarTelemetria(LocalDate.now());
            eventHubService.obterEstatisticas();
            
            return Response.ok(new HealthResponse("OK", "Serviço de telemetria funcionando")).build();
            
        } catch (Exception e) {
            LOGGER.severe("Erro no health check da telemetria: " + e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(new HealthResponse("ERROR", "Serviço de telemetria indisponível"))
                .build();
        }
    }

    /**
     * Classe para resposta de erro
     */
    public static class ErrorResponse {
        public String codigo;
        public String mensagem;

        public ErrorResponse() {}

        public ErrorResponse(String codigo, String mensagem) {
            this.codigo = codigo;
            this.mensagem = mensagem;
        }
    }

    /**
     * Classe para resposta de saúde
     */
    public static class HealthResponse {
        public String status;
        public String mensagem;

        public HealthResponse() {}

        public HealthResponse(String status, String mensagem) {
            this.status = status;
            this.mensagem = mensagem;
        }
    }

    /**
     * Classe para resposta de performance
     */
    public static class PerformanceResponse {
        public String dataHoraConsulta;
        public long memoriaUsadaMB;
        public long memoriaLivreMB;
        public long memoriaTotalMB;
        public long memoriaMaximaMB;
        public double percentualUsoMemoria;
        public int processadoresDisponiveis;
        public long eventHubEventosEnviados;
        public double eventHubTempoMedio;
        public long eventHubErros;

        public PerformanceResponse() {}
    }
}