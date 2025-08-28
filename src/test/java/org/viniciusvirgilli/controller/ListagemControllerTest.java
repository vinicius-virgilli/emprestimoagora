package org.viniciusvirgilli.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.viniciusvirgilli.dto.ListagemSimulacoesResponseDTO;
import org.viniciusvirgilli.dto.SimulacaoResumoDTO;
import org.viniciusvirgilli.enums.TipoSimulacao;
import org.viniciusvirgilli.service.ProcessaSimulacaoService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("Testes para ListagemController")
class ListagemControllerTest {

    @InjectMock
    ProcessaSimulacaoService processaSimulacaoService;

    @Nested
    @DisplayName("Testes do método listarSimulacoes")
    class ListarSimulacoesTests {

        @Test
        @DisplayName("Deve retornar lista de simulações com sucesso")
        void deveRetornarListaSimulacoesComSucesso() {
            // Arrange
            List<SimulacaoResumoDTO> simulacoes = Arrays.asList(
                SimulacaoResumoDTO.builder()
                    .idSimulacao(1L)
                    .valorDesejado(new BigDecimal("10000.00"))
                    .prazo((short) 12)
                    .valorTotalParcelas(new BigDecimal("11200.00"))
                    .build(),
                SimulacaoResumoDTO.builder()
                    .idSimulacao(2L)
                    .valorDesejado(new BigDecimal("5000.00"))
                    .prazo((short) 24)
                    .valorTotalParcelas(new BigDecimal("6000.00"))
                    .build()
            );

            ListagemSimulacoesResponseDTO responseDTO = ListagemSimulacoesResponseDTO.builder()
                .pagina(1)
                .qtdRegistros(2)
                .qtdRegistrosPagina(10)
                .registros(simulacoes)
                .build();

            when(processaSimulacaoService.buscaPaginada(anyInt(), anyInt(), any(TipoSimulacao.class)))
                .thenReturn(responseDTO);

            // Act & Assert
            given()
                .queryParam("pagina", 1)
                .queryParam("tamanhoPagina", 10)
                .queryParam("tipoEmprestimo", "PRICE")
                .contentType(ContentType.JSON)
            .when()
                .get("/api/simulacoes/paginada")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("pagina", equalTo(1))
                .body("qtdRegistros", equalTo(2))
                .body("qtdRegistrosPagina", equalTo(10))
                .body("registros", hasSize(2))
                .body("registros[0].idSimulacao", equalTo(1))
                .body("registros[0].valorDesejado", equalTo(10000.00f))
                .body("registros[0].Prazo", equalTo(12))
                .body("registros[0].valorTotalParcelas", equalTo(11200.00f))
                .body("registros[1].idSimulacao", equalTo(2))
                .body("registros[1].valorDesejado", equalTo(5000.00f))
                .body("registros[1].Prazo", equalTo(24))
                .body("registros[1].valorTotalParcelas", equalTo(6000.00f));

            verify(processaSimulacaoService, times(1)).buscaPaginada(1, 10, TipoSimulacao.PRICE);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há simulações")
        void deveRetornarListaVaziaQuandoNaoHaSimulacoes() {
            // Arrange
            ListagemSimulacoesResponseDTO responseDTO = ListagemSimulacoesResponseDTO.builder()
                .pagina(1)
                .qtdRegistros(0)
                .qtdRegistrosPagina(10)
                .registros(Collections.emptyList())
                .build();

            when(processaSimulacaoService.buscaPaginada(anyInt(), anyInt(), any(TipoSimulacao.class)))
                .thenReturn(responseDTO);

            // Act & Assert
            given()
                .queryParam("pagina", 1)
                .queryParam("tamanhoPagina", 10)
                .queryParam("tipoEmprestimo", "PRICE")
                .contentType(ContentType.JSON)
            .when()
                .get("/api/simulacoes/paginada")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("pagina", equalTo(1))
                .body("qtdRegistros", equalTo(0))
                .body("qtdRegistrosPagina", equalTo(10))
                .body("registros", hasSize(0));

            verify(processaSimulacaoService, times(1)).buscaPaginada(1, 10, TipoSimulacao.PRICE);
        }

        @Test
        @DisplayName("Deve usar valores padrão quando parâmetros não são informados")
        void deveUsarValoresPadraoQuandoParametrosNaoSaoInformados() {
            // Arrange
            ListagemSimulacoesResponseDTO responseDTO = ListagemSimulacoesResponseDTO.builder()
                .pagina(1)
                .qtdRegistros(1)
                .qtdRegistrosPagina(20)
                .registros(Arrays.asList(
                    SimulacaoResumoDTO.builder()
                        .idSimulacao(1L)
                        .valorDesejado(new BigDecimal("15000.00"))
                        .prazo((short) 36)
                        .valorTotalParcelas(new BigDecimal("18000.00"))
                        .build()
                ))
                .build();

            when(processaSimulacaoService.buscaPaginada(anyInt(), anyInt(), eq(TipoSimulacao.PRICE)))
                .thenReturn(responseDTO);

            // Act & Assert
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/simulacoes/paginada")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("pagina", equalTo(1))
                .body("qtdRegistros", equalTo(1))
                .body("qtdRegistrosPagina", equalTo(20))
                .body("registros", hasSize(1));

            verify(processaSimulacaoService, times(1)).buscaPaginada(0, 20, TipoSimulacao.PRICE);
        }

        @Test
        @DisplayName("Deve retornar simulações com diferentes tipos de empréstimo")
        void deveRetornarSimulacoesComDiferentesTiposEmprestimo() {
            // Arrange
            List<SimulacaoResumoDTO> simulacoes = Arrays.asList(
                SimulacaoResumoDTO.builder()
                    .idSimulacao(3L)
                    .valorDesejado(new BigDecimal("25000.00"))
                    .prazo((short) 48)
                    .valorTotalParcelas(new BigDecimal("32000.00"))
                    .build()
            );

            ListagemSimulacoesResponseDTO responseDTO = ListagemSimulacoesResponseDTO.builder()
                .pagina(2)
                .qtdRegistros(1)
                .qtdRegistrosPagina(5)
                .registros(simulacoes)
                .build();

            when(processaSimulacaoService.buscaPaginada(anyInt(), anyInt(), eq(TipoSimulacao.SAC)))
                .thenReturn(responseDTO);

            // Act & Assert
            given()
                .queryParam("pagina", 2)
                .queryParam("tamanhoPagina", 5)
                .queryParam("tipoEmprestimo", "SAC")
                .contentType(ContentType.JSON)
            .when()
                .get("/api/simulacoes/paginada")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("pagina", equalTo(2))
                .body("qtdRegistros", equalTo(1))
                .body("qtdRegistrosPagina", equalTo(5))
                .body("registros", hasSize(1))
                .body("registros[0].idSimulacao", equalTo(3))
                .body("registros[0].valorDesejado", equalTo(25000.00f))
                .body("registros[0].Prazo", equalTo(48))
                .body("registros[0].valorTotalParcelas", equalTo(32000.00f));

            verify(processaSimulacaoService, times(1)).buscaPaginada(2, 5, TipoSimulacao.SAC);
        }

        @Test
        @DisplayName("Deve validar estrutura completa da resposta JSON")
        void deveValidarEstruturaCompletaRespostaJson() {
            // Arrange
            SimulacaoResumoDTO simulacao = SimulacaoResumoDTO.builder()
                .idSimulacao(100L)
                .valorDesejado(new BigDecimal("50000.00"))
                .prazo((short) 60)
                .valorTotalParcelas(new BigDecimal("75000.00"))
                .build();

            ListagemSimulacoesResponseDTO responseDTO = ListagemSimulacoesResponseDTO.builder()
                .pagina(3)
                .qtdRegistros(1)
                .qtdRegistrosPagina(1)
                .registros(Arrays.asList(simulacao))
                .build();

            when(processaSimulacaoService.buscaPaginada(anyInt(), anyInt(), any(TipoSimulacao.class)))
                .thenReturn(responseDTO);

            // Act & Assert
            given()
                .queryParam("pagina", 3)
                .queryParam("tamanhoPagina", 1)
                .queryParam("tipoEmprestimo", "PRICE")
                .contentType(ContentType.JSON)
            .when()
                .get("/api/simulacoes/paginada")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasKey("pagina"))
                .body("$", hasKey("qtdRegistros"))
                .body("$", hasKey("qtdRegistrosPagina"))
                .body("$", hasKey("registros"))
                .body("registros[0]", hasKey("idSimulacao"))
                .body("registros[0]", hasKey("valorDesejado"))
                .body("registros[0]", hasKey("Prazo"))
                .body("registros[0]", hasKey("valorTotalParcelas"));

            verify(processaSimulacaoService, times(1)).buscaPaginada(3, 1, TipoSimulacao.PRICE);
        }
    }

