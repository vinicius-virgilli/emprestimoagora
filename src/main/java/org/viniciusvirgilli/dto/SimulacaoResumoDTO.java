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
     * Valor total das parcelas
     */
    @JsonProperty("valorTotalParcelas")
    @NotNull(message = "Valor total das parcelas é obrigatório")
    @PositiveOrZero(message = "Valor total das parcelas deve ser positivo ou zero")
    private BigDecimal valorTotalParcelas;

    /**
     * Construtor padrão
     */
    public SimulacaoResumoDTO() {
    }

    /**
     * Construtor completo
     */
    public SimulacaoResumoDTO(Long idSimulacao, BigDecimal valorDesejado, Short prazo, BigDecimal valorTotalParcelas) {
        this.idSimulacao = idSimulacao;
        this.valorDesejado = valorDesejado;
        this.prazo = prazo;
        this.valorTotalParcelas = valorTotalParcelas;
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

    public BigDecimal getValorTotalParcelas() {
        return valorTotalParcelas;
    }

    public void setValorTotalParcelas(BigDecimal valorTotalParcelas) {
        this.valorTotalParcelas = valorTotalParcelas;
    }

    @Override
    public String toString() {
        return "SimulacaoResumoDTO{" +
                "idSimulacao=" + idSimulacao +
                ", valorDesejado=" + valorDesejado +
                ", prazo=" + prazo +
                ", valorTotalParcelas=" + valorTotalParcelas +
                '}';
    }
}