package org.viniciusvirgilli.controller;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.viniciusvirgilli.dto.SimulacaoRequestDTO;
import org.viniciusvirgilli.dto.SimulacaoResponseDTO;
import org.viniciusvirgilli.exceptions.APIEmprestimoAgoraException;
import org.viniciusvirgilli.service.MetricasService;
import org.viniciusvirgilli.service.ProcessaSimulacaoService;

// OpenTelemetry imports
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributeKey;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Controller REST para operações de simulação de empréstimo
 */
@Path("/api/simulacao")
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Simulação", description = "Operações de simulação de empréstimo")
public class SimulacaoController {

    @Inject
    ProcessaSimulacaoService processaSimulacaoService;

    @Inject
    OpenTelemetry openTelemetry;

    @Inject
    MetricasService metricasService;

    // OpenTelemetry métricas
    private Meter meter;
    private DoubleHistogram httpServerDurationHistogram;
    private LongCounter httpServerRequestsCounter;
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successRequests = new AtomicLong(0);

    // Attribute keys
    private static final AttributeKey<String> ENDPOINT_KEY = AttributeKey.stringKey("endpoint");
    private static final AttributeKey<String> METHOD_KEY = AttributeKey.stringKey("method");
    private static final AttributeKey<String> STATUS_KEY = AttributeKey.stringKey("status");

    @PostConstruct
    public void initMetrics() {
        // Criar meter
        meter = openTelemetry.getMeter("emprestimo-agora");

        // Histogram para duração das requisições (em segundos)
        httpServerDurationHistogram = meter
                .histogramBuilder("http_server_duration_seconds")
                .setDescription("Tempo de resposta das requisições HTTP")
                .setUnit("s")
                .build();

        // Counter para requisições totais
        httpServerRequestsCounter = meter
                .counterBuilder("http_server_requests_total")
                .setDescription("Número total de requisições HTTP")
                .build();

        // Gauge para percentual de sucesso
        meter.gaugeBuilder("http_server_success_rate")
                .setDescription("Percentual de sucesso das requisições")
                .setUnit("%")
                .buildWithCallback(measurement -> {
                    long total = totalRequests.get();
                    long success = successRequests.get();
                    double successRate = total > 0 ? (double) success / total * 100.0 : 0.0;
                    measurement.record(successRate, Attributes.of(
                            ENDPOINT_KEY, "/api/simulacao/processar",
                            METHOD_KEY, "POST"
                    ));
                });
    }

    @POST
    @Path("/processar")
    @Operation(
            summary = "Processar simulação de empréstimo",
            description = "Recebe solicitação de simulação, valida dados, calcula SAC e PRICE, persiste no banco de dados de forma síncrona e retorna resultados"
    )
    @APIResponse(
            responseCode = "200",
            description = "Simulação processada com sucesso",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SimulacaoResponseDTO.class)
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos"
    )
    @APIResponse(
            responseCode = "404",
            description = "Nenhum produto disponivel para os parâmetros informados"
    )
    @APIResponse(
            responseCode = "500",
            description = "Erro interno do servidor ou erro de persistência no banco de dados"
    )
    public Response processarSimulacao(@Valid SimulacaoRequestDTO requestDTO) throws Exception {
        long startTime = System.nanoTime();
        totalRequests.incrementAndGet();

        String status = "500"; // Default para erro
        
        try {
            log.info(" [REQUISICAO][SIMULACAO] - Iniciando requisicao de simulacao: {}", requestDTO);

            SimulacaoResponseDTO simulacao = processaSimulacaoService.executar(requestDTO);

            // Incrementar contador de sucesso
            successRequests.incrementAndGet();
            status = "200";

            long durationNanos = System.nanoTime() - startTime;
            double durationSeconds = durationNanos / 1_000_000_000.0;
            
            log.info(" [REQUISICAO][SIMULACAO] - Finalizando requisicao de simulacao com ID: {} em {}ms",
                    simulacao.getIdSimulacao(), Math.round(durationSeconds * 1000));

            return Response.ok(simulacao).build();

        } catch (APIEmprestimoAgoraException exception) {
            log.warn("[REQUISICAO][SIMULACAO] - Erro na requisicao: {}", exception.getMessage());
            status = "400";
            throw exception;

        } catch (Exception e) {
            log.warn("[REQUISICAO][SIMULACAO] - Erro na requisicao: {}", e.getMessage());
            status = "500";
            throw e;
            
        } // No bloco finally do método processarSimulacao
        finally {
        // Calcular duração em segundos
        long endTime = System.nanoTime();
        double durationSeconds = (endTime - startTime) / 1_000_000_000.0;
        
        // Criar atributos para classificação das métricas
        Attributes attributes = Attributes.of(
            ENDPOINT_KEY, "/api/simulacao/processar",
            METHOD_KEY, "POST",
            STATUS_KEY, status
        );
        
        // Registrar duração no OpenTelemetry
        httpServerDurationHistogram.record(durationSeconds, attributes);
        
        // Registrar contador de requisições no OpenTelemetry
        httpServerRequestsCounter.add(1, attributes);
        
        // Registrar métricas no MetricasService para coleta posterior
        metricasService.registrarRequisicao("/api/simulacao/processar", durationSeconds, Integer.parseInt(status));
        }
    }

    /**
     * Endpoint para verificar saúde do serviço
     */
    @GET
    @Path("/health")
    @Operation(
            summary = "Verificar saúde do serviço",
            description = "Endpoint para verificar se o serviço de simulação está funcionando"
    )
    public Response health() {
        return Response.ok(new HealthResponse("OK", "Serviço de simulação funcionando")).build();
    }

    /**
     * Classe para resposta de erro
     */
    public static class ErrorResponse {
        public String codigo;
        public String mensagem;

        public ErrorResponse() {
        }

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

        public HealthResponse() {
        }

        public HealthResponse(String status, String mensagem) {
            this.status = status;
            this.mensagem = mensagem;
        }
    }
}