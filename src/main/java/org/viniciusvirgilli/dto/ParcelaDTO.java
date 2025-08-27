package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO representando uma parcela individual da simulação
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParcelaDTO {

    @JsonProperty("numero")
    @NotNull
    @Positive
    private Integer numero;

    @JsonProperty("valorAmortizacao")
    @NotNull
    @Positive
    private BigDecimal valorAmortizacao;

    @JsonProperty("valorJuros")
    @NotNull
    private BigDecimal valorJuros;

    @JsonProperty("valorPrestacao")
    @NotNull
    @Positive
    private BigDecimal valorPrestacao;
}