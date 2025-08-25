package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO representando o volume simulado por produto
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VolumeSimuladoDTO {

    /**
     * Código do produto
     */
    @JsonProperty("codigoProduto")
    @NotNull(message = "Código do produto é obrigatório")
    @Positive(message = "Código do produto deve ser positivo")
    private Integer codigoProduto;

    /**
     * Descrição do produto
     */
    @JsonProperty("descricaoProduto")
    @NotNull(message = "Descrição do produto é obrigatória")
    @Size(max = 200, message = "Descrição do produto deve ter no máximo 200 caracteres")
    private String descricaoProduto;

    /**
     * Taxa média de juros aplicada
     */
    @JsonProperty("taxaMediaJuros")
    @NotNull(message = "Taxa média de juros é obrigatória")
    @PositiveOrZero(message = "Taxa média de juros deve ser positiva ou zero")
    private BigDecimal taxaMediaJuros;

    /**
     * Valor médio das prestações
     */
    @JsonProperty("valorMedioPrestacao")
    @NotNull(message = "Valor médio da prestação é obrigatório")
    @PositiveOrZero(message = "Valor médio da prestação deve ser positivo ou zero")
    private BigDecimal valorMedioPrestacao;

    /**
     * Valor total desejado pelos clientes
     */
    @JsonProperty("valorTotalDesejado")
    @NotNull(message = "Valor total desejado é obrigatório")
    @PositiveOrZero(message = "Valor total desejado deve ser positivo ou zero")
    private BigDecimal valorTotalDesejado;

    @JsonProperty("valorTotalCredito")
    @NotNull(message = "Valor total de crédito é obrigatório")
    @PositiveOrZero(message = "Valor total de crédito deve ser positivo ou zero")
    private BigDecimal valorTotalCredito;
}