package org.viniciusvirgilli.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.viniciusvirgilli.dao.ProdutoDao;
import org.viniciusvirgilli.model.hack.Produto;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ProdutoController
 */
@QuarkusTest
@DisplayName("Testes do ProdutoController")
class ProdutoControllerTest {

    @InjectMock
    ProdutoDao produtoDao;

    @BeforeEach
    void setUp() {
        // Reset dos mocks antes de cada teste
        reset(produtoDao);
    }

    @Nested
    @DisplayName("Testes do método health")
    class HealthTests {

        @Test
        @DisplayName("Deve retornar health check com sucesso")
        void deveRetornarHealthCheckComSucesso() {
            // Act & Assert
            given()
                    .when()
                    .get("/api/produtos/health")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("status", equalTo("OK"))
                    .body("mensagem", equalTo("Serviço de produtos funcionando"));

            // Verify - O método health não chama nenhum serviço
            verifyNoInteractions(produtoDao);
        }

        @Test
        @DisplayName("Deve retornar health check independente do estado do DAO")
        void deveRetornarHealthCheckIndependenteDoEstadoDAO() {
            // Arrange - Simula que o DAO pode estar com problemas
            when(produtoDao.findAll()).thenThrow(new RuntimeException("Erro de conexão"));

            // Act & Assert - O health check ainda deve funcionar pois não usa o DAO
            given()
                    .when()
                    .get("/api/produtos/health")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("status", equalTo("OK"))
                    .body("mensagem", equalTo("Serviço de produtos funcionando"));

            // Verify - O método health não deve chamar o DAO
            verifyNoInteractions(produtoDao);
        }

        @Test
        @DisplayName("Deve retornar resposta JSON válida com estrutura correta")
        void deveRetornarRespostaJSONValidaComEstruturaCorreta() {
            // Act & Assert
            given()
                    .when()
                    .get("/api/produtos/health")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("status", equalTo("OK"))
                    .body("mensagem", equalTo("Serviço de produtos funcionando"))
                    // Verifica que a resposta contém apenas os campos esperados
                    .body("size()", equalTo(2));

            // Verify
            verifyNoInteractions(produtoDao);
        }
    }

    @Nested
    @DisplayName("Testes do método listarProdutos")
    class ListarProdutosTests {

