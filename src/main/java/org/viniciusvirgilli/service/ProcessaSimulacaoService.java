package org.viniciusvirgilli.service;

import io.smallrye.context.api.ManagedExecutorConfig;
import io.smallrye.context.api.NamedInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;
import org.viniciusvirgilli.dao.SimulacaoRealizadaDao;
import org.viniciusvirgilli.dto.*;
import org.viniciusvirgilli.exceptions.APIEmprestimoAgoraException;
import org.viniciusvirgilli.model.hack.Produto;
import org.viniciusvirgilli.enums.TipoSimulacao;
import org.viniciusvirgilli.dao.ProdutoDao;
import org.viniciusvirgilli.model.local.SimulacaoRealizada;
import org.viniciusvirgilli.util.CalculadoraUtil;
import org.viniciusvirgilli.validator.RequestListagemValidador;
import org.viniciusvirgilli.validator.RequestSimulacaoValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável pela lógica de negócio das simulações de empréstimo
 */
@Slf4j
@ApplicationScoped
public class ProcessaSimulacaoService {

    @Inject
    ProdutoDao produtoDao;

    @Inject
    ProdutoCacheService produtoCacheService;

    @Inject
    EventHubService eventHubService;

    @Inject
    RequestSimulacaoValidator validator;

    @Inject
    RequestListagemValidador requestListagemValidador;

    @Inject
    SimulacaoRealizadaDao simulacaoRealizadaDao;

    @Inject
    SimulacaoCacheService simulacaoCacheService;

    @Inject
    @ManagedExecutorConfig(propagated = ThreadContext.ALL_REMAINING)
    @NamedInstance("MyExecutor")
    ManagedExecutor managedExecutor;

    /**
     * Executa o processamento da requisição de simulação de empréstimo
     *
     * @return SimulacaoResponseDTO
     */
    public SimulacaoResponseDTO executar(SimulacaoRequestDTO requestDTO) {

        validator.validaRequestSimulacao(requestDTO);

        Produto produto = buscarProdutoCompativel(requestDTO.getValorDesejado(), requestDTO.getPrazo());

        List<SimulacaoPorSistemaDTO> resultados = calcularSimulacoes(requestDTO.getValorDesejado(), requestDTO.getPrazo(), produto.percentualTaxaJuros);

        SimulacaoRealizada simulacaoRealizada = persistirSimulacao(produto, resultados, requestDTO);

        SimulacaoResponseDTO simulacaoResponseDTO = montaSimulacaoResponseDTO(simulacaoRealizada, resultados);

        enviarSimulacaoEventHub(simulacaoResponseDTO);

        return simulacaoResponseDTO;

    }

    private void enviarSimulacaoEventHub(SimulacaoResponseDTO simulacaoResponseDTO) {
        managedExecutor.runAsync(() -> {
            long inicio = System.currentTimeMillis();

            try {
                eventHubService.enviarEventoSimulacao(simulacaoResponseDTO);

                long tempoExecucao = System.currentTimeMillis() - inicio;
                log.info("[PASSO 5][ENVIAR EVENT HUB] - Enviou simulacao de ID: {} para o Event Hub em {}ms\n", simulacaoResponseDTO.getIdSimulacao(), tempoExecucao);

            } catch (Exception e) {
                log.error("[ERRO][EVENT HUB] - Erro ao enviar simulacao para o Event Hub", e);
                throw new APIEmprestimoAgoraException("[ERRO][EVENT HUB] - Erro ao enviar simulacao para o Event Hub", e);
            }
        }).exceptionally(ex -> {
            log.error("[ERRO][EVENT HUB] - Excecao assincrona nao tratada", ex);
            return null;
        });
    }


    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public SimulacaoRealizada persistirSimulacao(Produto produto, List<SimulacaoPorSistemaDTO> resultados, SimulacaoRequestDTO requestDTO) {
        long incio = System.currentTimeMillis();

        SimulacaoRealizada simulacaoRealizada = SimulacaoRealizada.builder()
                .codigoProduto(produto.codigoProduto)
                .descricaoProduto(getDescricaoProduto(produto))
                .taxaJuros(produto.percentualTaxaJuros)
                .valorDesejado(requestDTO.getValorDesejado())
                .prazo(requestDTO.getPrazo())
                .valorTotalSAC(CalculadoraUtil.calcularValorTotalDasParcelas(resultados.get(0).getParcelas()))
                .valorTotalPRICE(CalculadoraUtil.calcularValorTotalDasParcelas(resultados.get(1).getParcelas()))
                .dataSimulacao(new java.sql.Date(System.currentTimeMillis()))
                .build();

        try {
            SimulacaoRealizada simulacaoPersistida = simulacaoRealizadaDao.salvar(simulacaoRealizada);

            // Invalida o cache após salvar nova simulação
            simulacaoCacheService.invalidateAll();

            long tempoExecucao = System.currentTimeMillis() - incio;
            log.info("[PASSO 4][PERSISTIR SIMULACAO] - Persistiu simulacao em {}ms", tempoExecucao);

            return simulacaoPersistida;

        } catch (Exception e) {
            log.error("[ERRO][TRANSACIONAL][BANCO DE DADOS] - Erro ao persistir simulação: {}", simulacaoRealizada.toString());
            throw new APIEmprestimoAgoraException("[ERRO][TRANSACIONAL][BANCO DE DADOS] - Erro ao persistir simulação: {}", e);
        }
    }

