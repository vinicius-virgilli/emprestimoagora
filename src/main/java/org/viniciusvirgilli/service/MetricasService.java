package org.viniciusvirgilli.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.viniciusvirgilli.dao.MetricaEndpointDao;
import org.viniciusvirgilli.dto.TelemetriaResponseDTO;
import org.viniciusvirgilli.dto.TelemetriaEndpointDTO;
import org.viniciusvirgilli.model.local.MetricaEndpoint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Serviço responsável por coletar e processar métricas do OpenTelemetry
 * com persistência no banco de dados PostgreSQL local
 */
@Slf4j
@ApplicationScoped
public class MetricasService {

    @Inject
    OpenTelemetry openTelemetry;

    @Inject
    MetricaEndpointDao metricaEndpointDao;

    // Cache em memória para métricas do dia atual (performance)
    private final Map<String, EndpointMetrics> endpointMetricsCache = new ConcurrentHashMap<>();
    private LocalDate cacheDate = LocalDate.now();
    
    // Contador para requisições de simulação
    private final AtomicLong contadorSimulacao = new AtomicLong(0);
    private static final int LIMITE_REQUISICOES_SIMULACAO = 10;

    // Attribute keys para buscar métricas
    private static final AttributeKey<String> ENDPOINT_KEY = AttributeKey.stringKey("endpoint");
    private static final AttributeKey<String> METHOD_KEY = AttributeKey.stringKey("method");
    private static final AttributeKey<String> STATUS_KEY = AttributeKey.stringKey("status");

    /**
     * Classe interna para armazenar métricas em cache
     */
    private static class EndpointMetrics {
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong successRequests = new AtomicLong(0);
        private final AtomicLong errorRequests = new AtomicLong(0);
        private final AtomicReference<Double> totalDuration = new AtomicReference<>(0.0);
        private final AtomicReference<Double> minDuration = new AtomicReference<>(Double.MAX_VALUE);
        private final AtomicReference<Double> maxDuration = new AtomicReference<>(0.0);
    }

    /**
     * Registra uma requisição com duração e status code
     */
    public void registrarRequisicao(String endpoint, double durationSeconds, int statusCode) {
        // Converte para milissegundos
        double durationMs = durationSeconds * 1000;
        
        // Atualiza cache em memória
        atualizarCache(endpoint, durationMs, statusCode >= 200 && statusCode < 400);
        
        // Verifica se é endpoint de simulação e incrementa contador
        if ("/api/simulacao/processar".equals(endpoint)) {
            long contadorAtual = contadorSimulacao.incrementAndGet();
            log.debug("Requisição de simulação #{} registrada", contadorAtual);
            
            // Persiste métricas a cada 10 requisições de simulação
            if (contadorAtual % LIMITE_REQUISICOES_SIMULACAO == 0) {
                log.info("Limite de {} requisições de simulação atingido. Persistindo métricas no banco...", LIMITE_REQUISICOES_SIMULACAO);
                persistirCacheNoBanco();
            }
        }
        
        // Persiste no banco de dados ao final do dia ou quando necessário
        persistirMetricasSeNecessario();
    }

    /**
     * Registra uma requisição apenas com duração (assume sucesso)
     */
    public void registrarRequisicao(String endpoint, double durationSeconds) {
        registrarRequisicao(endpoint, durationSeconds, 200);
    }

    /**
     * Atualiza o cache em memória
     */
    private void atualizarCache(String endpoint, double durationMs, boolean isSuccess) {
        // Verifica se mudou o dia (limpa cache se necessário)
        LocalDate hoje = LocalDate.now();
        if (!hoje.equals(cacheDate)) {
            persistirCacheNoBanco();
            endpointMetricsCache.clear();
            cacheDate = hoje;
        }

        EndpointMetrics metrics = endpointMetricsCache.computeIfAbsent(endpoint, k -> new EndpointMetrics());
        
        metrics.totalRequests.incrementAndGet();
        if (isSuccess) {
            metrics.successRequests.incrementAndGet();
        } else {
            metrics.errorRequests.incrementAndGet();
        }
        
        // Atualiza durações
        metrics.totalDuration.updateAndGet(current -> current + durationMs);
        metrics.minDuration.updateAndGet(current -> Math.min(current, durationMs));
        metrics.maxDuration.updateAndGet(current -> Math.max(current, durationMs));
    }

