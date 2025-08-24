package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO representando um resumo de simulação para listagem
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimulacaoResumoDTO {

    @JsonProperty("idSimulacao")
    @NotNull(message = "ID da simulação é obrigatório")
    @Positive(message = "ID da simulação deve ser positivo")
    private Long idSimulacao;

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
     * Utiliza o sistema PRICE como padrão
     */
    @JsonProperty("valorTotalParcelas")
    @NotNull(message = "Valor total das parcelasé obrigatório")
    @PositiveOrZero(message = "Valor total das parcelas deve ser positivo ou zero")
    private BigDecimal valorTotalParcelas;

}