    private SimulacaoResponseDTO montaSimulacaoResponseDTO(SimulacaoRealizada simulacaoRealizada, List<SimulacaoPorSistemaDTO> resultados) {
        return SimulacaoResponseDTO.builder()
                .idSimulacao(simulacaoRealizada.getIdSimulacao())
                .codigoProduto(simulacaoRealizada.getCodigoProduto())
                .descricaoProduto(simulacaoRealizada.getDescricaoProduto())
                .taxaJuros(simulacaoRealizada.getTaxaJuros())
                .resultadosSimulacao(resultados)
                .build();
    }

    private String getDescricaoProduto(Produto produto) {
        return "Produto " + produto.codigoProduto;
    }

    private Produto buscarProdutoCompativel(BigDecimal valorDesejado, Short prazo) {
        long inicio = System.currentTimeMillis();

        List<Produto> produtos = produtoCacheService.findAllProdutos();

        Produto produtoCompativel = produtos.stream()
                .filter(produto -> valorDesejado.compareTo(produto.getValorMinimo()) >= 0 &&
                        (produto.getValorMaximo() == null || valorDesejado.compareTo(produto.getValorMaximo()) <= 0))
                .filter(produto -> prazo >= produto.getNumeroMinimoMeses() &&
                        (produto.getNumeroMaximoMeses() == null || prazo <= produto.getNumeroMaximoMeses()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nenhum produto disponivel para o valor " + valorDesejado + " e prazo " + prazo));

        long tempoExecucao = System.currentTimeMillis() - inicio;
        log.info("[PASSO 2][BUSCAR PRODUTO COMPATIVEL] - Buscou produto compativel em {}ms", tempoExecucao);

        return produtoCompativel;
    }

    /**
     * Calcula as simulações SAC e PRICE
     */
    private List<SimulacaoPorSistemaDTO> calcularSimulacoes(BigDecimal valorDesejado, Short prazo, BigDecimal taxaJuros) {
        long inicio = System.currentTimeMillis();

        List<SimulacaoPorSistemaDTO> resultados = new ArrayList<>();

        List<ParcelaDTO> parcelasSAC = CalculadoraUtil.calcularSAC(valorDesejado, taxaJuros, prazo);
        resultados.add(new SimulacaoPorSistemaDTO(TipoSimulacao.SAC, parcelasSAC));

        List<ParcelaDTO> parcelasPRICE = CalculadoraUtil.calcularPRICE(valorDesejado, taxaJuros, prazo);
        resultados.add(new SimulacaoPorSistemaDTO(TipoSimulacao.PRICE, parcelasPRICE));

        long tempoExecucao = System.currentTimeMillis() - inicio;
        log.info("[PASSO 3][CALCULO SIMULACOES] - Calculou simulacoes em {}ms", tempoExecucao);

        return resultados;
    }

    public ListagemSimulacoesResponseDTO buscaPaginada(Integer pagina, Integer tamanhoPagina, TipoSimulacao tipoEmprestimo) {
        requestListagemValidador.validaRequestListagem(pagina, tamanhoPagina, tipoEmprestimo);

        return buscarSimulacoesPaginada(pagina, tamanhoPagina, tipoEmprestimo.name());
    }

    private ListagemSimulacoesResponseDTO buscarSimulacoesPaginada(Integer pagina, Integer tamanhoPagina, String tipoEmprestimo) {
        TipoSimulacao tipo = TipoSimulacao.valueOf(tipoEmprestimo);
        
        // Tenta buscar no cache primeiro
        ListagemSimulacoesResponseDTO resultadoCache = simulacaoCacheService.get(pagina, tamanhoPagina, tipo);
        if (resultadoCache != null) {
            log.info("[CACHE HIT] - Resultado encontrado no cache para página {} tipo {}", pagina, tipo);
            return resultadoCache;
        }
        
        long inicio = System.currentTimeMillis();
        ListagemSimulacoesResponseDTO resultados;
        
        // Busca no banco de dados
        if (TipoSimulacao.SAC.name().equals(tipoEmprestimo)) {
            resultados = simulacaoRealizadaDao.buscaPaginada(pagina, tamanhoPagina, TipoSimulacao.SAC);
        } else if (TipoSimulacao.PRICE.name().equals(tipoEmprestimo)) {
            resultados = simulacaoRealizadaDao.buscaPaginada(pagina, tamanhoPagina, TipoSimulacao.PRICE);
        } else {
            resultados = new ListagemSimulacoesResponseDTO();
        }

        long tempoExecucao = System.currentTimeMillis() - inicio;
        log.info("[BANCO] - Buscou simulacoes paginadas em {}ms", tempoExecucao);
        
        // Armazena resultado no cache
        simulacaoCacheService.put(pagina, tamanhoPagina, tipo, resultados);
        log.info("[CACHE PUT] - Resultado armazenado no cache para página {} tipo {}", pagina, tipo);
        
        return resultados;
    }

    public VolumeDiarioResponseDTO buscarVolumePorProdutoPorDia(LocalDate data, Integer codigoProduto, TipoSimulacao tipoSimulacao) {
        List<SimulacaoRealizada> simulacoesPorDiaProduto = simulacaoRealizadaDao.findVolumePorProdutoPorDia(data, codigoProduto);
    
        if (simulacoesPorDiaProduto.isEmpty()) {
            return VolumeDiarioResponseDTO.builder()
                .dataReferencia(data)
                .simulacoes(List.of())
                .build();
        }

        BigDecimal taxaMediaJuros = calculaTaxaMediaJuros(simulacoesPorDiaProduto);
        BigDecimal valorMedioPrestacao = calculaValorMedioPrestacao(simulacoesPorDiaProduto, tipoSimulacao);
        BigDecimal valorTotalDesejado = calculaTotalDesejado(simulacoesPorDiaProduto);
        BigDecimal valorTotalCredito = calculaValorTotalCredito(simulacoesPorDiaProduto, tipoSimulacao);

        SimulacaoRealizada primeiraSimulacao = simulacoesPorDiaProduto.get(0);

        VolumeSimuladoDTO volumeSimulado = VolumeSimuladoDTO.builder()
            .codigoProduto(primeiraSimulacao.getCodigoProduto())
            .descricaoProduto(primeiraSimulacao.getDescricaoProduto())
            .taxaMediaJuros(taxaMediaJuros)
            .valorMedioPrestacao(valorMedioPrestacao)
            .valorTotalDesejado(valorTotalDesejado)
            .valorTotalCredito(valorTotalCredito)
            .build();

        return VolumeDiarioResponseDTO.builder()
            .dataReferencia(data)
            .simulacoes(List.of(volumeSimulado))
            .build();
    }

    private BigDecimal calculaTaxaMediaJuros(List<SimulacaoRealizada> simulacoes) {
        BigDecimal resultado = simulacoes.stream()
                .map(SimulacaoRealizada::getTaxaJuros)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(simulacoes.size()), 9, RoundingMode.HALF_UP);
        
        log.info("Taxa média calculada: {}", resultado.toPlainString());
        return resultado;
    }

    private BigDecimal calculaValorMedioPrestacao(List<SimulacaoRealizada> simulacoes, TipoSimulacao tipoSimulacao) {
        if (simulacoes == null || simulacoes.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal somaValoresMediosPrestacao = simulacoes.stream()
            .map(simulacao -> {
                BigDecimal valorTotal = tipoSimulacao == TipoSimulacao.SAC 
                    ? simulacao.getValorTotalSAC() 
                    : simulacao.getValorTotalPRICE();
                
                return valorTotal.divide(new BigDecimal(simulacao.getPrazo()), 2, RoundingMode.HALF_UP);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return somaValoresMediosPrestacao.divide(
            new BigDecimal(simulacoes.size()), 
            2, 
            RoundingMode.HALF_UP
        );
    }

    private BigDecimal calculaValorTotalCredito(List<SimulacaoRealizada> simulacoes, TipoSimulacao tipoSimulacao) {
        if (simulacoes == null || simulacoes.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return simulacoes.stream()
            .map(simulacao -> {
                return tipoSimulacao == TipoSimulacao.SAC 
                    ? simulacao.getValorTotalSAC() 
                    : simulacao.getValorTotalPRICE();
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculaTotalDesejado(List<SimulacaoRealizada> simulacoes) {
        return simulacoes.stream()
            .map(SimulacaoRealizada::getValorDesejado)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}