package org.viniciusvirgilli.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.viniciusvirgilli.dto.*;
import org.viniciusvirgilli.enums.TipoSimulacao;
import org.viniciusvirgilli.exceptions.APIEmprestimoAgoraException;
import org.viniciusvirgilli.exceptions.ValidadorException;
import org.viniciusvirgilli.service.ProcessaSimulacaoService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("Testes para SimulacaoController")
class SimulacaoControllerTest {

    @InjectMock
    ProcessaSimulacaoService processaSimulacaoService;

    @Nested
    @DisplayName("Testes do método processarSimulacao")
    class ProcessarSimulacaoTests {

        @Test
        @DisplayName("Deve processar simulação com sucesso")
        void deveProcessarSimulacaoComSucesso() {
            // Arrange
            SimulacaoRequestDTO requestDTO = SimulacaoRequestDTO.builder()
                .valorDesejado(new BigDecimal("10000.00"))
                .prazo((short) 12)
                .build();
            
            List<ParcelaDTO> parcelasSAC = Arrays.asList(
                ParcelaDTO.builder()
                    .numero(1)
                    .valorAmortizacao(new BigDecimal("833.33"))
                    .valorJuros(new BigDecimal("100.00"))
                    .valorPrestacao(new BigDecimal("933.33"))
                    .build(),
                ParcelaDTO.builder()
                    .numero(2)
                    .valorAmortizacao(new BigDecimal("833.33"))
                    .valorJuros(new BigDecimal("91.67"))
                    .valorPrestacao(new BigDecimal("925.00"))
                    .build()
            );
            
            List<ParcelaDTO> parcelasPRICE = Arrays.asList(
                ParcelaDTO.builder()
                    .numero(1)
                    .valorAmortizacao(new BigDecimal("829.15"))
                    .valorJuros(new BigDecimal("100.00"))
                    .valorPrestacao(new BigDecimal("929.15"))
                    .build(),
                ParcelaDTO.builder()
                    .numero(2)
                    .valorAmortizacao(new BigDecimal("837.44"))
                    .valorJuros(new BigDecimal("91.71"))
                    .valorPrestacao(new BigDecimal("929.15"))
                    .build()
            );
            
            List<SimulacaoPorSistemaDTO> resultadoSimulacao = Arrays.asList(
                SimulacaoPorSistemaDTO.builder()
                    .tipo(TipoSimulacao.SAC)
                    .parcelas(parcelasSAC)
                    .build(),
                SimulacaoPorSistemaDTO.builder()
                    .tipo(TipoSimulacao.PRICE)
                    .parcelas(parcelasPRICE)
                    .build()
            );
            
            SimulacaoResponseDTO responseDTO = SimulacaoResponseDTO.builder()
                .idSimulacao(1L)
                .codigoProduto(1)
                .descricaoProduto("Empréstimo Pessoal")
                .taxaJuros(new BigDecimal("1.0"))
                .resultadoSimulacao(resultadoSimulacao)
                .build();

            when(processaSimulacaoService.executar(any(SimulacaoRequestDTO.class)))
                .thenReturn(responseDTO);

            // Act & Assert
            given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
            .when()
                .post("/api/simulacao/processar")
            .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("idSimulacao", equalTo(1))
                .body("codigoProduto", equalTo(1))
                .body("descricaoProduto", equalTo("Empréstimo Pessoal"))
                .body("taxaJuros", equalTo(1.0f))
                .body("resultadoSimulacao", hasSize(2))
                .body("resultadoSimulacao[0].tipo", equalTo("SAC"))
                .body("resultadoSimulacao[0].parcelas", hasSize(2))
                .body("resultadoSimulacao[1].tipo", equalTo("PRICE"))
                .body("resultadoSimulacao[1].parcelas", hasSize(2))
                .body("resultadoSimulacao[1].parcelas[0].numero", equalTo(1))
                .body("resultadoSimulacao[1].parcelas[0].valorAmortizacao", equalTo(829.15f))
                .body("resultadoSimulacao[1].parcelas[0].valorJuros", equalTo(100.0f))
                .body("resultadoSimulacao[1].parcelas[0].valorPrestacao", equalTo(929.15f))
                .body("resultadoSimulacao[0].tipo", equalTo("SAC"))
                .body("resultadoSimulacao[0].parcelas", hasSize(2))
                .body("resultadoSimulacao[0].parcelas[0].numero", equalTo(1))
                .body("resultadoSimulacao[0].parcelas[0].valorAmortizacao", equalTo(833.33f))
                .body("resultadoSimulacao[0].parcelas[0].valorJuros", equalTo(100.0f))
                .body("resultadoSimulacao[0].parcelas[0].valorPrestacao", equalTo(933.33f))
                .body("resultadoSimulacao[1].tipo", equalTo("PRICE"))
                .body("resultadoSimulacao[1].parcelas", hasSize(2))
                .body("resultadoSimulacao[1].parcelas[0].numero", equalTo(1))
                .body("resultadoSimulacao[1].parcelas[0].valorAmortizacao", equalTo(829.15f))
                .body("resultadoSimulacao[1].parcelas[0].valorJuros", equalTo(100.0f))
                .body("resultadoSimulacao[1].parcelas[0].valorPrestacao", equalTo(929.15f));

            // Verify
            verify(processaSimulacaoService, times(1)).executar(any(SimulacaoRequestDTO.class));
        }

