package org.viniciusvirgilli.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.viniciusvirgilli.dto.ListagemSimulacoesResponseDTO;
import org.viniciusvirgilli.enums.TipoSimulacao;
import org.viniciusvirgilli.service.PersistenciaService;
import org.viniciusvirgilli.service.ProcessaSimulacaoService;

import java.util.logging.Logger;

/**
 * Controller REST para listagem de simulações realizadas
 */
@Path("/api/simulacoes")
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Listagem", description = "Operações de listagem de simulações realizadas")
public class ListagemController {

    @Inject
    ProcessaSimulacaoService processaSimulacaoService;

    /**
     * Endpoint para listar todas as simulações realizadas com paginação
     * 
     * @param pagina número da página (padrão: 0)
     * @param tamanhoPagina tamanho da página (padrão: 20)
     * @return lista paginada de simulações
     */
    @GET
    @Path("/paginada")
    @Operation(
        summary = "Listar simulações realizadas",
        description = "Retorna uma lista paginada de todas as simulacoes de emprestimo realizadas"
    )
    @APIResponse(
        responseCode = "200",
        description = "Lista de simulacoes retornada com sucesso",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = ListagemSimulacoesResponseDTO.class)
        )
    )
    @APIResponse(
        responseCode = "400",
        description = "Parametros de paginacao invalidos"
    )
    @APIResponse(
        responseCode = "500",
        description = "Erro interno do servidor"
    )
    public Response listarSimulacoes(
            @Parameter(description = "Numero da pagina (começando em 0)", example = "0")
            @QueryParam("pagina") @DefaultValue("0") int pagina,
            
            @Parameter(description = "Tamanho da pagina", example = "20")
            @QueryParam("tamanhoPagina") @DefaultValue("20") int tamanhoPagina,

            @Parameter(description = "Tipo de empréstimo", example = "PRICE")
            @QueryParam("tipoEmprestimo") @DefaultValue("PRICE") TipoSimulacao tipoEmprestimo) {

        long inicio = System.currentTimeMillis();
        
        try {
            log.info("[REQUISICAO][LISTAGEM PAGINADA] - Iniciando listagem paginada de simulações");

            ListagemSimulacoesResponseDTO resultado = processaSimulacaoService.buscaPaginada(pagina, tamanhoPagina, tipoEmprestimo);
            
            log.info(String.format("Encontradas %d simulações na página %d",
                resultado.getQtdRegistrosPagina(), pagina));

            long tempoExecucao = System.currentTimeMillis() - inicio;
            log.info("[REQUISICAO][LISTAGEM PAGINADA] - Finalizando a listagem paginada em {}ms\n", tempoExecucao);

            return Response.ok(resultado).build();
            
        } catch (Exception e) {
            log.error("Erro ao listar simulações: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "Erro interno do servidor"))
                .build();
        }
    }

//    /**
//     * Endpoint para buscar uma simulação específica por ID
//     *
//     * @param idSimulacao ID da simulação
//     * @return dados da simulação
//     */
//    @GET
//    @Path("/{idSimulacao}")
//    @Operation(
//        summary = "Buscar simulação por ID",
//        description = "Retorna os dados de uma simulação específica pelo seu ID"
//    )
//    @APIResponse(
//        responseCode = "200",
//        description = "Simulação encontrada com sucesso"
//    )
//    @APIResponse(
//        responseCode = "404",
//        description = "Simulação não encontrada"
//    )
//    @APIResponse(
//        responseCode = "500",
//        description = "Erro interno do servidor"
//    )
//    public Response buscarSimulacaoPorId(
//            @Parameter(description = "ID da simulação", example = "123456789")
//            @PathParam("idSimulacao") Long idSimulacao) {
//
//        try {
//            log.info("Buscando simulação com ID: " + idSimulacao);
//
//            // var simulacao = persistenciaService.buscarSimulacaoPorId(idSimulacao);
//
//            // log.info("Simulação encontrada: " + simulacao.getIdSimulacao());
//
//            // return Response.ok(simulacao).build();
//
//        } catch (IllegalArgumentException e) {
//            log.warn("Simulação não encontrada: " + idSimulacao);
//            return Response.status(Response.Status.NOT_FOUND)
//                .entity(new ErrorResponse("NOT_FOUND", "Simulação não encontrada"))
//                .build();
//
//        } catch (Exception e) {
//            log.error("Erro ao buscar simulação: " + e.getMessage());
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                .entity(new ErrorResponse("INTERNAL_ERROR", "Erro interno do servidor"))
//                .build();
//        }
//    }

    /**
     * Endpoint para verificar saúde do serviço de listagem
     */
    @GET
    @Path("/health")
    @Operation(
        summary = "Verificar saúde do serviço de listagem",
        description = "Endpoint para verificar se o serviço de listagem está funcionando"
    )
    public Response health() {
        try {
            // persistenciaService.buscarTodasSimulacoes(0, 1);
            
            return Response.ok(new HealthResponse("OK", "Serviço de listagem funcionando")).build();
            
        } catch (Exception e) {
            log.error("Erro no health check: " + e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(new HealthResponse("ERROR", "Serviço de listagem indisponível"))
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