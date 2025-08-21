package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * DTO representando um resumo de simulação para listagem
 */
public class SimulacaoResumoDTO {

    /**
     * ID único da simulação
     */
    @JsonProperty("idSimulacao")
    @NotNull(message = "ID da simulação é obrigatório")
    @Positive(message = "ID da simulação deve ser positivo")
    private Long idSimulacao;

    /**
     * Valor desejado para o empréstimo
     */
    @JsonProperty("valorDesejado")
    @NotNull(message = "Valor desejado é obrigatório")
    @PositiveOrZero(message = "Valor desejado deve ser positivo ou zero")
    private BigDecimal valorDesejado;

    /**
     * Prazo em meses
     */
    @JsonProperty("Prazo")
    @NotNull(message = "Prazo é obrigatório")
    @Positive(message = "Prazo deve ser positivo")
    private Short prazo;

    /**
     * Valor total das parcelas SAC
     */
    @JsonProperty("valorTotalParcelasSAC")
    @NotNull(message = "Valor total das parcelas SAC é obrigatório")
    @PositiveOrZero(message = "Valor total das parcelas SAC deve ser positivo ou zero")
    private BigDecimal valorTotalParcelasSAC;

    /**
     * Valor total das parcelas PRICE
     */
    @JsonProperty("valorTotalParcelasPRICE")
    @NotNull(message = "Valor total das parcelas PRICE é obrigatório")
    @PositiveOrZero(message = "Valor total das parcelas PRICE deve ser positivo ou zero")
    private BigDecimal valorTotalParcelasPRICE;


    /**
     * Construtor padrão
     */
    public SimulacaoResumoDTO() {
    }

    /**
     * Construtor completo
     */
    public SimulacaoResumoDTO(Long idSimulacao, BigDecimal valorDesejado, Short prazo, 
                             BigDecimal valorTotalParcelasSAC, BigDecimal valorTotalParcelasPRICE) {
        this.idSimulacao = idSimulacao;
        this.valorDesejado = valorDesejado;
        this.prazo = prazo;
        this.valorTotalParcelasSAC = valorTotalParcelasSAC;
        this.valorTotalParcelasPRICE = valorTotalParcelasPRICE;
    }

    // Getters e Setters
    public Long getIdSimulacao() {
        return idSimulacao;
    }

    public void setIdSimulacao(Long idSimulacao) {
        this.idSimulacao = idSimulacao;
    }

    public BigDecimal getValorDesejado() {
        return valorDesejado;
    }

    public void setValorDesejado(BigDecimal valorDesejado) {
        this.valorDesejado = valorDesejado;
    }

    public Short getPrazo() {
        return prazo;
    }

    public void setPrazo(Short prazo) {
        this.prazo = prazo;
    }

    public BigDecimal getValorTotalParcelasSAC() {
        return valorTotalParcelasSAC;
    }

    public void setValorTotalParcelasSAC(BigDecimal valorTotalParcelasSAC) {
        this.valorTotalParcelasSAC = valorTotalParcelasSAC;
    }

    public BigDecimal getValorTotalParcelasPRICE() {
        return valorTotalParcelasPRICE;
    }

    public void setValorTotalParcelasPRICE(BigDecimal valorTotalParcelasPRICE) {
        this.valorTotalParcelasPRICE = valorTotalParcelasPRICE;
    }

    @Override
    public String toString() {
        return "SimulacaoResumoDTO{" +
                "idSimulacao=" + idSimulacao +
                ", valorDesejado=" + valorDesejado +
                ", prazo=" + prazo +
                ", valorTotalParcelasSAC=" + valorTotalParcelasSAC +
                ", valorTotalParcelasPRICE=" + valorTotalParcelasPRICE +
                '}';
    }
}