package org.viniciusvirgilli.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.viniciusvirgilli.dto.*;
import org.viniciusvirgilli.entity.SimulacaoRealizada;
import org.viniciusvirgilli.repository.SimulacaoRealizadaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Serviço para persistência de simulações no banco de dados local
 */
@ApplicationScoped
public class PersistenciaService {

    private static final Logger LOGGER = Logger.getLogger(PersistenciaService.class.getName());

    @Inject
    SimulacaoRealizadaRepository simulacaoRepository;

    /**
     * Persiste uma simulação realizada no banco local de forma assíncrona
     * 
     * @param request dados da requisição
     * @param response dados da resposta
     */
    public void persistirSimulacao(SimulacaoRequestDTO request, SimulacaoResponseDTO response) {
        CompletableFuture.runAsync(() -> {
            try {
                persistirSimulacaoSync(request, response);
            } catch (Exception e) {
                LOGGER.severe("Erro ao persistir simulação: " + e.getMessage());
                throw new RuntimeException("Falha na persistência", e);
            }
        });
    }

    /**
     * Persiste uma simulação de forma síncrona
     */
    @Transactional
    public void persistirSimulacaoSync(SimulacaoRequestDTO request, SimulacaoResponseDTO response) {
        try {
            SimulacaoRealizada simulacao = new SimulacaoRealizada(
                response.getIdSimulacao(),
                response.getCodigoProduto(),
                response.getDescricaoProduto(),
                request.getValorDesejado(),
                request.getPrazo(),
                response.getTaxaJuros(),
                calcularValorTotalSAC(response),
                calcularValorTotalPRICE(response),
                LocalDateTime.now()
            );

            simulacaoRepository.persist(simulacao);
            LOGGER.info("Simulação persistida com ID: " + simulacao.getId());
            
        } catch (Exception e) {
            LOGGER.severe("Erro ao persistir simulação no banco: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Busca todas as simulações realizadas com paginação
     */
    public ListagemSimulacoesResponseDTO buscarTodasSimulacoes(int pagina, int tamanhoPagina) {
        try {
            List<SimulacaoRealizada> simulacoes = simulacaoRepository
                .findAllPaginado(pagina, tamanhoPagina);
            
            long totalRegistros = simulacaoRepository.count();
            
            List<SimulacaoResumoDTO> resumos = simulacoes.stream()
                .map(this::converterParaResumo)
                .toList();
            
            return new ListagemSimulacoesResponseDTO(
                pagina,
                (int) totalRegistros,
                resumos.size(),
                resumos
            );
            
        } catch (Exception e) {
            LOGGER.severe("Erro ao buscar simulações: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar simulações", e);
        }
    }

    /**
     * Busca volume diário por produto
     */
    public VolumeDiarioResponseDTO buscarVolumeDiario(LocalDate dataReferencia) {
        try {
            List<Object[]> resultados = simulacaoRepository
                .findVolumePorProdutoPorDia(dataReferencia);
            
            List<VolumeSimuladoDTO> volumes = resultados.stream()
                .map(this::converterParaVolumeSimulado)
                .toList();
            
            return new VolumeDiarioResponseDTO(dataReferencia, volumes);
            
        } catch (Exception e) {
            LOGGER.severe("Erro ao buscar volume diário: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar volume diário", e);
        }
    }

    /**
     * Busca dados de telemetria das simulações
     */
    public TelemetriaResponseDTO buscarTelemetria(LocalDate dataReferencia) {
        try {
            // Buscar estatísticas das simulações
            Object[] stats = simulacaoRepository.findEstatisticasPorDia(dataReferencia);
            
            TelemetriaEndpointDTO telemetriaSimulacao = new TelemetriaEndpointDTO(
                "POST /api/simulacao/processar",
                stats[0] != null ? ((Number) stats[0]).intValue() : 0, // qtdRequisicoes
                stats[1] != null ? ((Number) stats[1]).intValue() : 0, // tempoMedio
                stats[2] != null ? ((Number) stats[2]).intValue() : 0, // tempoMinimo
                stats[3] != null ? ((Number) stats[3]).intValue() : 0, // tempoMaximo
                java.math.BigDecimal.valueOf(0.95) // percentualSucesso
            );
            
            return new TelemetriaResponseDTO(
                dataReferencia,
                List.of(telemetriaSimulacao)
            );
            
        } catch (Exception e) {
            LOGGER.severe("Erro ao buscar telemetria: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar telemetria", e);
        }
    }

    /**
     * Calcula o valor total das parcelas SAC
     */
    private java.math.BigDecimal calcularValorTotalSAC(SimulacaoResponseDTO response) {
        return response.getResultadosSimulacao().stream()
            .filter(r -> r.getTipo().name().equals("SAC"))
            .flatMap(r -> r.getParcelas().stream())
            .map(ParcelaDTO::getValorPrestacao)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    /**
     * Calcula o valor total das parcelas PRICE
     */
    private java.math.BigDecimal calcularValorTotalPRICE(SimulacaoResponseDTO response) {
        return response.getResultadosSimulacao().stream()
            .filter(r -> r.getTipo().name().equals("PRICE"))
            .flatMap(r -> r.getParcelas().stream())
            .map(ParcelaDTO::getValorPrestacao)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    /**
     * Converte SimulacaoRealizada para SimulacaoResumoDTO
     */
    private SimulacaoResumoDTO converterParaResumo(SimulacaoRealizada simulacao) {
        return new SimulacaoResumoDTO(
            simulacao.getIdSimulacao(),
            simulacao.getValorDesejado(),
            simulacao.getPrazoMeses(),
            simulacao.getValorTotalSAC() // Usando SAC como padrão para o resumo
        );
    }

    /**
     * Converte resultado da query para VolumeSimuladoDTO
     */
    private VolumeSimuladoDTO converterParaVolumeSimulado(Object[] resultado) {
        return new VolumeSimuladoDTO(
            ((Number) resultado[0]).intValue(), // codigoProduto
            (String) resultado[1], // descricaoProduto
            (java.math.BigDecimal) resultado[2], // taxaMediaJuro
            (java.math.BigDecimal) resultado[3], // valorMedioPrestacao
            (java.math.BigDecimal) resultado[4], // valorTotalDesejado
            (java.math.BigDecimal) resultado[5]  // valorTotalCredito
        );
    }

    /**
     * Busca simulação por ID
     */
    public SimulacaoRealizada buscarSimulacaoPorId(Long idSimulacao) {
        return simulacaoRepository.findByIdSimulacao(idSimulacao)
            .orElseThrow(() -> new IllegalArgumentException("Simulação não encontrada: " + idSimulacao));
    }

    /**
     * Remove simulações antigas (limpeza de dados)
     */
    @Transactional
    public int limparSimulacoesAntigas(LocalDateTime dataLimite) {
        return simulacaoRepository.deleteSimulacoesAntigas(dataLimite);
    }
}