        @Test
        @DisplayName("Deve retornar 400 quando dados de entrada são inválidos")
        void deveRetornar400QuandoDadosEntradaSaoInvalidos() {
            // Arrange
            SimulacaoRequestDTO requestDTO = SimulacaoRequestDTO.builder()
                .valorDesejado(null) // Valor inválido
                .prazo((short) 12)
                .build();

            // Act & Assert
            given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
            .when()
                .post("/api/simulacao/processar")
            .then()
                .statusCode(400);

            // Verify - não deve chamar o service quando validação falha
            verify(processaSimulacaoService, never()).executar(any(SimulacaoRequestDTO.class));
        }

        @Test
        @DisplayName("Deve retornar 400 quando service lança ValidadorException")
        void deveRetornar400QuandoServiceLancaValidadorException() {
            // Arrange
            SimulacaoRequestDTO requestDTO = SimulacaoRequestDTO.builder()
                .valorDesejado(new BigDecimal("1000.00"))
                .prazo((short) 6)
                .build();

            CamposComProblemasDTO camposComProblemas = CamposComProblemasDTO.builder()
                .campos(Arrays.asList("valorDesejado"))
                .message("Valor deve ser maior que zero")
                .build();

            when(processaSimulacaoService.executar(any(SimulacaoRequestDTO.class)))
                .thenThrow(new ValidadorException(camposComProblemas));

            // Act & Assert
            given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
            .when()
                .post("/api/simulacao/processar")
            .then()
                .statusCode(400);

            // Verify
            verify(processaSimulacaoService, times(1)).executar(any(SimulacaoRequestDTO.class));
        }

        @Test
        @DisplayName("Deve retornar 500 quando service lança RuntimeException")
        void deveRetornar500QuandoServiceLancaRuntimeException() {
            // Arrange
            SimulacaoRequestDTO requestDTO = SimulacaoRequestDTO.builder()
                .valorDesejado(new BigDecimal("10000.00"))
                .prazo((short) 12)
                .build();

            when(processaSimulacaoService.executar(any(SimulacaoRequestDTO.class)))
                .thenThrow(new RuntimeException("Erro de persistência"));

            // Act & Assert
            given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
            .when()
                .post("/api/simulacao/processar")
            .then()
                .statusCode(500);

            // Verify
            verify(processaSimulacaoService, times(1)).executar(any(SimulacaoRequestDTO.class));
        }

