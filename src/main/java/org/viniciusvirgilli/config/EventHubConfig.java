package org.viniciusvirgilli.config;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class EventHubConfig {

    @ConfigProperty(name = "eventhub.connection.string")
    String connectionString;

    @ConfigProperty(name = "eventhub.enabled", defaultValue = "true")
    boolean eventHubEnabled;

    @Produces
    @ApplicationScoped
    public EventHubProducerClient eventHubProducerClient() {
        if (!eventHubEnabled) {
            // Retorna um cliente mock ou null quando desabilitado
            return null;
        }
        
        return new EventHubClientBuilder()
                .connectionString(connectionString)
                .buildProducerClient();
    }
}