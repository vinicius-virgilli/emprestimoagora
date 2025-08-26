package org.viniciusvirgilli.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;

import java.time.LocalDate;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
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

    // Contadores internos para métricas
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successRequests = new AtomicLong(0);
    private final AtomicLong errorRequests = new AtomicLong(0);
    private final AtomicReference<Double> totalDuration = new AtomicReference<>(0.0);
    private final AtomicReference<Double> minDuration = new AtomicReference<>(Double.MAX_VALUE);
    private final AtomicReference<Double> maxDuration = new AtomicReference<>(0.0);

    // Attribute keys para buscar métricas
    private static final AttributeKey<String> ENDPOINT_KEY = AttributeKey.stringKey("endpoint");
    private static final AttributeKey<String> METHOD_KEY = AttributeKey.stringKey("method");
    private static final AttributeKey<String> STATUS_KEY = AttributeKey.stringKey("status");

    /**
     * Registra uma nova requisição com suas métricas
     */
    public void registrarRequisicao(String status, double durationSeconds) {
        totalRequests.incrementAndGet();
        
        if ("200".equals(status)) {
            successRequests.incrementAndGet();
        } else {
            errorRequests.incrementAndGet();
        }
        
        // Atualizar duração total
        totalDuration.updateAndGet(current -> current + durationSeconds);
        
        // Atualizar duração mínima
        minDuration.updateAndGet(current -> Math.min(current, durationSeconds));
        
        // Atualizar duração máxima
        maxDuration.updateAndGet(current -> Math.max(current, durationSeconds));
        
        LOGGER.info(String.format("Métrica registrada - Status: %s, Duração: %.3fs, Total: %d", 
                status, durationSeconds, totalRequests.get()));
    }

    /**
     * Coleta métricas do endpoint POST /api/simulacao/processar
     * 
     * @return array com [qtdRequisicoes, tempoMedio, tempoMinimo, tempoMaximo, percentualSucesso]
     */
    public Object[] coletarMetricasSimulacao() {
        try {
            long qtdRequisicoes = totalRequests.get();
            long qtdSucesso = successRequests.get();
            
            // Calcular métricas
            double tempoMedio = 0.0;
            double tempoMinimo = 0.0;
            double tempoMaximo = 0.0;
            double percentualSucesso = 0.0;
            
            if (qtdRequisicoes > 0) {
                // Converter de segundos para milissegundos
                tempoMedio = (totalDuration.get() / qtdRequisicoes) * 1000;
                tempoMinimo = minDuration.get() == Double.MAX_VALUE ? 0.0 : minDuration.get() * 1000;
                tempoMaximo = maxDuration.get() * 1000;
                percentualSucesso = (double) qtdSucesso / qtdRequisicoes;
            } else {
                // Valores padrão quando não há requisições
                tempoMedio = 0.0;
                tempoMinimo = 0.0;
                tempoMaximo = 0.0;
                percentualSucesso = 0.0;
            }
            
            LOGGER.info(String.format("Métricas coletadas - Requisições: %d, Sucesso: %d, Tempo médio: %.2fms", 
                    qtdRequisicoes, qtdSucesso, tempoMedio));
            
            return new Object[]{
                qtdRequisicoes,
                tempoMedio,
                tempoMinimo,
                tempoMaximo,
                percentualSucesso
            };
            
        } catch (Exception e) {
            LOGGER.warning("Erro ao coletar métricas: " + e.getMessage());
            
            // Retornar valores padrão em caso de erro
            return new Object[]{0L, 0.0, 0.0, 0.0, 0.0};
        }
    }

    /**
     * Coleta métricas e retorna no formato TelemetriaResponseDTO
     */
    public TelemetriaResponseDTO coletarMetricas() {
        Object[] metricas = coletarMetricasSimulacao();
        
        long qtdRequisicoes = (Long) metricas[0];
        double tempoMedio = (Double) metricas[1];
        double tempoMinimo = (Double) metricas[2];
        double tempoMaximo = (Double) metricas[3];
        double percentualSucesso = (Double) metricas[4];
        
        TelemetriaEndpointDTO endpoint = new TelemetriaEndpointDTO(
            "POST /api/simulacao/processar",
            (int) qtdRequisicoes,
            (int) Math.round(tempoMedio),
            (int) Math.round(tempoMinimo),
            (int) Math.round(tempoMaximo),
            BigDecimal.valueOf(percentualSucesso)
        );
        
        List<TelemetriaEndpointDTO> endpoints = new ArrayList<>();
        endpoints.add(endpoint);
        
        TelemetriaResponseDTO response = new TelemetriaResponseDTO();
        response.setDataReferencia(LocalDate.now());
        response.setListaEndpoints(endpoints);
        
        return response;
    }

    /**
     * Verifica se há métricas disponíveis
     */
    public boolean temMetricasDisponiveis() {
        return hasMetricas();
    }
    
    /**
     * Zera todas as métricas do endpoint de simulação
     */
    public void zerarMetricas() {
        try {
            totalRequests.set(0);
            successRequests.set(0);
            errorRequests.set(0);
            totalDuration.set(0.0);
            minDuration.set(Double.MAX_VALUE);
            maxDuration.set(0.0);
            
            LOGGER.info("Métricas zeradas com sucesso");
            
        } catch (Exception e) {
            LOGGER.warning("Erro ao zerar métricas: " + e.getMessage());
        }
    }

    /**
     * Verifica se há métricas disponíveis
     */
    public boolean hasMetricas() {
        return totalRequests.get() > 0;
    }
}