        @Test
        @DisplayName("Deve listar produtos com sucesso e validar todos os campos")
        void deveListarProdutosComSucessoEValidarTodosOsCampos() {
            // Arrange
            Produto produto1 = Produto.builder()
                    .codigoProduto(1)
                    .nomeProduto("Empréstimo Pessoal")
                    .percentualTaxaJuros(new BigDecimal("0.025000000"))
                    .numeroMinimoMeses((short) 6)
                    .numeroMaximoMeses((short) 60)
                    .valorMinimo(new BigDecimal("1000.00"))
                    .valorMaximo(new BigDecimal("50000.00"))
                    .build();

            Produto produto2 = Produto.builder()
                    .codigoProduto(2)
                    .nomeProduto("Financiamento Veículo")
                    .percentualTaxaJuros(new BigDecimal("0.018000000"))
                    .numeroMinimoMeses((short) 12)
                    .numeroMaximoMeses((short) 84)
                    .valorMinimo(new BigDecimal("5000.00"))
                    .valorMaximo(new BigDecimal("200000.00"))
                    .build();

            List<Produto> produtos = Arrays.asList(produto1, produto2);
            when(produtoDao.findAll()).thenReturn(produtos);

            // Act & Assert
            given()
                    .when()
                    .get("/api/produtos")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("size()", equalTo(2))
                    // Validações do primeiro produto
                    .body("[0].codigoProduto", equalTo(1))
                    .body("[0].nomeProduto", equalTo("Empréstimo Pessoal"))
                    .body("[0].percentualTaxaJuros", equalTo(0.025f))
                    .body("[0].numeroMinimoMeses", equalTo(6))
                    .body("[0].numeroMaximoMeses", equalTo(60))
                    .body("[0].valorMinimo", equalTo(1000.00f))
                    .body("[0].valorMaximo", equalTo(50000.00f))
                    // Validações do segundo produto
                    .body("[1].codigoProduto", equalTo(2))
                    .body("[1].nomeProduto", equalTo("Financiamento Veículo"))
                    .body("[1].percentualTaxaJuros", equalTo(0.018f))
                    .body("[1].numeroMinimoMeses", equalTo(12))
                    .body("[1].numeroMaximoMeses", equalTo(84))
                    .body("[1].valorMinimo", equalTo(5000.00f))
                    .body("[1].valorMaximo", equalTo(200000.00f));

            // Verify
            verify(produtoDao, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve listar produtos ordenados por código")
        void deveListarProdutosOrdenadosPorCodigo() {
            // Arrange - Produtos em ordem não sequencial
            Produto produto3 = Produto.builder()
                    .codigoProduto(3)
                    .nomeProduto("Cartão de Crédito")
                    .percentualTaxaJuros(new BigDecimal("0.035000000"))
                    .numeroMinimoMeses((short) 1)
                    .numeroMaximoMeses((short) 12)
                    .valorMinimo(new BigDecimal("100.00"))
                    .valorMaximo(new BigDecimal("10000.00"))
                    .build();

            Produto produto1 = Produto.builder()
                    .codigoProduto(1)
                    .nomeProduto("Empréstimo Pessoal")
                    .percentualTaxaJuros(new BigDecimal("0.025000000"))
                    .numeroMinimoMeses((short) 6)
                    .numeroMaximoMeses((short) 60)
                    .valorMinimo(new BigDecimal("1000.00"))
                    .valorMaximo(new BigDecimal("50000.00"))
                    .build();

            // O DAO já retorna ordenado por código
            List<Produto> produtos = Arrays.asList(produto1, produto3);
            when(produtoDao.findAll()).thenReturn(produtos);

            // Act & Assert
            given()
                    .when()
                    .get("/api/produtos")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("size()", equalTo(2))
                    .body("[0].codigoProduto", equalTo(1))
                    .body("[1].codigoProduto", equalTo(3))
                    // Verifica que estão ordenados
                    .body("codigoProduto", contains(1, 3));

            // Verify
            verify(produtoDao, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há produtos")
        void deveRetornarListaVaziaQuandoNaoHaProdutos() {
            // Arrange
            when(produtoDao.findAll()).thenReturn(Collections.emptyList());

            // Act & Assert
            given()
                    .when()
                    .get("/api/produtos")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("size()", equalTo(0))
                    .body("$", hasSize(0));

            // Verify
            verify(produtoDao, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve listar um único produto corretamente")
        void deveListarUmUnicoProdutoCorretamente() {
            // Arrange
            Produto produto = Produto.builder()
                    .codigoProduto(5)
                    .nomeProduto("Crediário")
                    .percentualTaxaJuros(new BigDecimal("0.015000000"))
                    .numeroMinimoMeses((short) 3)
                    .numeroMaximoMeses((short) 24)
                    .valorMinimo(new BigDecimal("500.00"))
                    .valorMaximo(new BigDecimal("15000.00"))
                    .build();

            when(produtoDao.findAll()).thenReturn(Collections.singletonList(produto));

            // Act & Assert
            given()
                    .when()
                    .get("/api/produtos")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("size()", equalTo(1))
                    .body("[0].codigoProduto", equalTo(5))
                    .body("[0].nomeProduto", equalTo("Crediário"))
                    .body("[0].percentualTaxaJuros", equalTo(0.015f))
                    .body("[0].numeroMinimoMeses", equalTo(3))
                    .body("[0].numeroMaximoMeses", equalTo(24))
                    .body("[0].valorMinimo", equalTo(500.00f))
                    .body("[0].valorMaximo", equalTo(15000.00f));

            // Verify
            verify(produtoDao, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve listar produtos com campos opcionais nulos")
        void deveListarProdutosComCamposOpcionaisNulos() {
            // Arrange - Produto com campos opcionais nulos
            Produto produto = Produto.builder()
                    .codigoProduto(10)
                    .nomeProduto("Produto Básico")
                    .percentualTaxaJuros(new BigDecimal("0.020000000"))
                    .numeroMinimoMeses((short) 1)
                    .numeroMaximoMeses(null) // Campo opcional
                    .valorMinimo(new BigDecimal("100.00"))
                    .valorMaximo(null) // Campo opcional
                    .build();

            when(produtoDao.findAll()).thenReturn(Collections.singletonList(produto));

            // Act & Assert
            given()
                    .when()
                    .get("/api/produtos")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("size()", equalTo(1))
                    .body("[0].codigoProduto", equalTo(10))
                    .body("[0].nomeProduto", equalTo("Produto Básico"))
                    .body("[0].percentualTaxaJuros", equalTo(0.020f))
                    .body("[0].numeroMinimoMeses", equalTo(1))
                    .body("[0].numeroMaximoMeses", nullValue())
                    .body("[0].valorMinimo", equalTo(100.00f))
                    .body("[0].valorMaximo", nullValue());

            // Verify
            verify(produtoDao, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve validar estrutura JSON da resposta com múltiplos produtos")
        void deveValidarEstruturaJSONDaRespostaComMultiplosProdutos() {
            // Arrange
            List<Produto> produtos = Arrays.asList(
                    Produto.builder()
                            .codigoProduto(1)
                            .nomeProduto("Produto 1")
                            .percentualTaxaJuros(new BigDecimal("0.025000000"))
                            .numeroMinimoMeses((short) 6)
                            .numeroMaximoMeses((short) 60)
                            .valorMinimo(new BigDecimal("1000.00"))
                            .valorMaximo(new BigDecimal("50000.00"))
                            .build(),
                    Produto.builder()
                            .codigoProduto(2)
                            .nomeProduto("Produto 2")
                            .percentualTaxaJuros(new BigDecimal("0.018000000"))
                            .numeroMinimoMeses((short) 12)
                            .numeroMaximoMeses((short) 84)
                            .valorMinimo(new BigDecimal("5000.00"))
                            .valorMaximo(new BigDecimal("200000.00"))
                            .build(),
                    Produto.builder()
                            .codigoProduto(3)
                            .nomeProduto("Produto 3")
                            .percentualTaxaJuros(new BigDecimal("0.030000000"))
                            .numeroMinimoMeses((short) 1)
                            .numeroMaximoMeses((short) 12)
                            .valorMinimo(new BigDecimal("100.00"))
                            .valorMaximo(new BigDecimal("10000.00"))
                            .build()
            );

            when(produtoDao.findAll()).thenReturn(produtos);

            // Act & Assert
            given()
                    .when()
                    .get("/api/produtos")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("size()", equalTo(3))
                    // Verifica que todos os produtos têm os campos obrigatórios
                    .body("codigoProduto", everyItem(notNullValue()))
                    .body("nomeProduto", everyItem(notNullValue()))
                    .body("percentualTaxaJuros", everyItem(notNullValue()))
                    .body("numeroMinimoMeses", everyItem(notNullValue()))
                    .body("valorMinimo", everyItem(notNullValue()))
                    // Verifica tipos dos campos
                    .body("codigoProduto", everyItem(instanceOf(Integer.class)))
                    .body("nomeProduto", everyItem(instanceOf(String.class)))
                    .body("percentualTaxaJuros", everyItem(instanceOf(Float.class)))
                    .body("numeroMinimoMeses", everyItem(instanceOf(Integer.class)))
                    .body("valorMinimo", everyItem(instanceOf(Float.class)));

            // Verify
            verify(produtoDao, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("Testes de cenários de erro")
    class ErrorTests {

        @Test
        @DisplayName("Deve retornar erro 500 quando ocorre RuntimeException no DAO")
        void deveRetornarErro500QuandoOcorreRuntimeExceptionNoDAO() {
            // Arrange
            when(produtoDao.findAll()).thenThrow(new RuntimeException("Erro de conexão com banco"));

            // Act & Assert
            given()
                    .when()
                    .get("/api/produtos")
                    .then()
                    .statusCode(500);

            // Verify
            verify(produtoDao, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve retornar erro 500 quando ocorre IllegalStateException no DAO")
        void deveRetornarErro500QuandoOcorreIllegalStateExceptionNoDAO() {
            // Arrange
            when(produtoDao.findAll()).thenThrow(new IllegalStateException("Estado inválido do EntityManager"));

            // Act & Assert
            given()
                    .when()
                    .get("/api/produtos")
                    .then()
                    .statusCode(500);

            // Verify
            verify(produtoDao, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve retornar erro 500 quando ocorre NullPointerException no DAO")
        void deveRetornarErro500QuandoOcorreNullPointerExceptionNoDAO() {
            // Arrange
            when(produtoDao.findAll()).thenThrow(new NullPointerException("Referência nula no DAO"));

            // Act & Assert
            given()
                    .when()
                    .get("/api/produtos")
                    .then()
                    .statusCode(500);

            // Verify
            verify(produtoDao, times(1)).findAll();
        }
    }
}
