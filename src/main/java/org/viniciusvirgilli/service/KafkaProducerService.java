package org.viniciusvirgilli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.viniciusvirgilli.dto.SimulacaoResponseDTO;
import org.viniciusvirgilli.exceptions.APIEmprestimoAgoraException;

/**
 * Serviço responsável por enviar simulações para o tópico Kafka
 */
@ApplicationScoped
@Slf4j
public class KafkaProducerService {

    @Inject
    @Channel("simulacoes-out")
    Emitter<String> simulacoesEmitter;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "kafka.retry.attempts", defaultValue = "3")
    int retryAttempts;

    @ConfigProperty(name = "kafka.retry.delay", defaultValue = "5000")
    long retryDelay;

    /**
     * Envia simulação para o tópico Kafka
     */
    public void enviarSimulacao(SimulacaoResponseDTO simulacao) {
        try {
            String simulacaoJson = objectMapper.writeValueAsString(simulacao);
            
            simulacoesEmitter.send(simulacaoJson)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("[KAFKA PRODUCER][ERRO] - Falha ao enviar simulacao ID: {} para Kafka", 
                                simulacao.getIdSimulacao(), throwable);
                        throw new APIEmprestimoAgoraException("Erro ao enviar para Kafka", throwable);
                    } else {
                        log.info("[KAFKA PRODUCER][SUCESSO] - Simulacao ID: {} enviada para Kafka", 
                                simulacao.getIdSimulacao());
                    }
                });
                
        } catch (Exception e) {
            log.error("[KAFKA PRODUCER][ERRO] - Erro ao serializar simulacao ID: {}", 
                    simulacao.getIdSimulacao(), e);
            throw new APIEmprestimoAgoraException("Erro ao serializar simulação", e);
        }
    }

    /**
     * Health check para verificar se o producer está funcionando
     */
    public boolean isHealthy() {
        return simulacoesEmitter != null;
    }
}