        @Test
        @DisplayName("Deve validar estrutura completa da resposta JSON")
        void deveValidarEstruturaCompletaRespostaJson() {
            // Arrange
            SimulacaoRequestDTO requestDTO = SimulacaoRequestDTO.builder()
                .valorDesejado(new BigDecimal("25000.00"))
                .prazo((short) 24)
                .build();
            
            List<ParcelaDTO> parcelas = Arrays.asList(
                ParcelaDTO.builder()
                    .numero(1)
                    .valorAmortizacao(new BigDecimal("1041.67"))
                    .valorJuros(new BigDecimal("250.00"))
                    .valorPrestacao(new BigDecimal("1291.67"))
                    .build()
            );
            
            List<SimulacaoPorSistemaDTO> resultadoSimulacao = Arrays.asList(
                SimulacaoPorSistemaDTO.builder()
                    .tipo(TipoSimulacao.SAC)
                    .parcelas(parcelas)
                    .build()
            );
            
            SimulacaoResponseDTO responseDTO = SimulacaoResponseDTO.builder()
                .idSimulacao(123L)
                .codigoProduto(2)
                .descricaoProduto("Crédito Consignado")
                .taxaJuros(new BigDecimal("1.0"))
                .resultadoSimulacao(resultadoSimulacao)
                .build();

            when(processaSimulacaoService.executar(any(SimulacaoRequestDTO.class)))
                .thenReturn(responseDTO);

            // Act & Assert
            given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
            .when()
                .post("/api/simulacao/processar")
            .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("$", hasKey("idSimulacao"))
                .body("$", hasKey("codigoProduto"))
                .body("$", hasKey("descricaoProduto"))
                .body("$", hasKey("taxaJuros"))
                .body("$", hasKey("resultadoSimulacao"))
                .body("resultadoSimulacao[0]", hasKey("tipo"))
                .body("resultadoSimulacao[0]", hasKey("parcelas"))
                .body("resultadoSimulacao[0].parcelas[0]", hasKey("numero"))
                .body("resultadoSimulacao[0].parcelas[0]", hasKey("valorAmortizacao"))
                .body("resultadoSimulacao[0].parcelas[0]", hasKey("valorJuros"))
                .body("resultadoSimulacao[0].parcelas[0]", hasKey("valorPrestacao"));

            // Verify
            verify(processaSimulacaoService, times(1)).executar(any(SimulacaoRequestDTO.class));
        }

        @Test
        @DisplayName("Deve testar com diferentes valores e prazos")
        void deveTestarComDiferentesValoresEPrazos() {
            // Arrange
            SimulacaoRequestDTO requestDTO = SimulacaoRequestDTO.builder()
                .valorDesejado(new BigDecimal("50000.00"))
                .prazo((short) 60)
                .build();
            
            SimulacaoResponseDTO responseDTO = SimulacaoResponseDTO.builder()
                .idSimulacao(999L)
                .codigoProduto(3)
                .descricaoProduto("Financiamento Imobiliário")
                .taxaJuros(new BigDecimal("0.8"))
                .resultadoSimulacao(Arrays.asList(
                    SimulacaoPorSistemaDTO.builder()
                        .tipo(TipoSimulacao.PRICE)
                        .parcelas(Arrays.asList(
                            ParcelaDTO.builder()
                                .numero(1)
                                .valorAmortizacao(new BigDecimal("433.33"))
                                .valorJuros(new BigDecimal("400.00"))
                                .valorPrestacao(new BigDecimal("833.33"))
                                .build()
                        ))
                        .build()
                ))
                .build();

            when(processaSimulacaoService.executar(any(SimulacaoRequestDTO.class)))
                .thenReturn(responseDTO);

            // Act & Assert
            given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
            .when()
                .post("/api/simulacao/processar")
            .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("idSimulacao", equalTo(999))
                .body("codigoProduto", equalTo(3))
                .body("descricaoProduto", equalTo("Financiamento Imobiliário"))
                .body("taxaJuros", equalTo(0.8f));

            // Verify
            verify(processaSimulacaoService, times(1)).executar(any(SimulacaoRequestDTO.class));
        }
    }

    @Nested
    @DisplayName("Testes do método buscarSimulacaoPorId")
    class BuscarSimulacaoPorIdTests {

