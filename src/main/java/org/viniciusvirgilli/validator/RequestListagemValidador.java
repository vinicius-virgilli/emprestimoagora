package org.viniciusvirgilli.validator;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.viniciusvirgilli.dto.CamposComProblemasDTO;
import org.viniciusvirgilli.enums.TipoSimulacao;
import org.viniciusvirgilli.exceptions.ValidadorException;
import org.viniciusvirgilli.util.MessageUtils;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Slf4j
public class RequestListagemValidador {

    public void validaRequestListagem(Integer pagina, Integer tamanhoPagina, TipoSimulacao tipoSimulacao) {
        long inicio = System.currentTimeMillis();

        List<String> campos = new ArrayList<>();

        validarPagina(pagina, campos);
        // validarTamanhoPagina(tamanhoPagina, campos);

        if (!campos.isEmpty()) {
            CamposComProblemasDTO problemasDTO = CamposComProblemasDTO.of(campos);
            log.debug("[VALIDADOR] - validacao realizada com problemas: {}", problemasDTO.toString());

            throw new ValidadorException(problemasDTO);
        }

        long tempoTotal = System.currentTimeMillis() - inicio;
        log.info("[PASSO 1][VALIDADOR] - validacao realizada com sucesso em {}ms", tempoTotal);
    }

    private void validarPagina(Integer pagina, List<String> campos) {
        if (pagina == null || pagina < 0) {
            campos.add(MessageUtils.getString("solicita_listagem_todas_simulacoes_pagina_invalida"));
        }
    }

    private void validarTamanhoPagina(Integer tamanhoPagina, List<String> campos) {
        if (tamanhoPagina == null || tamanhoPagina < 0) {
            campos.add(MessageUtils.getString("solicita_listagem_todas_simulacoes_tamanho_pagina_invalida"));
        }
        if (tamanhoPagina > 10000) {
            campos.add(MessageUtils.getString("solicita_listagem_todas_simulacoes_tamanho_pagina_maior_que_o_permitido"));
        }
        if (tamanhoPagina < 1) {
            campos.add(MessageUtils.getString("solicita_listagem_todas_simulacoes_tamanho_pagina_menor_que_o_permitido"));
        }
    }
}