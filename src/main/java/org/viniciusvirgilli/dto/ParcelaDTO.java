package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO representando uma parcela individual da simulação
 */
public class ParcelaDTO {

    /**
     * Número da parcela
     */
    @JsonProperty("numero")
    @NotNull
    @Positive
    private Integer numero;

    /**
     * Valor da amortização da parcela
     */
    @JsonProperty("valorAmortizacao")
    @NotNull
    @Positive
    private BigDecimal valorAmortizacao;

    /**
     * Valor dos juros da parcela
     */
    @JsonProperty("valorJuros")
    @NotNull
    private BigDecimal valorJuros;

    /**
     * Valor total da prestação (amortização + juros)
     */
    @JsonProperty("valorPrestacao")
    @NotNull
    @Positive
    private BigDecimal valorPrestacao;

    /**
     * Construtor padrão
     */
    public ParcelaDTO() {
    }

    /**
     * Construtor completo
     */
    public ParcelaDTO(Integer numero, BigDecimal valorAmortizacao, BigDecimal valorJuros, BigDecimal valorPrestacao) {
        this.numero = numero;
        this.valorAmortizacao = valorAmortizacao;
        this.valorJuros = valorJuros;
        this.valorPrestacao = valorPrestacao;
    }

    // Getters e Setters
    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public BigDecimal getValorAmortizacao() {
        return valorAmortizacao;
    }

    public void setValorAmortizacao(BigDecimal valorAmortizacao) {
        this.valorAmortizacao = valorAmortizacao;
    }

    public BigDecimal getValorJuros() {
        return valorJuros;
    }

    public void setValorJuros(BigDecimal valorJuros) {
        this.valorJuros = valorJuros;
    }

    public BigDecimal getValorPrestacao() {
        return valorPrestacao;
    }

    public void setValorPrestacao(BigDecimal valorPrestacao) {
        this.valorPrestacao = valorPrestacao;
    }

    @Override
    public String toString() {
        return "ParcelaDTO{" +
                "numero=" + numero +
                ", valorAmortizacao=" + valorAmortizacao +
                ", valorJuros=" + valorJuros +
                ", valorPrestacao=" + valorPrestacao +
                '}';
    }
}