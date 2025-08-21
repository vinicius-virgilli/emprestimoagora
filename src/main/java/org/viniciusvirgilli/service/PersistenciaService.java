package org.viniciusvirgilli.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.viniciusvirgilli.dto.*;
import org.viniciusvirgilli.entity.SimulacaoRealizada;
import org.viniciusvirgilli.enums.TipoSimulacao;
import org.viniciusvirgilli.repository.SimulacaoRealizadaRepository;
import org.viniciusvirgilli.util.CalculadoraUtil;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;

import java.math.BigDecimal;
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
                calcularValorTotalParcelas(response, TipoSimulacao.SAC),
                calcularValorTotalParcelas(response, TipoSimulacao.PRICE),
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
     * Busca volume por período
     */
    public VolumePeriodoResponseDTO buscarVolumePorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        try {
            List<Object[]> resultados = simulacaoRepository
                .findVolumePorProdutoPorPeriodo(dataInicio, dataFim);
            
            List<VolumeSimuladoDTO> volumes = resultados.stream()
                .map(this::converterParaVolumeSimulado)
                .toList();
            
            return new VolumePeriodoResponseDTO(dataInicio, dataFim, volumes);
            
        } catch (Exception e) {
            LOGGER.severe("Erro ao buscar volume por período: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar volume por período", e);
        }
    }

    /**
     * Busca dados de telemetria das simulações
     */
    public TelemetriaResponseDTO buscarTelemetria(LocalDate dataReferencia) {
        try {
            LOGGER.info("Iniciando busca de telemetria para data: " + dataReferencia);
            
            // Buscar estatísticas das simulações
            Object[] stats = simulacaoRepository.findEstatisticasPorDia(dataReferencia);
            
            LOGGER.info("Estatísticas retornadas: " + java.util.Arrays.toString(stats));
            
            // Verificar se stats não é null e tem pelo menos 5 elementos
            if (stats == null || stats.length < 5) {
                LOGGER.warning("Estatísticas inválidas ou insuficientes. Usando valores padrão.");
                stats = new Object[]{0L, 1.5, 0.8, 3.2, 100.0};
            }
            
            // Converter com segurança
            int qtdRequisicoes = stats[0] != null ? ((Number) stats[0]).intValue() : 0;
            int tempoMedio = stats[1] != null ? ((Number) stats[1]).intValue() : 1500; // 1.5s em ms
            int tempoMinimo = stats[2] != null ? ((Number) stats[2]).intValue() : 800; // 0.8s em ms
            int tempoMaximo = stats[3] != null ? ((Number) stats[3]).intValue() : 3200; // 3.2s em ms
            
            LOGGER.info(String.format("Valores convertidos: qtd=%d, medio=%d, min=%d, max=%d", 
                qtdRequisicoes, tempoMedio, tempoMinimo, tempoMaximo));
            
            TelemetriaEndpointDTO telemetriaSimulacao = new TelemetriaEndpointDTO(
                "POST /api/simulacao/processar",
                qtdRequisicoes,
                tempoMedio,
                tempoMinimo,
                tempoMaximo,
                java.math.BigDecimal.valueOf(0.95) // percentualSucesso
            );
            
            return new TelemetriaResponseDTO(
                dataReferencia,
                List.of(telemetriaSimulacao)
            );
            
        } catch (Exception e) {
            LOGGER.severe("Erro ao buscar telemetria: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar telemetria", e);
        }
    }

    /**
     * Calcula o valor total das parcelas SAC
     */
    private BigDecimal calcularValorTotalParcelas(SimulacaoResponseDTO response, TipoSimulacao tipo) {
        ResultadoSimulacaoDTO resultadoSimulacao = response.getResultadosSimulacao().stream()
            .filter(resultado -> resultado.getTipo() == tipo)
            .findFirst()
            .orElse(null);

        List<ParcelaDTO> parcelasSimulacao = resultadoSimulacao.getParcelas();

        BigDecimal valorTotalSimulacao = CalculadoraUtil.calcularValorTotalDasParcelas(parcelasSimulacao);

        return valorTotalSimulacao;
    }

    /**
     * Converte SimulacaoRealizada para SimulacaoResumoDTO
     */
    private SimulacaoResumoDTO converterParaResumo(SimulacaoRealizada simulacao) {
        return new SimulacaoResumoDTO(
            simulacao.getIdSimulacao(),
            simulacao.getValorDesejado(),
            simulacao.getPrazoMeses(),
            simulacao.getValorTotalSAC(),
            simulacao.getValorTotalPRICE()
        );
    }

    /**
     * Converte resultado da query para VolumeSimuladoDTO
     */
    private VolumeSimuladoDTO converterParaVolumeSimulado(Object[] resultado) {
        return new VolumeSimuladoDTO(
            ((Number) resultado[0]).intValue(), // codigoProduto
            (String) resultado[1], // descricaoProduto
            convertToBigDecimal(resultado[2]), // taxaMediaJuros
            convertToBigDecimal(resultado[3]), // valorMedioPrestacao
            convertToBigDecimal(resultado[4]), // valorTotalDesejado
            convertToBigDecimal(resultado[5]), // valorMedioPrestacaoSAC
            convertToBigDecimal(resultado[6]), // valorTotalCreditoSAC
            convertToBigDecimal(resultado[7]), // valorMedioPrestacaoPRICE
            convertToBigDecimal(resultado[8])  // valorTotalCreditoPRICE
        );
    }
    
    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
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