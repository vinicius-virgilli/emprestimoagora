package org.viniciusvirgilli.dto;

import lombok.*;
import org.viniciusvirgilli.enums.TipoSimulacao;

import java.util.List;

/**
 * DTO representando o resultado de uma simulação (SAC ou PRICE)
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimulacaoPorSistemaDTO {
    private TipoSimulacao tipo;
    private List<ParcelaDTO> parcelas;
}