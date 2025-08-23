package org.viniciusvirgilli.service;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.viniciusvirgilli.dto.SimulacaoResponseDTO;

import java.util.List;

/**
 * Serviço para integração com Azure Event Hub
 */
@ApplicationScoped
public class EventHubService {
    
    private static final Logger LOG = Logger.getLogger(EventHubService.class);
    
    @Inject
    EventHubProducerClient producer;
    
    @ConfigProperty(name = "eventhub.enabled", defaultValue = "true")
    boolean eventHubEnabled;

    public void enviarEventoSimulacao(SimulacaoResponseDTO simulacao) throws JsonProcessingException {
        if (!eventHubEnabled || producer == null) {
            LOG.info("Event Hub desabilitado ou não configurado. Simulando envio: " + simulacao.getIdSimulacao());
            return;
        }
        
        try {
            EventData eventData = new EventData(new ObjectMapper().writeValueAsString(simulacao));
            producer.send(List.of(eventData));
            LOG.info("Evento enviado com sucesso para o Event Hub: " + simulacao.getIdSimulacao());
        } catch (Exception e) {
            LOG.error("Erro ao enviar evento para o Event Hub: " + e.getMessage(), e);
            throw e;
        }
    }
}