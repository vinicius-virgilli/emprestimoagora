package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO representando o volume simulado por produto
 */
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
    @JsonProperty("taxaMediaJuro")
    @NotNull(message = "Taxa média de juros é obrigatória")
    @PositiveOrZero(message = "Taxa média de juros deve ser positiva ou zero")
    private BigDecimal taxaMediaJuro;

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

    /**
     * Valor total de crédito disponibilizado
     */
    @JsonProperty("valorTotalCredito")
    @NotNull(message = "Valor total de crédito é obrigatório")
    @PositiveOrZero(message = "Valor total de crédito deve ser positivo ou zero")
    private BigDecimal valorTotalCredito;

    /**
     * Construtor padrão
     */
    public VolumeSimuladoDTO() {
    }

    /**
     * Construtor completo
     */
    public VolumeSimuladoDTO(Integer codigoProduto, String descricaoProduto, BigDecimal taxaMediaJuro,
                            BigDecimal valorMedioPrestacao, BigDecimal valorTotalDesejado, BigDecimal valorTotalCredito) {
        this.codigoProduto = codigoProduto;
        this.descricaoProduto = descricaoProduto;
        this.taxaMediaJuro = taxaMediaJuro;
        this.valorMedioPrestacao = valorMedioPrestacao;
        this.valorTotalDesejado = valorTotalDesejado;
        this.valorTotalCredito = valorTotalCredito;
    }

    // Getters e Setters
    public Integer getCodigoProduto() {
        return codigoProduto;
    }

    public void setCodigoProduto(Integer codigoProduto) {
        this.codigoProduto = codigoProduto;
    }

    public String getDescricaoProduto() {
        return descricaoProduto;
    }

    public void setDescricaoProduto(String descricaoProduto) {
        this.descricaoProduto = descricaoProduto;
    }

    public BigDecimal getTaxaMediaJuro() {
        return taxaMediaJuro;
    }

    public void setTaxaMediaJuro(BigDecimal taxaMediaJuro) {
        this.taxaMediaJuro = taxaMediaJuro;
    }

    public BigDecimal getValorMedioPrestacao() {
        return valorMedioPrestacao;
    }

    public void setValorMedioPrestacao(BigDecimal valorMedioPrestacao) {
        this.valorMedioPrestacao = valorMedioPrestacao;
    }

    public BigDecimal getValorTotalDesejado() {
        return valorTotalDesejado;
    }

    public void setValorTotalDesejado(BigDecimal valorTotalDesejado) {
        this.valorTotalDesejado = valorTotalDesejado;
    }

    public BigDecimal getValorTotalCredito() {
        return valorTotalCredito;
    }

    public void setValorTotalCredito(BigDecimal valorTotalCredito) {
        this.valorTotalCredito = valorTotalCredito;
    }

    @Override
    public String toString() {
        return "VolumeSimuladoDTO{" +
                "codigoProduto=" + codigoProduto +
                ", descricaoProduto='" + descricaoProduto + '\'' +
                ", taxaMediaJuro=" + taxaMediaJuro +
                ", valorMedioPrestacao=" + valorMedioPrestacao +
                ", valorTotalDesejado=" + valorTotalDesejado +
                ", valorTotalCredito=" + valorTotalCredito +
                '}';
    }
}