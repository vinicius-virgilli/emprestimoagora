package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para retorno da simulação de empréstimo
 * Modelo de envelope de retorno para simulação
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class SimulacaoResponseDTO {

    @JsonProperty("idSimulacao")
    @NotNull
    @Positive
    private Long idSimulacao;

    @JsonProperty("codigoProduto")
    @NotNull
    @Positive
    private Integer codigoProduto;

    @JsonProperty("descricaoProduto")
    @NotNull
    @Size(max = 200)
    private String descricaoProduto;

    @JsonProperty("taxaJuros")
    @NotNull
    @Positive
    private BigDecimal taxaJuros;

    @JsonProperty("resultadosSimulacao")
    @NotNull
    @NotEmpty
    @Valid
    private List<SimulacaoPorSistemaDTO> resultadosSimulacao;
}