package org.viniciusvirgilli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.viniciusvirgilli.dto.SimulacaoResponseDTO;
import org.viniciusvirgilli.exceptions.APIEmprestimoAgoraException;

/**
 * Serviço responsável por consumir simulações do Kafka e enviá-las para o Event Hub
 */
@ApplicationScoped
@Slf4j
public class KafkaConsumerService {

    @Inject
    EventHubService eventHubService;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "kafka.consumer.retry.attempts", defaultValue = "5")
    int maxRetryAttempts;

    @ConfigProperty(name = "kafka.consumer.retry.delay", defaultValue = "10000")
    long retryDelayMs;

    @ConfigProperty(name = "kafka.consumer.enabled", defaultValue = "true")
    boolean consumerEnabled;

    /**
     * Consome mensagens do tópico Kafka e envia para o Event Hub
     */
    @Incoming("simulacoes-in")
    @Blocking
    public void processarSimulacao(String simulacaoJson) {
        if (!consumerEnabled) {
            log.warn("[KAFKA CONSUMER] - Consumer desabilitado, ignorando mensagem");
            return;
        }

        try {
            SimulacaoResponseDTO simulacao = objectMapper.readValue(simulacaoJson, SimulacaoResponseDTO.class);
            
            log.info("[KAFKA CONSUMER] - Recebida simulacao ID: {} do Kafka", simulacao.getIdSimulacao());
            
            long startTime = System.currentTimeMillis();
            
            enviarParaEventHubComRetry(simulacao);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("[KAFKA CONSUMER][SUCESSO] - Simulacao ID: {} enviada para Event Hub em {}ms", 
                    simulacao.getIdSimulacao(), duration);
                    
        } catch (Exception e) {
            log.error("[KAFKA CONSUMER][ERRO] - Falha ao processar mensagem JSON: {}", simulacaoJson, e);
        }
    }

    private void enviarParaEventHubComRetry(SimulacaoResponseDTO simulacao) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                eventHubService.enviarEventoSimulacao(simulacao);
                
                if (attempt > 1) {
                    log.info("[KAFKA CONSUMER][RETRY][SUCESSO] - Simulacao ID: {} enviada na tentativa {}", 
                            simulacao.getIdSimulacao(), attempt);
                }
                
                return; // Sucesso
                
            } catch (Exception e) {
                lastException = e;
                
                if (attempt < maxRetryAttempts) {
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new APIEmprestimoAgoraException("Thread interrompida", ie);
                    }
                }
            }
        }
        
        log.error("[KAFKA CONSUMER][DLQ] - Simulacao ID: {} enviada para DLQ após {} tentativas", 
                simulacao.getIdSimulacao(), maxRetryAttempts);
    }
}