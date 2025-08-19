package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO para entrada de dados da simulação de empréstimo
 * Modelo de envelope para simulação
 */
public class SimulacaoRequestDTO {

    /**
     * Valor desejado para o empréstimo
     */
    @JsonProperty("valorDesejado")
    @NotNull(message = "Valor desejado é obrigatório")
    @Positive(message = "Valor desejado deve ser positivo")
    private BigDecimal valorDesejado;

    /**
     * Prazo em meses para pagamento
     */
    @JsonProperty("prazo")
    @NotNull(message = "Prazo é obrigatório")
    @Positive(message = "Prazo deve ser positivo")
    private Short prazo;

    /**
     * Construtor padrão
     */
    public SimulacaoRequestDTO() {
    }

    /**
     * Construtor completo
     */
    public SimulacaoRequestDTO(BigDecimal valorDesejado, Short prazo) {
        this.valorDesejado = valorDesejado;
        this.prazo = prazo;
    }

    // Getters e Setters
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

    @Override
    public String toString() {
        return "SimulacaoRequestDTO{" +
                "valorDesejado=" + valorDesejado +
                ", prazo=" + prazo +
                '}';
    }
}