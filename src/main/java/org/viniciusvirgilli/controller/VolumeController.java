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
import org.viniciusvirgilli.dto.VolumeDiarioResponseDTO;
import org.viniciusvirgilli.dto.VolumePeriodoResponseDTO;
import org.viniciusvirgilli.service.PersistenciaService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Logger;

/**
 * Controller REST para consulta de volume diário por produto
 */
@Path("/api/volume")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Volume", description = "Operações de consulta de volume diário por produto")
public class VolumeController {

    private static final Logger LOGGER = Logger.getLogger(VolumeController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Inject
    PersistenciaService persistenciaService;

    /**
     * Endpoint para consultar volume diário por produto
     * 
     * @param dataReferencia data de referência no formato yyyy-MM-dd (padrão: hoje)
     * @return volume simulado por produto na data especificada
     */
    @GET
    @Path("/diario")
    @Operation(
        summary = "Consultar volume diário por produto",
        description = "Retorna o volume de simulações realizadas por produto em uma data específica"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Volume diário retornado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = VolumeDiarioResponseDTO.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Data de referência inválida"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Erro interno do servidor"
        )
    })
    public Response consultarVolumeDiario(
            @Parameter(
                description = "Data de referência no formato yyyy-MM-dd (obrigatório)", 
                example = "2024-01-15",
                required = true
            )
            @QueryParam("dataReferencia") String dataReferenciaStr) {
        
        try {
            // Validar se o parâmetro foi fornecido
            if (dataReferenciaStr == null || dataReferenciaStr.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("MISSING_PARAMETER", 
                        "O parâmetro dataReferencia é obrigatório. Use o formato yyyy-MM-dd"))
                    .build();
            }
            
            // Converter e validar a data
            LocalDate dataReferencia;
            try {
                dataReferencia = LocalDate.parse(dataReferenciaStr, DATE_FORMATTER);
                LOGGER.info("Usando data informada como referência: " + dataReferencia);
            } catch (DateTimeParseException e) {
                LOGGER.warning("Data inválida fornecida: " + dataReferenciaStr);
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_DATE", 
                        "Data deve estar no formato yyyy-MM-dd. Exemplo: 2024-01-15"))
                    .build();
            }
            
            // Validar se a data não é futura
            if (dataReferencia.isAfter(LocalDate.now())) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("FUTURE_DATE", 
                        "Não é possível consultar dados de datas futuras"))
                    .build();
            }
            
            LOGGER.info("Consultando volume diário para data: " + dataReferencia);
            
            // Buscar volume diário
            VolumeDiarioResponseDTO resultado = persistenciaService.buscarVolumeDiario(dataReferencia);
            
            LOGGER.info(String.format("Volume diário encontrado: %d produtos com simulações", 
                resultado.getSimulacoes().size()));
            
            return Response.ok(resultado).build();
            
        } catch (Exception e) {
            LOGGER.severe("Erro ao consultar volume diário: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "Erro interno do servidor"))
                .build();
        }
    }

    /**
     * Endpoint para consultar volume por período
     * 
     * @param dataInicio data de início no formato yyyy-MM-dd
     * @param dataFim data de fim no formato yyyy-MM-dd
     * @return volume agregado por produto no período
     */
    @GET
    @Path("/periodo")
    @Operation(
        summary = "Consultar volume por período",
        description = "Retorna o volume agregado de simulações por produto em um período específico"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Volume do período retornado com sucesso"
        ),
        @APIResponse(
            responseCode = "400",
            description = "Parâmetros de data inválidos"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Erro interno do servidor"
        )
    })
    public Response consultarVolumePorPeriodo(
            @Parameter(
                description = "Data de início no formato yyyy-MM-dd", 
                example = "2024-01-01"
            )
            @QueryParam("dataInicio") String dataInicioStr,
            
            @Parameter(
                description = "Data de fim no formato yyyy-MM-dd", 
                example = "2024-01-31"
            )
            @QueryParam("dataFim") String dataFimStr) {
        
        try {
            // Validar parâmetros obrigatórios
            if (dataInicioStr == null || dataInicioStr.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("MISSING_START_DATE", "Data de início é obrigatória"))
                    .build();
            }
            
            if (dataFimStr == null || dataFimStr.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("MISSING_END_DATE", "Data de fim é obrigatória"))
                    .build();
            }
            
            // Parsear datas
            LocalDate dataInicio, dataFim;
            try {
                dataInicio = LocalDate.parse(dataInicioStr, DATE_FORMATTER);
                dataFim = LocalDate.parse(dataFimStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_DATE_FORMAT", 
                        "Datas devem estar no formato yyyy-MM-dd"))
                    .build();
            }
            
            // Validar período
            if (dataInicio.isAfter(dataFim)) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_PERIOD", 
                        "Data de início deve ser anterior ou igual à data de fim"))
                    .build();
            }
            
            // Validar se o período não é muito longo (máximo 1 ano)
            if (dataInicio.plusYears(1).isBefore(dataFim)) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("PERIOD_TOO_LONG", 
                        "Período máximo permitido é de 1 ano"))
                    .build();
            }
            
            LOGGER.info(String.format("Consultando volume por período: %s a %s", dataInicio, dataFim));
            
            // Consulta por período agregado
            VolumePeriodoResponseDTO resultado = persistenciaService.buscarVolumePorPeriodo(dataInicio, dataFim);
            
            LOGGER.info(String.format("Volume do período encontrado: %d produtos", 
                resultado.getSimulacoes().size()));
            
            return Response.ok(resultado).build();
            
        } catch (Exception e) {
            LOGGER.severe("Erro ao consultar volume por período: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "Erro interno do servidor"))
                .build();
        }
    }

    /**
     * Endpoint para verificar saúde do serviço de volume
     */
    @GET
    @Path("/health")
    @Operation(
        summary = "Verificar saúde do serviço de volume",
        description = "Endpoint para verificar se o serviço de volume está funcionando"
    )
    public Response health() {
        try {
            // Fazer uma consulta simples para verificar conectividade
            persistenciaService.buscarVolumeDiario(LocalDate.now());
            
            return Response.ok(new HealthResponse("OK", "Serviço de volume funcionando")).build();
            
        } catch (Exception e) {
            LOGGER.severe("Erro no health check do volume: " + e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(new HealthResponse("ERROR", "Serviço de volume indisponível"))
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
}