    /**
     * Persiste o cache atual no banco de dados
     */
    private void persistirCacheNoBanco() {
        if (endpointMetricsCache.isEmpty()) {
            return;
        }

        try {
            for (Map.Entry<String, EndpointMetrics> entry : endpointMetricsCache.entrySet()) {
                String endpoint = entry.getKey();
                EndpointMetrics metrics = entry.getValue();
                
                if (metrics.totalRequests.get() > 0) {
                    salvarOuAtualizarMetrica(endpoint, cacheDate, metrics);
                }
            }
            log.info("Cache de métricas persistido no banco para a data: {}", cacheDate);
        } catch (Exception e) {
            log.error("Erro ao persistir cache de métricas no banco", e);
        }
    }

    /**
     * Salva ou atualiza uma métrica no banco de dados
     */
    private void salvarOuAtualizarMetrica(String endpoint, LocalDate data, EndpointMetrics metrics) {
        Optional<MetricaEndpoint> metricaExistente = metricaEndpointDao.buscarPorEndpointEData(endpoint, data);
        
        MetricaEndpoint metrica;
        if (metricaExistente.isPresent()) {
            // Atualiza métrica existente
            metrica = metricaExistente.get();
            metrica.setTotalRequisicoes(metrica.getTotalRequisicoes() + metrics.totalRequests.get());
            metrica.setRequisicoesSuccesso(metrica.getRequisicoesSuccesso() + metrics.successRequests.get());
            metrica.setRequisicoesErro(metrica.getRequisicoesErro() + metrics.errorRequests.get());
            
            BigDecimal novaDuracaoTotal = metrica.getDuracaoTotalMs().add(BigDecimal.valueOf(metrics.totalDuration.get()));
            metrica.setDuracaoTotalMs(novaDuracaoTotal);
            
            BigDecimal novaMinima = metrica.getDuracaoMinimaMs().min(BigDecimal.valueOf(metrics.minDuration.get()));
            metrica.setDuracaoMinimaMs(novaMinima);
            
            BigDecimal novaMaxima = metrica.getDuracaoMaximaMs().max(BigDecimal.valueOf(metrics.maxDuration.get()));
            metrica.setDuracaoMaximaMs(novaMaxima);
        } else {
            // Cria nova métrica
            metrica = MetricaEndpoint.builder()
                    .nomeEndpoint(endpoint)
                    .dataReferencia(data)
                    .totalRequisicoes(metrics.totalRequests.get())
                    .requisicoesSuccesso(metrics.successRequests.get())
                    .requisicoesErro(metrics.errorRequests.get())
                    .duracaoTotalMs(BigDecimal.valueOf(metrics.totalDuration.get()))
                    .duracaoMinimaMs(BigDecimal.valueOf(metrics.minDuration.get()))
                    .duracaoMaximaMs(BigDecimal.valueOf(metrics.maxDuration.get()))
                    .build();
        }
        
        metricaEndpointDao.salvar(metrica);
    }

    /**
     * Persiste métricas se necessário (chamado periodicamente)
     */
    private void persistirMetricasSeNecessario() {
        // Persiste a cada 100 requisições ou se mudou o dia
        LocalDate hoje = LocalDate.now();
        if (!hoje.equals(cacheDate)) {
            persistirCacheNoBanco();
            endpointMetricsCache.clear();
            cacheDate = hoje;
        }
    }