        @Test
        @DisplayName("Deve retornar simulação por ID com sucesso")
        void deveRetornarSimulacaoPorIdComSucesso() {
            // Arrange
            Long idSimulacao = 1L;
            
            List<ParcelaDTO> parcelasSAC = Arrays.asList(
                ParcelaDTO.builder()
                    .numero(1)
                    .valorAmortizacao(new BigDecimal("833.33"))
                    .valorJuros(new BigDecimal("100.00"))
                    .valorPrestacao(new BigDecimal("933.33"))
                    .build(),
                ParcelaDTO.builder()
                    .numero(2)
                    .valorAmortizacao(new BigDecimal("833.33"))
                    .valorJuros(new BigDecimal("91.67"))
                    .valorPrestacao(new BigDecimal("925.00"))
                    .build()
            );
            
            List<ParcelaDTO> parcelasPRICE = Arrays.asList(
                ParcelaDTO.builder()
                    .numero(1)
                    .valorAmortizacao(new BigDecimal("829.15"))
                    .valorJuros(new BigDecimal("100.00"))
                    .valorPrestacao(new BigDecimal("929.15"))
                    .build(),
                ParcelaDTO.builder()
                    .numero(2)
                    .valorAmortizacao(new BigDecimal("837.44"))
                    .valorJuros(new BigDecimal("91.71"))
                    .valorPrestacao(new BigDecimal("929.15"))
                    .build()
            );
            
            List<SimulacaoPorSistemaDTO> resultadoSimulacao = Arrays.asList(
                SimulacaoPorSistemaDTO.builder()
                    .tipo(TipoSimulacao.SAC)
                    .parcelas(parcelasSAC)
                    .build(),
                SimulacaoPorSistemaDTO.builder()
                    .tipo(TipoSimulacao.PRICE)
                    .parcelas(parcelasPRICE)
                    .build()
            );
            
            SimulacaoResponseDTO responseDTO = SimulacaoResponseDTO.builder()
                .idSimulacao(idSimulacao)
                .codigoProduto(1)
                .descricaoProduto("Empréstimo Pessoal")
                .taxaJuros(new BigDecimal("1.0"))
                .resultadoSimulacao(resultadoSimulacao)
                .build();

            when(processaSimulacaoService.buscarPorId(idSimulacao))
                .thenReturn(responseDTO);

            // Act & Assert
            given()
                .pathParam("id", idSimulacao)
                .contentType(ContentType.JSON)
            .when()
                .get("/api/simulacao/{id}")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("idSimulacao", equalTo(1))
                .body("codigoProduto", equalTo(1))
                .body("descricaoProduto", equalTo("Empréstimo Pessoal"))
                .body("taxaJuros", equalTo(1.0f))
                .body("resultadoSimulacao", hasSize(2))
                .body("resultadoSimulacao[0].tipo", equalTo("SAC"))
                .body("resultadoSimulacao[0].parcelas", hasSize(2))
                .body("resultadoSimulacao[0].parcelas[0].numero", equalTo(1))
                .body("resultadoSimulacao[0].parcelas[0].valorAmortizacao", equalTo(833.33f))
                .body("resultadoSimulacao[0].parcelas[0].valorJuros", equalTo(100.0f))
                .body("resultadoSimulacao[0].parcelas[0].valorPrestacao", equalTo(933.33f))
                .body("resultadoSimulacao[1].tipo", equalTo("PRICE"))
                .body("resultadoSimulacao[1].parcelas", hasSize(2))
                .body("resultadoSimulacao[1].parcelas[0].numero", equalTo(1))
                .body("resultadoSimulacao[1].parcelas[0].valorAmortizacao", equalTo(829.15f))
                .body("resultadoSimulacao[1].parcelas[0].valorJuros", equalTo(100.0f))
                .body("resultadoSimulacao[1].parcelas[0].valorPrestacao", equalTo(929.15f));

            // Verify
            verify(processaSimulacaoService, times(1)).buscarPorId(idSimulacao);
        }

        @Test
        @DisplayName("Deve retornar 404 quando simulação não for encontrada")
        void deveRetornar404QuandoSimulacaoNaoForEncontrada() {
            // Arrange
            Long idSimulacao = 999L;
            
            when(processaSimulacaoService.buscarPorId(idSimulacao))
                .thenReturn(null);

            // Act & Assert
            given()
                .pathParam("id", idSimulacao)
                .contentType(ContentType.JSON)
            .when()
                .get("/api/simulacao/{id}")
            .then()
                .statusCode(404)
                .body(equalTo("Simulação não encontrada"));

            // Verify
            verify(processaSimulacaoService, times(1)).buscarPorId(idSimulacao);
        }

        @Test
        @DisplayName("Deve retornar 500 quando service lança APIEmprestimoAgoraException")
        void deveRetornar500QuandoServiceLancaAPIEmprestimoAgoraException() {
            // Arrange
            Long idSimulacao = 1L;
            
            when(processaSimulacaoService.buscarPorId(idSimulacao))
                .thenThrow(new APIEmprestimoAgoraException("Erro ao buscar simulação"));

            // Act & Assert
            given()
                .pathParam("id", idSimulacao)
                .contentType(ContentType.JSON)
            .when()
                .get("/api/simulacao/{id}")
            .then()
                .statusCode(500);

            // Verify
            verify(processaSimulacaoService, times(1)).buscarPorId(idSimulacao);
        }