    @Nested
    @DisplayName("Testes de cenários de erro")
    class ErrorTests {

        @Test
        @DisplayName("Deve retornar erro 500 quando ProcessaSimulacaoService lança RuntimeException")
        void deveRetornarErro500QuandoServiceLancaRuntimeException() {
            // Arrange
            when(processaSimulacaoService.buscaPaginada(anyInt(), anyInt(), any(TipoSimulacao.class)))
                .thenThrow(new RuntimeException("Erro interno do serviço"));

            // Act & Assert
            given()
                .queryParam("pagina", 1)
                .queryParam("tamanhoPagina", 10)
                .queryParam("tipoEmprestimo", "PRICE")
                .contentType(ContentType.JSON)
            .when()
                .get("/api/simulacoes/paginada")
            .then()
                .statusCode(500);

            verify(processaSimulacaoService, times(1)).buscaPaginada(1, 10, TipoSimulacao.PRICE);
        }

        @Test
        @DisplayName("Deve retornar erro 500 quando ProcessaSimulacaoService lança IllegalStateException")
        void deveRetornarErro500QuandoServiceLancaIllegalStateException() {
            // Arrange
            when(processaSimulacaoService.buscaPaginada(anyInt(), anyInt(), any(TipoSimulacao.class)))
                .thenThrow(new IllegalStateException("Estado inválido do serviço"));

            // Act & Assert
            given()
                .queryParam("pagina", 2)
                .queryParam("tamanhoPagina", 5)
                .queryParam("tipoEmprestimo", "SAC")
                .contentType(ContentType.JSON)
            .when()
                .get("/api/simulacoes/paginada")
            .then()
                .statusCode(500);

            verify(processaSimulacaoService, times(1)).buscaPaginada(2, 5, TipoSimulacao.SAC);
        }