    /**
     * Coleta métricas para uma data específica
     */
    public TelemetriaResponseDTO coletarMetricas(LocalDate dataReferencia) {
        List<TelemetriaEndpointDTO> endpoints = new ArrayList<>();
        
        // Se é a data atual, persiste o cache primeiro
        if (dataReferencia.equals(LocalDate.now())) {
            persistirCacheNoBanco();
        }
        
        // Busca métricas do banco de dados
        List<MetricaEndpoint> metricas = metricaEndpointDao.buscarPorData(dataReferencia);
        
        for (MetricaEndpoint metrica : metricas) {
            if (metrica.getTotalRequisicoes() > 0) {
                TelemetriaEndpointDTO endpointDTO = criarTelemetriaEndpointDTO(metrica);
                endpoints.add(endpointDTO);
            }
        }
        
        TelemetriaResponseDTO response = new TelemetriaResponseDTO();
        response.setDataReferencia(dataReferencia);
        response.setListaEndpoints(endpoints);
        
        return response;
    }

    /**
     * Coleta métricas para a data atual (compatibilidade)
     */
    public TelemetriaResponseDTO coletarMetricas() {
        return coletarMetricas(LocalDate.now());
    }

    /**
     * Cria TelemetriaEndpointDTO a partir de MetricaEndpoint
     */
    private TelemetriaEndpointDTO criarTelemetriaEndpointDTO(MetricaEndpoint metrica) {
        BigDecimal tempoMedio = metrica.calcularTempoMedio();
        BigDecimal percentualSucesso = metrica.calcularPercentualSucesso();
        
        return new TelemetriaEndpointDTO(
            metrica.getNomeEndpoint(),
            metrica.getTotalRequisicoes().intValue(),
            tempoMedio.intValue(),
            metrica.getDuracaoMinimaMs().intValue(),
            metrica.getDuracaoMaximaMs().intValue(),
            percentualSucesso.divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP)
        );
    }

    /**
     * Força a persistência do cache atual
     */
    public void forcarPersistencia() {
        persistirCacheNoBanco();
    }

    /**
     * Limpa todas as métricas do banco de dados
     */
    public void zerarTodasMetricas() {
        endpointMetricsCache.clear();
        metricaEndpointDao.deletarTodas();
        log.info("Todas as métricas foram zeradas");
    }

    /**
     * Limpa métricas de uma data específica
     */
    public void zerarMetricasPorData(LocalDate data) {
        if (data.equals(LocalDate.now())) {
            endpointMetricsCache.clear();
        }
        metricaEndpointDao.deletarPorData(data);
        log.info("Métricas zeradas para a data: {}", data);
    }

    // Métodos de compatibilidade mantidos
    public Object[] coletarMetricasSimulacao() {
        return coletarMetricasEndpoint("/api/simulacao/processar");
    }

    public Object[] coletarMetricasListagem() {
        return coletarMetricasEndpoint("/api/simulacoes/paginada");
    }

    private Object[] coletarMetricasEndpoint(String endpoint) {
        LocalDate hoje = LocalDate.now();
        Optional<MetricaEndpoint> metrica = metricaEndpointDao.buscarPorEndpointEData(endpoint, hoje);
        
        if (metrica.isPresent()) {
            MetricaEndpoint m = metrica.get();
            BigDecimal tempoMedio = m.calcularTempoMedio();
            BigDecimal percentualSucesso = m.calcularPercentualSucesso();
            
            return new Object[]{
                m.getTotalRequisicoes(),
                tempoMedio.doubleValue(),
                m.getDuracaoMinimaMs().doubleValue(),
                m.getDuracaoMaximaMs().doubleValue(),
                percentualSucesso.doubleValue()
            };
        }
        
        return new Object[]{0L, 0.0, 0.0, 0.0, 0.0};
    }

    public boolean temMetricasDisponiveis() {
        return !metricaEndpointDao.buscarTodas().isEmpty() || !endpointMetricsCache.isEmpty();
    }

    public boolean hasMetricas() {
        return temMetricasDisponiveis();
    }
}