package org.viniciusvirgilli.validator;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.viniciusvirgilli.dto.SimulacaoRequestDTO;
import org.viniciusvirgilli.exceptions.APIEmprestimoAgoraException;
import org.viniciusvirgilli.util.MessageUtils;

import java.math.BigDecimal;

@ApplicationScoped
@Slf4j
public class RequestSimulacaoValidator {

    public void validaRequestSimulacao(SimulacaoRequestDTO dto) {
        long inicio = System.currentTimeMillis();

        StringBuilder campos = new StringBuilder();

        // validaValorDesejado(dto.getValorDesejado(), campos);
        validaPrazo(dto.getPrazo(), campos);
        // validaNomeCliente(dto.getNomeCliente(), campos);
        // validaCpfCliente(dto.getCpfCliente(), campos);

        if (!campos.isEmpty()) {
            log.error("[VALIDADOR] - validacao realizada com problemas");
            throw new APIEmprestimoAgoraException(
                    MessageUtils.getString("solicita_simulacao_emprestimo_campos_com_problemas") + " " + campos);
        }

        long tempoTotal = System.currentTimeMillis() - inicio;
        log.info("[PASSO 1][VALIDADOR] - validacao realizada com sucesso em {}ms", tempoTotal);
    }

//    private void validaValorDesejado(BigDecimal valorDesejado, StringBuilder campos) {
//        if (valorDesejado == null) {
//            campos.append(System.lineSeparator());
//            campos.append(MessageUtils.getString("solicita_simulacao_emprestimo_valor_desejado_nulo"));
//        } else if (valorDesejado.compareTo(BigDecimal.ZERO) <= 0) {
//            campos.append(MessageUtils.getString("solicita_simulacao_emprestimo_valor_desejado_menor_que_zero"));
//        }
//    }

    private void validaPrazo(Short prazo, StringBuilder campos) {
//        if (prazo == null) {
//            campos.append(MessageUtils.getString("solicita_simulacao_emprestimo_prazo_nulo"));
//        } else if (prazo <= 0) {
//            campos.append(MessageUtils.getString("solicita_simulacao_emprestimo_prazo_menor_que_zero"));
//        }
        if (prazo != null && prazo % 1 != 0) {
            campos.append(MessageUtils.getString("solicita_simulacao_emprestimo_prazo_nao_inteiro"));
        }
    }

//    private void validaNomeCliente(String nomeCliente, StringBuilder campos) {
//        if (nomeCliente != null && nomeCliente.length() > 140) {
//            campos.append(MessageUtils.getString("solicita_simulacao_emprestimo_nome_cliente_maior_que_o_permitido"));
//        }
//    }
//
//    private void validaCpfCliente(String cpf, StringBuilder campos) {
//        if (cpf == null || !cpf.matches("\\d{11}")) {
//            campos.append(MessageUtils.getString("solicita_simulacao_emprestimo_cpf_cliente_formato_invalido"));
//        }
//    }
}
