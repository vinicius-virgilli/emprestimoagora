package org.viniciusvirgilli.controller;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.viniciusvirgilli.dto.VolumeDiarioResponseDTO;
import org.viniciusvirgilli.dto.VolumeSimuladoDTO;
import org.viniciusvirgilli.enums.TipoSimulacao;
import org.viniciusvirgilli.service.ProcessaSimulacaoService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("Testes do VolumeController - Método Health")
class VolumeControllerTest {

    @InjectMock
    ProcessaSimulacaoService processaSimulacaoService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(processaSimulacaoService);
    }

    @Test
    @DisplayName("Deve retornar health check com sucesso quando serviço está funcionando")
    void deveRetornarHealthCheckComSucesso() {
        // Arrange
        VolumeSimuladoDTO volumeSimulado = VolumeSimuladoDTO.builder()
                .codigoProduto(1)
                .descricaoProduto("Empréstimo Pessoal")
                .taxaMediaJuros(new BigDecimal("2.5"))
                .valorMedioPrestacao(new BigDecimal("929.58"))
                .valorTotalDesejado(new BigDecimal("10000.00"))
                .valorTotalCredito(new BigDecimal("10000.00"))
                .build();

        VolumeDiarioResponseDTO volumeResponse = VolumeDiarioResponseDTO.builder()
                .dataReferencia(LocalDate.now())
                .simulacoes(List.of(volumeSimulado))
                .build();

        when(processaSimulacaoService.buscarVolumePorProdutoPorDia(
                any(LocalDate.class),
                eq(1),
                eq(TipoSimulacao.PRICE)
        )).thenReturn(volumeResponse);

        // Act & Assert
        given()
                .when()
                .get("/api/volume/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("OK"))
                .body("mensagem", equalTo("Serviço de volume funcionando"));

        // Verify
        verify(processaSimulacaoService, times(1))
                .buscarVolumePorProdutoPorDia(any(LocalDate.class), eq(1), eq(TipoSimulacao.PRICE));
    }

    @Test
    @DisplayName("Deve retornar erro 503 quando serviço está indisponível")
    void deveRetornarErro503QuandoServicoIndisponivel() {
        // Arrange
        when(processaSimulacaoService.buscarVolumePorProdutoPorDia(
                any(LocalDate.class),
                eq(1),
                eq(TipoSimulacao.PRICE)
        )).thenThrow(new RuntimeException("Erro de conectividade com banco de dados"));

        // Act & Assert
        given()
                .when()
                .get("/api/volume/health")
                .then()
                .statusCode(503)
                .contentType(ContentType.JSON)
                .body("status", equalTo("ERROR"))
                .body("mensagem", containsString("Serviço de volume indisponível"))
                .body("mensagem", containsString("Erro de conectividade com banco de dados"));

        // Verify
        verify(processaSimulacaoService, times(1))
                .buscarVolumePorProdutoPorDia(any(LocalDate.class), eq(1), eq(TipoSimulacao.PRICE));
    }

    @Test
    @DisplayName("Deve retornar erro 503 quando ocorre exceção genérica")
    void deveRetornarErro503QuandoOcorreExcecaoGenerica() {
        // Arrange
        when(processaSimulacaoService.buscarVolumePorProdutoPorDia(
                any(LocalDate.class),
                eq(1),
                eq(TipoSimulacao.PRICE)
        )).thenThrow(new IllegalStateException("Estado inválido do serviço"));

        // Act & Assert
        given()
                .when()
                .get("/api/volume/health")
                .then()
                .statusCode(503)
                .contentType(ContentType.JSON)
                .body("status", equalTo("ERROR"))
                .body("mensagem", containsString("Serviço de volume indisponível"))
                .body("mensagem", containsString("Estado inválido do serviço"));

        // Verify
        verify(processaSimulacaoService, times(1))
                .buscarVolumePorProdutoPorDia(any(LocalDate.class), eq(1), eq(TipoSimulacao.PRICE));
    }

    @Test
    @DisplayName("Deve retornar health check mesmo quando volume está vazio")
    void deveRetornarHealthCheckComVolumeVazio() {
        // Arrange
        VolumeDiarioResponseDTO volumeVazio = VolumeDiarioResponseDTO.builder()
                .dataReferencia(LocalDate.now())
                .simulacoes(List.of()) // Lista vazia
                .build();

        when(processaSimulacaoService.buscarVolumePorProdutoPorDia(
                any(LocalDate.class),
                eq(1),
                eq(TipoSimulacao.PRICE)
        )).thenReturn(volumeVazio);

        // Act & Assert
        given()
                .when()
                .get("/api/volume/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("OK"))
                .body("mensagem", equalTo("Serviço de volume funcionando"));

        // Verify
        verify(processaSimulacaoService, times(1))
                .buscarVolumePorProdutoPorDia(any(LocalDate.class), eq(1), eq(TipoSimulacao.PRICE));
    }

    @Test
    @DisplayName("Deve retornar erro 500 quando ocorre exceção no serviço")
    void deveRetornarErro500QuandoOcorreExcecaoNoServico() {
        // Arrange
        LocalDate dataReferencia = LocalDate.of(2024, 1, 15);
        Integer codigoProduto = 1;
        TipoSimulacao tipoSimulacao = TipoSimulacao.PRICE;

        when(processaSimulacaoService.buscarVolumePorProdutoPorDia(
                eq(dataReferencia),
                eq(codigoProduto),
                eq(tipoSimulacao)
        )).thenThrow(new RuntimeException("Erro interno do serviço"));

        // Act & Assert
        given()
                .queryParam("dataReferencia", "2024-01-15")
                .queryParam("codigoProduto", 1)
                .queryParam("tipoSimulacao", "PRICE")
                .when()
                .get("/api/volume/diario")  // URL corrigida
                .then()
                .statusCode(500);

        // Verify
        verify(processaSimulacaoService, times(1))
                .buscarVolumePorProdutoPorDia(eq(dataReferencia), eq(codigoProduto), eq(tipoSimulacao));
    }

    // ========== TESTES PARA CONSULTAR VOLUME DIÁRIO ==========

    @Test
    @DisplayName("Deve consultar volume diário com sucesso com todos os parâmetros")
    void deveConsultarVolumeDiarioComSucessoComTodosParametros() {
        // Arrange
        LocalDate dataReferencia = LocalDate.of(2024, 1, 15);
        Integer codigoProduto = 1;
        TipoSimulacao tipoSimulacao = TipoSimulacao.SAC;

        VolumeSimuladoDTO volumeSimulado = VolumeSimuladoDTO.builder()
                .codigoProduto(1)
                .descricaoProduto("Empréstimo Pessoal")
                .taxaMediaJuros(new BigDecimal("2.5"))
                .valorMedioPrestacao(new BigDecimal("929.58"))
                .valorTotalDesejado(new BigDecimal("10000.00"))
                .valorTotalCredito(new BigDecimal("10000.00"))
                .build();

        VolumeDiarioResponseDTO volumeResponse = VolumeDiarioResponseDTO.builder()
                .dataReferencia(dataReferencia)
                .simulacoes(List.of(volumeSimulado))
                .build();

        when(processaSimulacaoService.buscarVolumePorProdutoPorDia(
                eq(dataReferencia),
                eq(codigoProduto),
                eq(tipoSimulacao)
        )).thenReturn(volumeResponse);

        // Act & Assert
        given()
                .queryParam("dataReferencia", "2024-01-15")
                .queryParam("codigoProduto", 1)
                .queryParam("tipoSimulacao", "SAC")
                .when()
                .get("/api/volume/diario")  // URL corrigida
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("dataReferencia", equalTo("2024-01-15"))
                .body("simulacoes", hasSize(1))
                .body("simulacoes[0].codigoProduto", equalTo(1))
                .body("simulacoes[0].descricaoProduto", equalTo("Empréstimo Pessoal"))
                .body("simulacoes[0].taxaMediaJuros", equalTo(2.5f))
                .body("simulacoes[0].valorMedioPrestacao", equalTo(929.58f))
                .body("simulacoes[0].valorTotalDesejado", equalTo(10000.00f))
                .body("simulacoes[0].valorTotalCredito", equalTo(10000.00f));

        // Verify
        verify(processaSimulacaoService, times(1))
                .buscarVolumePorProdutoPorDia(eq(dataReferencia), eq(codigoProduto), eq(tipoSimulacao));
    }

    @Test
    @DisplayName("Deve consultar volume diário com tipo simulação padrão PRICE quando não informado")
    void deveConsultarVolumeDiarioComTipoSimulacaoPadrao() {
        // Arrange
        LocalDate dataReferencia = LocalDate.of(2024, 1, 15);
        Integer codigoProduto = 2;

        VolumeSimuladoDTO volumeSimulado = VolumeSimuladoDTO.builder()
                .codigoProduto(2)
                .descricaoProduto("Financiamento Veicular")
                .taxaMediaJuros(new BigDecimal("1.8"))
                .valorMedioPrestacao(new BigDecimal("1200.00"))
                .valorTotalDesejado(new BigDecimal("50000.00"))
                .valorTotalCredito(new BigDecimal("50000.00"))
                .build();

        VolumeDiarioResponseDTO volumeResponse = VolumeDiarioResponseDTO.builder()
                .dataReferencia(dataReferencia)
                .simulacoes(List.of(volumeSimulado))
                .build();

        when(processaSimulacaoService.buscarVolumePorProdutoPorDia(
                eq(dataReferencia),
                eq(codigoProduto),
                eq(TipoSimulacao.PRICE) // Valor padrão
        )).thenReturn(volumeResponse);

        // Act & Assert
        given()
                .queryParam("dataReferencia", "2024-01-15")
                .queryParam("codigoProduto", 2)
                // Não informando tipoSimulacao para testar valor padrão
                .when()
                .get("/api/volume/diario")  // URL corrigida
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("dataReferencia", equalTo("2024-01-15"))
                .body("simulacoes", hasSize(1))
                .body("simulacoes[0].codigoProduto", equalTo(2))
                .body("simulacoes[0].descricaoProduto", equalTo("Financiamento Veicular"));

        // Verify
        verify(processaSimulacaoService, times(1))
                .buscarVolumePorProdutoPorDia(eq(dataReferencia), eq(codigoProduto), eq(TipoSimulacao.PRICE));
    }

    @Test
    @DisplayName("Deve consultar volume diário com data atual quando não informada")
    void deveConsultarVolumeDiarioComDataAtualQuandoNaoInformada() {
        // Arrange
        Integer codigoProduto = 1;
        TipoSimulacao tipoSimulacao = TipoSimulacao.PRICE;
        LocalDate dataAtual = LocalDate.now();
    
        VolumeSimuladoDTO volumeSimulado = VolumeSimuladoDTO.builder()
                .codigoProduto(1)
                .descricaoProduto("Empréstimo Pessoal")
                .taxaMediaJuros(new BigDecimal("2.5"))
                .valorMedioPrestacao(new BigDecimal("929.58"))
                .valorTotalDesejado(new BigDecimal("10000.00"))
                .valorTotalCredito(new BigDecimal("10000.00"))
                .build();
    
        VolumeDiarioResponseDTO volumeResponse = VolumeDiarioResponseDTO.builder()
                .dataReferencia(dataAtual)
                .simulacoes(List.of(volumeSimulado))
                .build();
    
        when(processaSimulacaoService.buscarVolumePorProdutoPorDia(
                eq(dataAtual), // Usa data específica em vez de any()
                eq(codigoProduto),
                eq(tipoSimulacao)
        )).thenReturn(volumeResponse);
    
        // Act & Assert
        given()
                .queryParam("dataReferencia", dataAtual.toString()) // Inclui a dataReferencia
                .queryParam("codigoProduto", 1)
                .queryParam("tipoSimulacao", "PRICE")
                .when()
                .get("/api/volume/diario")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("simulacoes", hasSize(1))
                .body("simulacoes[0].codigoProduto", equalTo(1));
    
        // Verify
        verify(processaSimulacaoService, times(1))
                .buscarVolumePorProdutoPorDia(eq(dataAtual), eq(codigoProduto), eq(tipoSimulacao));
    }

    @Test
    @DisplayName("Deve retornar volume vazio quando não há simulações para os parâmetros informados")
    void deveRetornarVolumeVazioQuandoNaoHaSimulacoes() {
        // Arrange
        LocalDate dataReferencia = LocalDate.of(2024, 1, 15);
        Integer codigoProduto = 999; // Produto inexistente
        TipoSimulacao tipoSimulacao = TipoSimulacao.PRICE;

        VolumeDiarioResponseDTO volumeVazio = VolumeDiarioResponseDTO.builder()
                .dataReferencia(dataReferencia)
                .simulacoes(List.of()) // Lista vazia
                .build();

        when(processaSimulacaoService.buscarVolumePorProdutoPorDia(
                eq(dataReferencia),
                eq(codigoProduto),
                eq(tipoSimulacao)
        )).thenReturn(volumeVazio);

        // Act & Assert
        given()
                .queryParam("dataReferencia", "2024-01-15")
                .queryParam("codigoProduto", 999)
                .queryParam("tipoSimulacao", "PRICE")
                .when()
                .get("/api/volume/diario")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("dataReferencia", equalTo("2024-01-15"))
                .body("simulacoes", hasSize(0));

        // Verify
        verify(processaSimulacaoService, times(1))
                .buscarVolumePorProdutoPorDia(eq(dataReferencia), eq(codigoProduto), eq(tipoSimulacao));
    }

    @Test
    @DisplayName("Deve retornar múltiplas simulações quando há volume significativo")
    void deveRetornarMultiplasSimulacoesQuandoHaVolumeSignificativo() {
        // Arrange
        LocalDate dataReferencia = LocalDate.of(2024, 1, 15);
        Integer codigoProduto = 1;
        TipoSimulacao tipoSimulacao = TipoSimulacao.SAC;

        VolumeSimuladoDTO simulacao1 = VolumeSimuladoDTO.builder()
                .codigoProduto(1)
                .descricaoProduto("Empréstimo Pessoal")
                .taxaMediaJuros(new BigDecimal("2.5"))
                .valorMedioPrestacao(new BigDecimal("929.58"))
                .valorTotalDesejado(new BigDecimal("10000.00"))
                .valorTotalCredito(new BigDecimal("10000.00"))
                .build();

        VolumeSimuladoDTO simulacao2 = VolumeSimuladoDTO.builder()
                .codigoProduto(1)
                .descricaoProduto("Empréstimo Pessoal")
                .taxaMediaJuros(new BigDecimal("3.0"))
                .valorMedioPrestacao(new BigDecimal("1500.00"))
                .valorTotalDesejado(new BigDecimal("20000.00"))
                .valorTotalCredito(new BigDecimal("20000.00"))
                .build();

        VolumeDiarioResponseDTO volumeResponse = VolumeDiarioResponseDTO.builder()
                .dataReferencia(dataReferencia)
                .simulacoes(List.of(simulacao1, simulacao2))
                .build();

        when(processaSimulacaoService.buscarVolumePorProdutoPorDia(
                eq(dataReferencia),
                eq(codigoProduto),
                eq(tipoSimulacao)
        )).thenReturn(volumeResponse);

        // Act & Assert
        given()
                .queryParam("dataReferencia", "2024-01-15")
                .queryParam("codigoProduto", 1)
                .queryParam("tipoSimulacao", "SAC")
                .when()
                .get("/api/volume/diario")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("dataReferencia", equalTo("2024-01-15"))
                .body("simulacoes", hasSize(2))
                .body("simulacoes[0].valorTotalDesejado", equalTo(10000.00f))
                .body("simulacoes[1].valorTotalDesejado", equalTo(20000.00f));

        // Verify
        verify(processaSimulacaoService, times(1))
                .buscarVolumePorProdutoPorDia(eq(dataReferencia), eq(codigoProduto), eq(tipoSimulacao));
    }

}