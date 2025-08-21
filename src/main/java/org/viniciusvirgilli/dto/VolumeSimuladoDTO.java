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

    /**
     * Valor médio das prestações SAC
     */
    @JsonProperty("valorMedioPrestacaoSAC")
    @NotNull(message = "Valor médio da prestação SAC é obrigatório")
    @PositiveOrZero(message = "Valor médio da prestação SAC deve ser positivo ou zero")
    private BigDecimal valorMedioPrestacaoSAC;

    /**
     * Valor total de crédito SAC
     */
    @JsonProperty("valorTotalCreditoSAC")
    @NotNull(message = "Valor total de crédito SAC é obrigatório")
    @PositiveOrZero(message = "Valor total de crédito SAC deve ser positivo ou zero")
    private BigDecimal valorTotalCreditoSAC;

    /**
     * Valor médio das prestações PRICE
     */
    @JsonProperty("valorMedioPrestacaoPRICE")
    @NotNull(message = "Valor médio da prestação PRICE é obrigatório")
    @PositiveOrZero(message = "Valor médio da prestação PRICE deve ser positivo ou zero")
    private BigDecimal valorMedioPrestacaoPRICE;

    /**
     * Valor total de crédito PRICE
     */
    @JsonProperty("valorTotalCreditoPRICE")
    @NotNull(message = "Valor total de crédito PRICE é obrigatório")
    @PositiveOrZero(message = "Valor total de crédito PRICE deve ser positivo ou zero")
    private BigDecimal valorTotalCreditoPRICE;

    /**
     * Construtor padrão
     */
    public VolumeSimuladoDTO() {
    }

    /**
     * Construtor completo
     */
    public VolumeSimuladoDTO(Integer codigoProduto, String descricaoProduto, BigDecimal taxaMediaJuros,
                            BigDecimal valorMedioPrestacao, BigDecimal valorTotalDesejado, 
                            BigDecimal valorMedioPrestacaoSAC, BigDecimal valorTotalCreditoSAC,
                            BigDecimal valorMedioPrestacaoPRICE, BigDecimal valorTotalCreditoPRICE) {
        this.codigoProduto = codigoProduto;
        this.descricaoProduto = descricaoProduto;
        this.taxaMediaJuros = taxaMediaJuros;
        this.valorMedioPrestacao = valorMedioPrestacao;
        this.valorTotalDesejado = valorTotalDesejado;
        this.valorMedioPrestacaoSAC = valorMedioPrestacaoSAC;
        this.valorTotalCreditoSAC = valorTotalCreditoSAC;
        this.valorMedioPrestacaoPRICE = valorMedioPrestacaoPRICE;
        this.valorTotalCreditoPRICE = valorTotalCreditoPRICE;
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

    public BigDecimal getTaxaMediaJuros() {
        return taxaMediaJuros;
    }

    public void setTaxaMediaJuros(BigDecimal taxaMediaJuros) {
        this.taxaMediaJuros = taxaMediaJuros;
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

    public BigDecimal getValorMedioPrestacaoSAC() {
        return valorMedioPrestacaoSAC;
    }

    public void setValorMedioPrestacaoSAC(BigDecimal valorMedioPrestacaoSAC) {
        this.valorMedioPrestacaoSAC = valorMedioPrestacaoSAC;
    }

    public BigDecimal getValorTotalCreditoSAC() {
        return valorTotalCreditoSAC;
    }

    public void setValorTotalCreditoSAC(BigDecimal valorTotalCreditoSAC) {
        this.valorTotalCreditoSAC = valorTotalCreditoSAC;
    }

    public BigDecimal getValorMedioPrestacaoPRICE() {
        return valorMedioPrestacaoPRICE;
    }

    public void setValorMedioPrestacaoPRICE(BigDecimal valorMedioPrestacaoPRICE) {
        this.valorMedioPrestacaoPRICE = valorMedioPrestacaoPRICE;
    }

    public BigDecimal getValorTotalCreditoPRICE() {
        return valorTotalCreditoPRICE;
    }

    public void setValorTotalCreditoPRICE(BigDecimal valorTotalCreditoPRICE) {
        this.valorTotalCreditoPRICE = valorTotalCreditoPRICE;
    }

    @Override
    public String toString() {
        return "VolumeSimuladoDTO{" +
                "codigoProduto=" + codigoProduto +
                ", descricaoProduto='" + descricaoProduto + '\'' +
                ", taxaMediaJuros=" + taxaMediaJuros +
                ", valorMedioPrestacao=" + valorMedioPrestacao +
                ", valorTotalDesejado=" + valorTotalDesejado +
                ", valorMedioPrestacaoSAC=" + valorMedioPrestacaoSAC +
                ", valorTotalCreditoSAC=" + valorTotalCreditoSAC +
                ", valorMedioPrestacaoPRICE=" + valorMedioPrestacaoPRICE +
                ", valorTotalCreditoPRICE=" + valorTotalCreditoPRICE +
                '}';
    }
}