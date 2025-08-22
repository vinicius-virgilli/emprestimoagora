package org.viniciusvirgilli.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.viniciusvirgilli.dto.SimulacaoRequestDTO;
import org.viniciusvirgilli.dto.SimulacaoResponseDTO;
import org.viniciusvirgilli.service.SimulacaoService;
import org.viniciusvirgilli.service.EventHubService;
import org.viniciusvirgilli.service.PersistenciaService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;

import java.util.logging.Logger;
import java.time.Duration;

/**
 * Controller REST para operações de simulação de empréstimo
 */
@Path("/api/simulacao")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Simulação", description = "Operações de simulação de empréstimo")
public class SimulacaoController {

    private static final Logger LOGGER = Logger.getLogger(SimulacaoController.class.getName());

    @Inject
    SimulacaoService simulacaoService;

    @Inject
    EventHubService eventHubService;

    @Inject
    PersistenciaService persistenciaService;

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

    /**
     * Endpoint principal para processar simulação de empréstimo
     * 
     * @param request dados da simulação
     * @return resposta com simulações SAC e PRICE
     */
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
        description = "Nenhum produto disponível para os parâmetros informados"
    )
    @APIResponse(
        responseCode = "500",
        description = "Erro interno do servidor ou erro de persistência no banco de dados"
    )
    public Response processarSimulacao(@Valid SimulacaoRequestDTO request) {
        // Incrementar contador de requisições totais
        simulacaoRequestCounter.increment();
        
        // Medir tempo de resposta
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            LOGGER.info("Iniciando processamento de simulação: " + request.toString());
            
            // 1. Processar simulação usando o service
            SimulacaoResponseDTO response = simulacaoService.processarSimulacao(request);
            
            LOGGER.info("Simulação processada com sucesso. ID: " + response.getIdSimulacao());

            // 2. Persistir simulação no banco local (síncrono - obrigatório)
            persistenciaService.persistirSimulacao(request, response);
            LOGGER.info("Simulação persistida no banco local com sucesso");
            
            // 3. Enviar para Event Hub (assíncrono - opcional)
            try {
                eventHubService.enviarEventoSimulacao(response);
                LOGGER.info("Evento enviado para Event Hub com sucesso");
            } catch (Exception e) {
                LOGGER.warning("Erro ao enviar evento para Event Hub: " + e.getMessage());
                // Não falha a operação se o Event Hub falhar
            }
            
            // Incrementar contador de sucesso
            simulacaoSuccessCounter.increment();
            
            // Registrar tempo de resposta para requisições bem-sucedidas
            sample.stop(simulacaoTimer);
            
            return Response.ok(response).build();
            
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Erro de validação: " + e.getMessage());
            
            // Incrementar contador de erro
            simulacaoErrorCounter.increment();
            
            // Registrar tempo de resposta para requisições com erro
            sample.stop(simulacaoTimer);
            
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("VALIDATION_ERROR", e.getMessage()))
                .build();
                
        } catch (jakarta.persistence.PersistenceException e) {
            LOGGER.severe("Erro ao persistir simulação no banco de dados: " + e.getMessage());
            
            // Incrementar contador de erro
            simulacaoErrorCounter.increment();
            
            // Registrar tempo de resposta para requisições com erro
            sample.stop(simulacaoTimer);
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("PERSISTENCE_ERROR", "Erro ao salvar simulação no banco de dados"))
                .build();
                
        } catch (Exception e) {
            LOGGER.severe("Erro interno ao processar simulação: " + e.getMessage());
            
            // Incrementar contador de erro
            simulacaoErrorCounter.increment();
            
            // Registrar tempo de resposta para requisições com erro
            sample.stop(simulacaoTimer);
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "Erro interno do servidor"))
                .build();
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