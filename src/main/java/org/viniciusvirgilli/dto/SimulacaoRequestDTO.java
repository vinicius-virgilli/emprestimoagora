package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.br.CPF;
import org.viniciusvirgilli.util.MessageUtils;

import java.math.BigDecimal;
import java.util.StringJoiner;

/**
 * DTO para entrada de dados da simulação de empréstimo
 * Modelo de envelope para simulação
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimulacaoRequestDTO {

    @NotNull(message = "{solicita_simulacao_emprestimo_valor_desejado_nulo}")
    @Positive(message = "{solicita_simulacao_emprestimo_valor_desejado_menor_que_zero}")
    private BigDecimal valorDesejado;

    @NotNull(message = "{solicita_simulacao_emprestimo_prazo_nulo}")
    @Positive(message = "{solicita_simulacao_emprestimo_prazo_menor_que_zero}")
    private Short prazo;

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "{", "}");

        joiner.add("valorDesejado=" + valorDesejado);
        joiner.add("prazo=" + prazo);

        return joiner.toString();
    }

}