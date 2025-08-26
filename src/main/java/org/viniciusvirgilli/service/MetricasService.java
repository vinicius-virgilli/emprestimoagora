package org.viniciusvirgilli.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;

import java.time.LocalDate;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.viniciusvirgilli.dto.TelemetriaResponseDTO;
import org.viniciusvirgilli.dto.TelemetriaEndpointDTO;
import java.math.BigDecimal;

/**
 * Serviço responsável por coletar e processar métricas do OpenTelemetry
 */
@ApplicationScoped
public class MetricasService {

    private static final Logger LOGGER = Logger.getLogger(MetricasService.class.getName());

    @Inject
    OpenTelemetry openTelemetry;

    // Estrutura para armazenar métricas por endpoint
    private final Map<String, EndpointMetrics> endpointMetrics = new ConcurrentHashMap<>();

    // Attribute keys para buscar métricas
    private static final AttributeKey<String> ENDPOINT_KEY = AttributeKey.stringKey("endpoint");
    private static final AttributeKey<String> METHOD_KEY = AttributeKey.stringKey("method");
    private static final AttributeKey<String> STATUS_KEY = AttributeKey.stringKey("status");

    /**
     * Classe interna para armazenar métricas de um endpoint específico
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
     * Registra uma nova requisição com suas métricas para um endpoint específico
     */
    public void registrarRequisicao(String endpoint, double durationSeconds) {
        String status = "200"; // Assumindo sucesso por padrão
        registrarRequisicao(endpoint, status, durationSeconds);
    }

    /**
     * Registra uma nova requisição com suas métricas para um endpoint específico
     */
    public void registrarRequisicao(String endpoint, String status, double durationSeconds) {
        EndpointMetrics metrics = endpointMetrics.computeIfAbsent(endpoint, k -> new EndpointMetrics());
        
        metrics.totalRequests.incrementAndGet();
        
        if ("200".equals(status)) {
            metrics.successRequests.incrementAndGet();
        } else {
            metrics.errorRequests.incrementAndGet();
        }
        
        // Atualizar duração total
        metrics.totalDuration.updateAndGet(current -> current + durationSeconds);
        
        // Atualizar duração mínima
        metrics.minDuration.updateAndGet(current -> Math.min(current, durationSeconds));
        
        // Atualizar duração máxima
        metrics.maxDuration.updateAndGet(current -> Math.max(current, durationSeconds));
        
        LOGGER.info(String.format("Métrica registrada - Endpoint: %s, Status: %s, Duração: %.3fs, Total: %d", 
                endpoint, status, durationSeconds, metrics.totalRequests.get()));
    }

    /**
     * Coleta métricas de um endpoint específico
     * 
     * @param endpoint o endpoint para coletar métricas
     * @return array com [qtdRequisicoes, tempoMedio, tempoMinimo, tempoMaximo, percentualSucesso]
     */
    public Object[] coletarMetricasEndpoint(String endpoint) {
        EndpointMetrics metrics = endpointMetrics.get(endpoint);
        
        if (metrics == null) {
            return new Object[]{0L, 0.0, 0.0, 0.0, 0.0};
        }
        
        try {
            long qtdRequisicoes = metrics.totalRequests.get();
            long qtdSucesso = metrics.successRequests.get();
            
            // Calcular métricas
            double tempoMedio = 0.0;
            double tempoMinimo = 0.0;
            double tempoMaximo = 0.0;
            double percentualSucesso = 0.0;
            
            if (qtdRequisicoes > 0) {
                // Converter de segundos para milissegundos
                tempoMedio = (metrics.totalDuration.get() / qtdRequisicoes) * 1000;
                tempoMinimo = metrics.minDuration.get() == Double.MAX_VALUE ? 0.0 : metrics.minDuration.get() * 1000;
                tempoMaximo = metrics.maxDuration.get() * 1000;
                percentualSucesso = (double) qtdSucesso / qtdRequisicoes;
            }
            
            LOGGER.info(String.format("Métricas coletadas para %s - Requisições: %d, Sucesso: %d, Tempo médio: %.2fms", 
                    endpoint, qtdRequisicoes, qtdSucesso, tempoMedio));
            
            return new Object[]{
                qtdRequisicoes,
                tempoMedio,
                tempoMinimo,
                tempoMaximo,
                percentualSucesso
            };
            
        } catch (Exception e) {
            LOGGER.warning("Erro ao coletar métricas para " + endpoint + ": " + e.getMessage());
            return new Object[]{0L, 0.0, 0.0, 0.0, 0.0};
        }
    }

    /**
     * Coleta métricas do endpoint POST /api/simulacao/processar (compatibilidade)
     */
    public Object[] coletarMetricasSimulacao() {
        return coletarMetricasEndpoint("/api/simulacao/processar");
    }

    /**
     * Coleta métricas e retorna no formato TelemetriaResponseDTO para todos os endpoints
     */
    public TelemetriaResponseDTO coletarMetricas() {
        List<TelemetriaEndpointDTO> endpoints = new ArrayList<>();
        
        // Coletar métricas para o endpoint de simulação
        Object[] metricasSimulacao = coletarMetricasEndpoint("/api/simulacao/processar");
        if ((Long) metricasSimulacao[0] > 0) {
            TelemetriaEndpointDTO endpointSimulacao = criarTelemetriaEndpointDTO(
                "POST /api/simulacao/processar", metricasSimulacao);
            endpoints.add(endpointSimulacao);
        }
        
        // Coletar métricas para o endpoint de listagem
        Object[] metricasListagem = coletarMetricasEndpoint("/api/simulacoes/paginada");
        if ((Long) metricasListagem[0] > 0) {
            TelemetriaEndpointDTO endpointListagem = criarTelemetriaEndpointDTO(
                "GET /api/simulacoes/paginada", metricasListagem);
            endpoints.add(endpointListagem);
        }
        
        TelemetriaResponseDTO response = new TelemetriaResponseDTO();
        response.setDataReferencia(LocalDate.now());
        response.setListaEndpoints(endpoints);
        
        return response;
    }

    /**
     * Método auxiliar para criar TelemetriaEndpointDTO
     */
    private TelemetriaEndpointDTO criarTelemetriaEndpointDTO(String nomeApi, Object[] metricas) {
        long qtdRequisicoes = (Long) metricas[0];
        double tempoMedio = (Double) metricas[1];
        double tempoMinimo = (Double) metricas[2];
        double tempoMaximo = (Double) metricas[3];
        double percentualSucesso = (Double) metricas[4];
        
        return new TelemetriaEndpointDTO(
            nomeApi,
            (int) qtdRequisicoes,
            (int) Math.round(tempoMedio),
            (int) Math.round(tempoMinimo),
            (int) Math.round(tempoMaximo),
            BigDecimal.valueOf(percentualSucesso)
        );
    }

    /**
     * Verifica se há métricas disponíveis para qualquer endpoint
     */
    public boolean temMetricasDisponiveis() {
        return endpointMetrics.values().stream()
            .anyMatch(metrics -> metrics.totalRequests.get() > 0);
    }
    
    /**
     * Zera todas as métricas de todos os endpoints
     */
    public void zerarMetricas() {
        try {
            endpointMetrics.clear();
            LOGGER.info("Métricas zeradas com sucesso para todos os endpoints");
        } catch (Exception e) {
            LOGGER.warning("Erro ao zerar métricas: " + e.getMessage());
        }
    }

    /**
     * Verifica se há métricas disponíveis (compatibilidade)
     */
    public boolean hasMetricas() {
        return temMetricasDisponiveis();
    }
}