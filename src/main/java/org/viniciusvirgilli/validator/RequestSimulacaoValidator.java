package org.viniciusvirgilli.validator;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.viniciusvirgilli.dto.CamposComProblemasDTO;
import org.viniciusvirgilli.dto.SimulacaoRequestDTO;
import org.viniciusvirgilli.exceptions.ValidadorException;
import org.viniciusvirgilli.util.MessageUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Slf4j
public class RequestSimulacaoValidator {

    Integer nomeClienteTamanhoMaximo = 140;

    public void validaRequestSimulacao(SimulacaoRequestDTO dto) {
        long inicio = System.currentTimeMillis();

        List<String> campos = new ArrayList<>();

        validaValorDesejado(dto.getValorDesejado(), campos);
        validaPrazo(dto.getPrazo(), campos);
        validaNomeCliente(dto.getNomeCliente(), campos);
        validaCpfCliente(dto.getCpfCliente(), campos);

        if (!campos.isEmpty()) {
            CamposComProblemasDTO problemasDTO = CamposComProblemasDTO.of(campos);

            log.error("[VALIDADOR] - validacao detectou problemas: {}", problemasDTO.toString());
            
            throw new ValidadorException(problemasDTO);
        }

        long tempoTotal = System.currentTimeMillis() - inicio;
        log.info("[PASSO 1][VALIDADOR] - validacao realizada com sucesso em {}ms", tempoTotal);
    }

    private void validaValorDesejado(BigDecimal valorDesejado, List<String> campos) {
        if (valorDesejado == null) {
            campos.add(MessageUtils.getString("solicita_simulacao_emprestimo_valor_desejado_nulo"));
            return;
        } else if (!valorDesejado.toString().matches("^\\d+(\\.\\d{1,2})?$")) {
            campos.add(MessageUtils.getString("solicita_simulacao_emprestimo_valor_desejado_tipo_invalido"));
        }
        if (valorDesejado.compareTo(BigDecimal.ZERO) <= 0) {
            campos.add(MessageUtils.getString("solicita_simulacao_emprestimo_valor_desejado_menor_que_zero"));
        }
    }

    private void validaPrazo(Short prazo, List<String> campos) {
        if (prazo == null) {
            campos.add(MessageUtils.getString("solicita_simulacao_emprestimo_prazo_nulo"));
            return;
        }
        if (prazo <= 0) {
            campos.add(MessageUtils.getString("solicita_simulacao_emprestimo_prazo_menor_que_zero"));
        }
    }

    private void validaNomeCliente(String nomeCliente, List<String> campos) {
        if (nomeCliente != null && nomeCliente.length() > nomeClienteTamanhoMaximo) {
            campos.add(MessageUtils.getString("solicita_simulacao_emprestimo_nome_cliente_maior_que_o_permitido"));
        }
        if (nomeCliente != null && !nomeCliente.matches("^[\\p{L}\\s]+$")) {
            campos.add(MessageUtils.getString("solicita_simulacao_emprestimo_nome_cliente_tipo_invalido"));
        }
    }

    private void validaCpfCliente(String cpf, List<String> campos) {
        if (cpf == null) {
            return;
        }
        if (!cpf.matches("\\d{11}")) {
            campos.add(MessageUtils.getString("solicita_simulacao_emprestimo_cpf_cliente_formato_invalido"));
        }
    }
}