        @Test
        @DisplayName("Deve retornar 500 quando service lança RuntimeException")
        void deveRetornar500QuandoServiceLancaRuntimeException() {
            // Arrange
            Long idSimulacao = 1L;
            
            when(processaSimulacaoService.buscarPorId(idSimulacao))
                .thenThrow(new RuntimeException("Erro inesperado"));

            // Act & Assert
            given()
                .pathParam("id", idSimulacao)
                .contentType(ContentType.JSON)
            .when()
                .get("/api/simulacao/{id}")
            .then()
                .statusCode(500);

            // Verify
            verify(processaSimulacaoService, times(1)).buscarPorId(idSimulacao);
        }

        @Test
        @DisplayName("Deve validar estrutura completa da resposta JSON")
        void deveValidarEstruturaCompletaRespostaJson() {
            // Arrange
            Long idSimulacao = 123L;
            
            List<ParcelaDTO> parcelas = Arrays.asList(
                ParcelaDTO.builder()
                    .numero(1)
                    .valorAmortizacao(new BigDecimal("1000.00"))
                    .valorJuros(new BigDecimal("50.00"))
                    .valorPrestacao(new BigDecimal("1050.00"))
                    .build()
            );
            
            List<SimulacaoPorSistemaDTO> resultadoSimulacao = Arrays.asList(
                SimulacaoPorSistemaDTO.builder()
                    .tipo(TipoSimulacao.SAC)
                    .parcelas(parcelas)
                    .build()
            );
            
            SimulacaoResponseDTO responseDTO = SimulacaoResponseDTO.builder()
                .idSimulacao(idSimulacao)
                .codigoProduto(2)
                .descricaoProduto("Crédito Consignado")
                .taxaJuros(new BigDecimal("0.5"))
                .resultadoSimulacao(resultadoSimulacao)
                .build();

            when(processaSimulacaoService.buscarPorId(idSimulacao))
                .thenReturn(responseDTO);

            // Act & Assert
            given()
                .pathParam("id", idSimulacao)
                .contentType(ContentType.JSON)
            .when()
                .get("/api/simulacao/{id}")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasKey("idSimulacao"))
                .body("$", hasKey("codigoProduto"))
                .body("$", hasKey("descricaoProduto"))
                .body("$", hasKey("taxaJuros"))
                .body("$", hasKey("resultadoSimulacao"))
                .body("resultadoSimulacao[0]", hasKey("tipo"))
                .body("resultadoSimulacao[0]", hasKey("parcelas"))
                .body("resultadoSimulacao[0].parcelas[0]", hasKey("numero"))
                .body("resultadoSimulacao[0].parcelas[0]", hasKey("valorAmortizacao"))
                .body("resultadoSimulacao[0].parcelas[0]", hasKey("valorJuros"))
                .body("resultadoSimulacao[0].parcelas[0]", hasKey("valorPrestacao"));

            // Verify
            verify(processaSimulacaoService, times(1)).buscarPorId(idSimulacao);
        }

        @Test
        @DisplayName("Deve testar com diferentes tipos de ID")
        void deveTestarComDiferentesTiposId() {
            // Arrange
            Long idSimulacao = 9999999L; // ID grande
            
            SimulacaoResponseDTO responseDTO = SimulacaoResponseDTO.builder()
                .idSimulacao(idSimulacao)
                .codigoProduto(3)
                .descricaoProduto("Financiamento Veículo")
                .taxaJuros(new BigDecimal("1.5"))
                .resultadoSimulacao(Arrays.asList(
                    SimulacaoPorSistemaDTO.builder()
                        .tipo(TipoSimulacao.PRICE)
                        .parcelas(Arrays.asList(
                            ParcelaDTO.builder()
                                .numero(1)
                                .valorAmortizacao(new BigDecimal("500.00"))
                                .valorJuros(new BigDecimal("75.00"))
                                .valorPrestacao(new BigDecimal("575.00"))
                                .build()
                        ))
                        .build()
                ))
                .build();

            when(processaSimulacaoService.buscarPorId(idSimulacao))
                .thenReturn(responseDTO);

            // Act & Assert
            given()
                .pathParam("id", idSimulacao)
                .contentType(ContentType.JSON)
            .when()
                .get("/api/simulacao/{id}")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("idSimulacao", equalTo(9999999))
                .body("codigoProduto", equalTo(3))
                .body("descricaoProduto", equalTo("Financiamento Veículo"))
                .body("taxaJuros", equalTo(1.5f));

            // Verify
            verify(processaSimulacaoService, times(1)).buscarPorId(idSimulacao);
        }
    }
}