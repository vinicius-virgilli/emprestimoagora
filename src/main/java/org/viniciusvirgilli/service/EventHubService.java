package org.viniciusvirgilli.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.viniciusvirgilli.dto.SimulacaoResponseDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;

/**
 * Serviço para integração com Azure Event Hub
 */
@ApplicationScoped
public class EventHubService {

    @Inject
    EventHubProducerClient producerClient;

    private static final Logger LOGGER = Logger.getLogger(EventHubService.class.getName());
    
    private final ObjectMapper objectMapper;

    @ConfigProperty(name = "eventhub.connection.string")
    String eventHubConnectionString;

    @ConfigProperty(name = "eventhub.name", defaultValue = "simulacao-eventos")
    String eventHubName;

    @ConfigProperty(name = "eventhub.enabled", defaultValue = "false")
    boolean eventHubEnabled;

    public EventHubService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    /**
     * Envia evento de simulação para o Event Hub de forma assíncrona
     * 
     * @param simulacaoResponse dados da simulação processada
     */
    public void enviarEventoSimulacao(SimulacaoResponseDTO simulacaoResponse) {
        if (!eventHubEnabled) {
            LOGGER.info("Event Hub desabilitado. Evento não será enviado.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                enviarEvento(simulacaoResponse);
                LOGGER.info("Evento enviado com sucesso para EventHub. ID Simulação: " + simulacaoResponse.getIdSimulacao());
            } catch (Exception e) {
                LOGGER.severe("ERRO ao enviar evento para EventHub - ID Simulação: " +
                    simulacaoResponse.getIdSimulacao() + ", Erro: " + e.getMessage());
            }
        });
    }

    private void enviarEvento(SimulacaoResponseDTO simulacaoResponse) throws Exception {
        if (eventHubConnectionString.isEmpty()) {
            throw new IllegalStateException("Connection string do Event Hub não configurada");
        }

        String eventoJson = objectMapper.writeValueAsString(simulacaoResponse);
        
        LOGGER.info("Enviando evento para Event Hub: " + eventHubName);
        LOGGER.info("Tamanho do evento: " + eventoJson.length() + " bytes");

        try {
            LOGGER.info("=== ENVIANDO PARA EVENTHUB ===");
            LOGGER.info("Connection String: " + eventHubConnectionString.substring(0, 50) + "...");
            LOGGER.info("Event Hub Name: " + eventHubName);
            LOGGER.info("Evento JSON: " + eventoJson);
            LOGGER.info("Tamanho: " + eventoJson.length() + " bytes");
            
            EventData eventData = new EventData(eventoJson);
            producerClient.send(List.of(eventData));
            
            LOGGER.info("Evento enviado com SUCESSO para EventHub: " + eventHubName);
            
        } catch (Exception e) {
            LOGGER.severe("Erro ao enviar evento para EventHub: " + e.getMessage());
            throw new RuntimeException("Falha no envio para EventHub", e);
        }
    }

    /**
     * Verifica se o Event Hub está configurado e disponível
     */
    public boolean isEventHubDisponivel() {
        return eventHubEnabled && !eventHubConnectionString.isEmpty();
    }

    /**
     * Obtém estatísticas do Event Hub (para telemetria)
     */
    public EventHubStats obterEstatisticas() {
        // TODO: Implementar coleta de estatísticas reais
        return new EventHubStats(
            isEventHubDisponivel(),
            0L, // eventos enviados
            0L, // eventos com erro
            0.0  // tempo médio de envio
        );
    }

    /**
     * Classe para estatísticas do Event Hub
     */
    public static class EventHubStats {
        public boolean disponivel;
        public Long eventosEnviados;
        public Long eventosComErro;
        public Double tempoMedioEnvio;

        public EventHubStats(boolean disponivel, Long eventosEnviados, Long eventosComErro, Double tempoMedioEnvio) {
            this.disponivel = disponivel;
            this.eventosEnviados = eventosEnviados;
            this.eventosComErro = eventosComErro;
            this.tempoMedioEnvio = tempoMedioEnvio;
        }
    }
}