package org.viniciusvirgilli.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.viniciusvirgilli.dto.SimulacaoResponseDTO;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
// Imports comentados temporariamente devido a problemas de compatibilidade

/**
 * Serviço para integração com Azure Event Hub
 * Simula o envio de eventos de simulação para integração com área de relacionamento
 */
@ApplicationScoped
public class EventHubService {

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

        // Envio assíncrono para EventHub real
        CompletableFuture.runAsync(() -> {
            try {
                enviarEventoReal(simulacaoResponse);
                LOGGER.info("Evento enviado com sucesso para EventHub real. ID Simulação: " + simulacaoResponse.getIdSimulacao());
            } catch (Exception e) {
                // Log do erro mas não falha a operação principal
                LOGGER.severe("ERRO ao enviar evento para EventHub real - ID Simulação: " + 
                    simulacaoResponse.getIdSimulacao() + ", Erro: " + e.getMessage());
                // Não propaga a exceção para não afetar o fluxo principal
            }
        });
    }

    /**
     * Simula o envio do evento (para desenvolvimento/teste)
     */
    private void simularEnvioEvento(SimulacaoResponseDTO simulacaoResponse) {
        try {
            String eventoJson = objectMapper.writeValueAsString(criarEnvelopeEvento(simulacaoResponse));
            
            LOGGER.info("=== SIMULAÇÃO EVENT HUB ===");
            LOGGER.info("Event Hub: " + eventHubName);
            LOGGER.info("Evento JSON: " + eventoJson);
            LOGGER.info("=== FIM SIMULAÇÃO ===");
            
            // Simular delay de rede
            Thread.sleep(100);
            
        } catch (Exception e) {
            LOGGER.warning("Erro na simulação do Event Hub: " + e.getMessage());
        }
    }

    /**
     * Envia evento real para o Azure Event Hub
     */
    private void enviarEventoReal(SimulacaoResponseDTO simulacaoResponse) throws Exception {
        if (eventHubConnectionString.isEmpty()) {
            throw new IllegalStateException("Connection string do Event Hub não configurada");
        }

        String eventoJson = objectMapper.writeValueAsString(criarEnvelopeEvento(simulacaoResponse));
        
        LOGGER.info("Enviando evento para Event Hub real: " + eventHubName);
        LOGGER.info("Tamanho do evento: " + eventoJson.length() + " bytes");
        
        // Implementação real com Azure Event Hub SDK
        EventHubProducerClient producer = null;
        try {
            LOGGER.info("=== ENVIANDO PARA EVENTHUB REAL ===");
            LOGGER.info("Connection String: " + eventHubConnectionString.substring(0, 50) + "...");
            LOGGER.info("Event Hub Name: " + eventHubName);
            LOGGER.info("Evento JSON: " + eventoJson);
            LOGGER.info("Tamanho: " + eventoJson.length() + " bytes");
            
            // Criar o cliente do Event Hub
            producer = new EventHubClientBuilder()
                .connectionString(eventHubConnectionString, eventHubName)
                .buildProducerClient();
            
            // Criar o evento
            EventData eventData = new EventData(eventoJson);
            
            // Enviar o evento
            producer.send(java.util.Arrays.asList(eventData));
            
            LOGGER.info("Evento enviado com SUCESSO para EventHub REAL: " + eventHubName);
            LOGGER.info("=== FIM ENVIO EVENTHUB REAL ===");
            
        } catch (Exception e) {
            LOGGER.severe("Erro ao enviar evento para EventHub real: " + e.getMessage());
            throw new RuntimeException("Falha no envio para EventHub", e);
        } finally {
            if (producer != null) {
                try {
                    producer.close();
                } catch (Exception e) {
                    LOGGER.warning("Erro ao fechar producer do EventHub: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Cria o envelope do evento com metadados
     */
    private EventoSimulacao criarEnvelopeEvento(SimulacaoResponseDTO simulacaoResponse) {
        return new EventoSimulacao(
            "SIMULACAO_PROCESSADA",
            System.currentTimeMillis(),
            "simulacao-service",
            "1.0",
            simulacaoResponse
        );
    }

    /**
     * Classe para representar o envelope do evento
     */
    public static class EventoSimulacao {
        public String tipoEvento;
        public Long timestamp;
        public String origem;
        public String versao;
        public SimulacaoResponseDTO dados;

        public EventoSimulacao() {}

        public EventoSimulacao(String tipoEvento, Long timestamp, String origem, String versao, SimulacaoResponseDTO dados) {
            this.tipoEvento = tipoEvento;
            this.timestamp = timestamp;
            this.origem = origem;
            this.versao = versao;
            this.dados = dados;
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

        public EventHubStats() {}

        public EventHubStats(boolean disponivel, Long eventosEnviados, Long eventosComErro, Double tempoMedioEnvio) {
            this.disponivel = disponivel;
            this.eventosEnviados = eventosEnviados;
            this.eventosComErro = eventosComErro;
            this.tempoMedioEnvio = tempoMedioEnvio;
        }
    }
}