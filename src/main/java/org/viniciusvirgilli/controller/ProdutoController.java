package org.viniciusvirgilli.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.viniciusvirgilli.dto.HealthResponseDTO;
import org.viniciusvirgilli.model.hack.Produto;
import org.viniciusvirgilli.dao.ProdutoDao;

import java.util.List;
import java.util.logging.Logger;

/**
 * Controller REST para operações com produtos
 */
@Path("/api/produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Produtos", description = "Operações de consulta de produtos disponíveis")
public class ProdutoController {

    private static final Logger LOGGER = Logger.getLogger(ProdutoController.class.getName());

    @Inject
    ProdutoDao produtoDao;

    /**
     * Endpoint para listar todos os produtos disponíveis
     * 
     * @return lista de produtos
     */
    @GET
    @Operation(
        summary = "Listar todos os produtos",
        description = "Retorna a lista de todos os produtos disponíveis para simulação"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Lista de produtos retornada com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = Produto.class)
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Erro interno do servidor"
        )
    })
    public Response listarProdutos() {
        try {
            LOGGER.info("Consultando lista de produtos");
            
            List<Produto> produtos = produtoDao.findAll();
            
            LOGGER.info("Encontrados " + produtos.size() + " produtos");
            
            return Response.ok(produtos).build();
            
        } catch (Exception e) {
            LOGGER.severe("Erro ao consultar produtos: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro interno do servidor")
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
        description = "Endpoint para verificar se o serviço de produtos está funcionando"
    )
    public Response health() {
        return Response.ok(HealthResponseDTO.builder()
                .status("OK")
                .mensagem("Serviço de produtos funcionando")
                .build()).build();
    }
}