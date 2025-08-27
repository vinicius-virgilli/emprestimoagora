package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO representando dados de telemetria de um endpoint
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TelemetriaEndpointDTO {

    /**
     * Nome da API/endpoint
     */
    @JsonProperty("nomeApi")
    @NotNull(message = "Nome da API é obrigatório")
    @Size(max = 100, message = "Nome da API deve ter no máximo 100 caracteres")
    private String nomeApi;

    /**
     * Quantidade de requisições realizadas
     */
    @JsonProperty("qtdRequisicoes")
    @NotNull(message = "Quantidade de requisições é obrigatória")
    @PositiveOrZero(message = "Quantidade de requisições deve ser positiva ou zero")
    private Integer qtdRequisicoes;

    /**
     * Tempo médio de resposta em milissegundos
     */
    @JsonProperty("tempoMedio")
    @NotNull(message = "Tempo médio é obrigatório")
    @PositiveOrZero(message = "Tempo médio deve ser positivo ou zero")
    private Integer tempoMedio;

    /**
     * Tempo mínimo de resposta em milissegundos
     */
    @JsonProperty("tempoMinimo")
    @NotNull(message = "Tempo mínimo é obrigatório")
    @PositiveOrZero(message = "Tempo mínimo deve ser positivo ou zero")
    private Integer tempoMinimo;

    /**
     * Tempo máximo de resposta em milissegundos
     */
    @JsonProperty("tempoMaximo")
    @NotNull(message = "Tempo máximo é obrigatório")
    @PositiveOrZero(message = "Tempo máximo deve ser positivo ou zero")
    private Integer tempoMaximo;

    /**
     * Percentual de sucesso (0.0 a 1.0)
     */
    @JsonProperty("percentualSucesso")
    @NotNull(message = "Percentual de sucesso é obrigatório")
    @DecimalMin(value = "0.0", message = "Percentual de sucesso deve ser no mínimo 0.0")
    @DecimalMax(value = "1.0", message = "Percentual de sucesso deve ser no máximo 1.0")
    private BigDecimal percentualSucesso;

}