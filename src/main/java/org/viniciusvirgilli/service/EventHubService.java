package org.viniciusvirgilli.service;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.viniciusvirgilli.dto.SimulacaoResponseDTO;
import org.viniciusvirgilli.exceptions.APIEmprestimoAgoraException;

import java.util.List;

/**
 * Serviço para integração com Azure Event Hub
 */
@ApplicationScoped
@Slf4j
public class EventHubService {

    @Inject
    EventHubProducerClient producer;

    // @ConfigProperty(name = "eventhub.enabled", defaultValue = "true")
    boolean eventHubEnabled = false;

    public void enviarEventoSimulacao(SimulacaoResponseDTO simulacao) {
        if (!eventHubEnabled || producer == null) {
            throw new APIEmprestimoAgoraException(
                    "Event Hub não está habilitado!, simulacao de ID: " + simulacao.getIdSimulacao() + " nao sera enviada ao eventhub!");
        }

        try {
            EventData eventData = new EventData(new ObjectMapper().writeValueAsString(simulacao));
            producer.send(List.of(eventData));

        } catch (Exception e) {
            throw new APIEmprestimoAgoraException("Erro ao enviar evento para Event Hub", e);
        }
    }
}