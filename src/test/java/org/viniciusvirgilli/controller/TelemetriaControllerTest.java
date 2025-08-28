package org.viniciusvirgilli.controller;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.viniciusvirgilli.dto.TelemetriaResponseDTO;
import org.viniciusvirgilli.dto.TelemetriaEndpointDTO;
import org.viniciusvirgilli.service.MetricasService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("Testes do TelemetriaController - Método Health")
class TelemetriaControllerTest {

    @InjectMock
    MetricasService metricasService;

    @BeforeEach
    void setUp() {
        // Reset dos mocks antes de cada teste
        reset(metricasService);
    }

    @Test
    @DisplayName("Deve retornar health check com sucesso quando serviço está funcionando")
    void deveRetornarHealthCheckComSucesso() {
        // Arrange
        TelemetriaResponseDTO telemetriaResponse = TelemetriaResponseDTO.builder()
                .dataReferencia(LocalDate.now())
                .build();

        when(metricasService.coletarMetricas(any(LocalDate.class)))
                .thenReturn(telemetriaResponse);

        // Act & Assert
        given()
                .when()
                .get("/api/telemetria/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("OK"))
                .body("mensagem", equalTo("Serviço de telemetria funcionando"));

        // Verify
        verify(metricasService, times(1))
                .coletarMetricas(any(LocalDate.class));
    }

    @Test
    @DisplayName("Deve retornar erro 503 quando serviço de métricas está indisponível")
    void deveRetornarErro503QuandoServicoMetricasIndisponivel() {
        // Arrange
        String mensagemErro = "Erro de conexão com banco de dados";
        when(metricasService.coletarMetricas(any(LocalDate.class)))
                .thenThrow(new RuntimeException(mensagemErro));

        // Act & Assert
        given()
                .when()
                .get("/api/telemetria/health")
                .then()
                .statusCode(503)
                .contentType(ContentType.JSON)
                .body("status", equalTo("ERROR"))
                .body("mensagem", containsString("Serviço de telemetria indisponível"))
                .body("mensagem", containsString(mensagemErro));

        // Verify
        verify(metricasService, times(1))
                .coletarMetricas(any(LocalDate.class));
    }

    @Test
    @DisplayName("Deve retornar erro 503 quando ocorre exceção genérica no serviço")
    void deveRetornarErro503QuandoOcorreExcecaoGenerica() {
        // Arrange
        when(metricasService.coletarMetricas(any(LocalDate.class)))
                .thenThrow(new IllegalStateException("Estado inválido do serviço"));

        // Act & Assert
        given()
                .when()
                .get("/api/telemetria/health")
                .then()
                .statusCode(503)
                .contentType(ContentType.JSON)
                .body("status", equalTo("ERROR"))
                .body("mensagem", containsString("Serviço de telemetria indisponível"))
                .body("mensagem", containsString("Estado inválido do serviço"));

        // Verify
        verify(metricasService, times(1))
                .coletarMetricas(any(LocalDate.class));
    }

    @Test
    @DisplayName("Deve retornar health check mesmo quando métricas estão vazias")
    void deveRetornarHealthCheckComMetricasVazias() {
        // Arrange
        TelemetriaResponseDTO telemetriaVazia = TelemetriaResponseDTO.builder()
                .dataReferencia(LocalDate.now())
                .build();

        when(metricasService.coletarMetricas(any(LocalDate.class)))
                .thenReturn(telemetriaVazia);

        // Act & Assert
        given()
                .when()
                .get("/api/telemetria/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("OK"))
                .body("mensagem", equalTo("Serviço de telemetria funcionando"));

        // Verify
        verify(metricasService, times(1))
                .coletarMetricas(any(LocalDate.class));
    }

    @Test
    @DisplayName("Deve retornar erro 503 quando ocorre timeout no serviço de métricas")
    void deveRetornarErro503QuandoOcorreTimeoutNoServico() {
        // Arrange
        when(metricasService.coletarMetricas(any(LocalDate.class)))
                .thenThrow(new RuntimeException("Timeout na consulta de métricas"));

        // Act & Assert
        given()
                .when()
                .get("/api/telemetria/health")
                .then()
                .statusCode(503)
                .contentType(ContentType.JSON)
                .body("status", equalTo("ERROR"))
                .body("mensagem", containsString("Serviço de telemetria indisponível"))
                .body("mensagem", containsString("Timeout na consulta de métricas"));

        // Verify
        verify(metricasService, times(1))
                .coletarMetricas(any(LocalDate.class));
    }

    @Test
    @DisplayName("Deve retornar erro 503 quando serviço de métricas retorna null")
    void deveRetornarErro503QuandoServicoRetornaNullPointerException() {
        // Arrange
        when(metricasService.coletarMetricas(any(LocalDate.class)))
                .thenThrow(new NullPointerException("Referência nula no serviço de métricas"));

        // Act & Assert
        given()
                .when()
                .get("/api/telemetria/health")
                .then()
                .statusCode(503)
                .contentType(ContentType.JSON)
                .body("status", equalTo("ERROR"))
                .body("mensagem", containsString("Serviço de telemetria indisponível"))
                .body("mensagem", containsString("Referência nula no serviço de métricas"));

        // Verify
        verify(metricasService, times(1))
                .coletarMetricas(any(LocalDate.class));
    }

    // ========== TESTES PARA O MÉTODO getTelemetria ==========

    @Test
    @DisplayName("Deve retornar telemetria com sucesso quando data é informada")
    void deveRetornarTelemetriaComSucessoComDataInformada() {
        // Arrange
        LocalDate dataReferencia = LocalDate.of(2024, 1, 15);
        TelemetriaEndpointDTO endpoint1 = new TelemetriaEndpointDTO(
                "/api/simulacao",
                100,
                250,
                50,
                500,
                new BigDecimal("0.95")
        );
        TelemetriaEndpointDTO endpoint2 = new TelemetriaEndpointDTO(
                "/api/volume/diario",
                75,
                180,
                30,
                400,
                new BigDecimal("0.98")
        );
        
        TelemetriaResponseDTO telemetriaResponse = TelemetriaResponseDTO.builder()
                .dataReferencia(dataReferencia)
                .listaEndpoints(Arrays.asList(endpoint1, endpoint2))
                .build();

        when(metricasService.coletarMetricas(dataReferencia))
                .thenReturn(telemetriaResponse);

        // Act & Assert
        given()
                .queryParam("dataReferencia", "2024-01-15")
                .when()
                .get("/api/telemetria")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("dataReferencia", equalTo("2024-01-15"))
                .body("listaEndpoints", hasSize(2))
                .body("listaEndpoints[0].nomeApi", equalTo("/api/simulacao"))
                .body("listaEndpoints[0].qtdRequisicoes", equalTo(100))
                .body("listaEndpoints[0].tempoMedio", equalTo(250))
                .body("listaEndpoints[0].tempoMinimo", equalTo(50))
                .body("listaEndpoints[0].tempoMaximo", equalTo(500))
                .body("listaEndpoints[0].percentualSucesso", equalTo(0.95f))
                .body("listaEndpoints[1].nomeApi", equalTo("/api/volume/diario"))
                .body("listaEndpoints[1].qtdRequisicoes", equalTo(75))
                .body("listaEndpoints[1].tempoMedio", equalTo(180))
                .body("listaEndpoints[1].percentualSucesso", equalTo(0.98f));

        // Verify
        verify(metricasService, times(1))
                .coletarMetricas(dataReferencia);
    }

    @Test
    @DisplayName("Deve retornar telemetria com data atual quando data não é informada")
    void deveRetornarTelemetriaComDataAtualQuandoDataNaoInformada() {
        // Arrange
        LocalDate dataAtual = LocalDate.now();
        TelemetriaEndpointDTO endpoint = new TelemetriaEndpointDTO(
                "/api/produto",
                50,
                120,
                20,
                300,
                new BigDecimal("1.00")
        );
        
        TelemetriaResponseDTO telemetriaResponse = TelemetriaResponseDTO.builder()
                .dataReferencia(dataAtual)
                .listaEndpoints(Collections.singletonList(endpoint))
                .build();

        when(metricasService.coletarMetricas(any(LocalDate.class)))
                .thenReturn(telemetriaResponse);

        // Act & Assert
        given()
                .when()
                .get("/api/telemetria")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("dataReferencia", equalTo(dataAtual.toString()))
                .body("listaEndpoints", hasSize(1))
                .body("listaEndpoints[0].nomeApi", equalTo("/api/produto"))
                .body("listaEndpoints[0].qtdRequisicoes", equalTo(50))
                .body("listaEndpoints[0].percentualSucesso", equalTo(1.00f));

        // Verify
        verify(metricasService, times(1))
                .coletarMetricas(any(LocalDate.class));
    }

    @Test
    @DisplayName("Deve retornar telemetria vazia quando não há dados para a data")
    void deveRetornarTelemetriaVaziaQuandoNaoHaDados() {
        // Arrange
        LocalDate dataReferencia = LocalDate.of(2024, 1, 1);
        TelemetriaResponseDTO telemetriaVazia = TelemetriaResponseDTO.builder()
                .dataReferencia(dataReferencia)
                .listaEndpoints(Collections.emptyList())
                .build();

        when(metricasService.coletarMetricas(dataReferencia))
                .thenReturn(telemetriaVazia);

        // Act & Assert
        given()
                .queryParam("dataReferencia", "2024-01-01")
                .when()
                .get("/api/telemetria")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("dataReferencia", equalTo("2024-01-01"))
                .body("listaEndpoints", hasSize(0))
                .body("listaEndpoints", empty());

        // Verify
        verify(metricasService, times(1))
                .coletarMetricas(dataReferencia);
    }
}