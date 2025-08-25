package org.viniciusvirgilli.controller;

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
import org.viniciusvirgilli.service.ProcessaSimulacaoService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;

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
    MeterRegistry meterRegistry;

    // Métricas do endpoint de simulação
    private Timer simulacaoTimer;
    private Counter simulacaoRequestCounter;
    private Counter simulacaoSuccessCounter;
    private Counter simulacaoErrorCounter;

    @jakarta.annotation.PostConstruct
    public void initMetrics() {
        simulacaoTimer = Timer.builder("simulacao.processar.duration")
                .description("Tempo de resposta do endpoint POST /api/simulacao/processar")
                .tag("endpoint", "POST /api/simulacao/processar")
                .register(meterRegistry);

        simulacaoRequestCounter = Counter.builder("simulacao.processar.requests.total")
                .description("Número total de requisições ao endpoint POST /api/simulacao/processar")
                .tag("endpoint", "POST /api/simulacao/processar")
                .register(meterRegistry);

        simulacaoSuccessCounter = Counter.builder("simulacao.processar.requests.success")
                .description("Número de requisições bem-sucedidas ao endpoint POST /api/simulacao/processar")
                .tag("endpoint", "POST /api/simulacao/processar")
                .tag("status", "success")
                .register(meterRegistry);

        simulacaoErrorCounter = Counter.builder("simulacao.processar.requests.error")
                .description("Número de requisições com erro ao endpoint POST /api/simulacao/processar")
                .tag("endpoint", "POST /api/simulacao/processar")
                .tag("status", "error")
                .register(meterRegistry);
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
        long inicio = System.currentTimeMillis();

        simulacaoRequestCounter.increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            log.info(" [REQUISICAO][SIMULACAO] - Iniciando requisicao de simulacao: {}", requestDTO.toString());

            SimulacaoResponseDTO simulacao = processaSimulacaoService.executar(requestDTO);

            simulacaoSuccessCounter.increment();
            sample.stop(simulacaoTimer);

            long tempoExecucao = System.currentTimeMillis() - inicio;
            log.info(" [REQUISICAO][SIMULACAO] - Finalizando requisicao de simulacao com ID: {} em {}ms\n", simulacao.getIdSimulacao(), tempoExecucao);

            return Response.ok(simulacao).build();
            
        } catch (APIEmprestimoAgoraException exception) {
            log.warn("[REQUISICAO][SIMULACAO] - Erro na requisicao" + exception.getMessage() + System.lineSeparator());

            simulacaoErrorCounter.increment();
            sample.stop(simulacaoTimer);
            
            throw exception;
                
        } catch (Exception e) {
            log.warn("[REQUISICAO][SIMULACAO] - Erro na requisicao: " + e.getMessage() + System.lineSeparator());

            simulacaoErrorCounter.increment();
            sample.stop(simulacaoTimer);

            throw e;
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
}