        @Test
        @DisplayName("Deve retornar erro 500 quando ProcessaSimulacaoService lança NullPointerException")
        void deveRetornarErro500QuandoServiceLancaNullPointerException() {
            // Arrange
            when(processaSimulacaoService.buscaPaginada(anyInt(), anyInt(), any(TipoSimulacao.class)))
                .thenThrow(new NullPointerException("Referência nula no serviço"));

            // Act & Assert
            given()
                .queryParam("pagina", 1)
                .queryParam("tamanhoPagina", 20)
                .contentType(ContentType.JSON)
            .when()
                .get("/api/simulacoes/paginada")
            .then()
                .statusCode(500);

            verify(processaSimulacaoService, times(1)).buscaPaginada(1, 20, TipoSimulacao.PRICE);
        }

    }

    @Nested
    @DisplayName("Testes de validação de parâmetros")
    class ParameterValidationTests {

        @Test
        @DisplayName("Deve aceitar parâmetros válidos de paginação")
        void deveAceitarParametrosValidosPaginacao() {
            // Arrange
            ListagemSimulacoesResponseDTO responseDTO = ListagemSimulacoesResponseDTO.builder()
                .pagina(10)
                .qtdRegistros(0)
                .qtdRegistrosPagina(50)
                .registros(Collections.emptyList())
                .build();

            when(processaSimulacaoService.buscaPaginada(anyInt(), anyInt(), any(TipoSimulacao.class)))
                .thenReturn(responseDTO);

            // Act & Assert
            given()
                .queryParam("pagina", 10)
                .queryParam("tamanhoPagina", 50)
                .queryParam("tipoEmprestimo", "SAC")
                .contentType(ContentType.JSON)
            .when()
                .get("/api/simulacoes/paginada")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("pagina", equalTo(10))
                .body("qtdRegistrosPagina", equalTo(50));

            verify(processaSimulacaoService, times(1)).buscaPaginada(10, 50, TipoSimulacao.SAC);
        }

        @Test
        @DisplayName("Deve aceitar tipo de empréstimo padrão")
        void deveAceitarTipoEmprestimoPadrao() {
            // Arrange
            ListagemSimulacoesResponseDTO responseDTO = ListagemSimulacoesResponseDTO.builder()
                .pagina(1)
                .qtdRegistros(0)
                .qtdRegistrosPagina(10)
                .registros(Collections.emptyList())
                .build();

            when(processaSimulacaoService.buscaPaginada(anyInt(), anyInt(), eq(TipoSimulacao.PRICE)))
                .thenReturn(responseDTO);

            // Act & Assert
            given()
                .queryParam("pagina", 1)
                .queryParam("tamanhoPagina", 10)
                .contentType(ContentType.JSON)
            .when()
                .get("/api/simulacoes/paginada")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON);

            verify(processaSimulacaoService, times(1)).buscaPaginada(1, 10, TipoSimulacao.PRICE);
        }
    }
}