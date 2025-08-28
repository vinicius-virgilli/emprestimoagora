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
import org.viniciusvirgilli.dto.HealthResponseDTO;
import org.viniciusvirgilli.dto.VolumeDiarioResponseDTO;
import org.viniciusvirgilli.enums.TipoSimulacao;
import org.viniciusvirgilli.service.ProcessaSimulacaoService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
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
    ProcessaSimulacaoService processaSimulacaoService;

    /**
     * Endpoint para consultar volume diário por produto
     *
     * @return volume simulado por produto na data especificada
     */
    @GET
    @Path("/diario")
    @Operation(
        summary = "Consultar volume diário por produto",
        description = "Retorna o volume de simulações realizadas para um produto em uma data específica"
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
            @QueryParam("dataReferencia") LocalDate dataReferencia,
            
            @Parameter(
                description = "Código do produto (obrigatório)", 
                example = "1",
                required = true
            )
            @QueryParam("codigoProduto") Integer codigoProduto,
            
            @Parameter(
                description = "Tipo de cálculo (obrigatório)", 
                example = "PRICE",
                required = false
            )
            @QueryParam("tipoSimulacao") @DefaultValue("PRICE") TipoSimulacao tipoSimulacao) {
        
        try {
            
            LOGGER.info("Consultando volume diário para data: " + dataReferencia);
            
            VolumeDiarioResponseDTO resultado = processaSimulacaoService.buscarVolumePorProdutoPorDia(dataReferencia, codigoProduto, tipoSimulacao);
            
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
     * Endpoint para verificar saúde do serviço de volume
     */
    @GET
    @Path("/health")
    @Operation(
        summary = "Verificar saúde do serviço de volume",
        description = "Endpoint para verificar se o serviço de volume está funcionando"
    )
    @APIResponse(
        responseCode = "200",
        description = "Serviço funcionando corretamente",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = HealthResponseDTO.class)
        )
    )
    @APIResponse(
        responseCode = "503",
        description = "Serviço indisponível"
    )
    public Response health() {
        try {
            // Fazer uma consulta simples para verificar conectividade
            processaSimulacaoService.buscarVolumePorProdutoPorDia(LocalDate.now(), 1, TipoSimulacao.PRICE);

            return Response.ok(HealthResponseDTO.builder()
                    .status("OK")
                    .mensagem("Serviço de volume funcionando")
                    .build()).build();

        } catch (Exception e) {
            LOGGER.severe("Erro no health check do volume: " + e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(HealthResponseDTO.builder()
                        .status("ERROR")
                        .mensagem("Serviço de volume indisponível: " + e.getMessage())
                        .build())
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

}