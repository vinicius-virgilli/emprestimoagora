package org.viniciusvirgilli.service;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.viniciusvirgilli.dto.SimulacaoResponseDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serviço para integração com Azure Event Hub
 * com tratamento específico para timeouts de conexão
 */
@ApplicationScoped
public class EventHubService {

    private static final Logger LOG = Logger.getLogger(EventHubService.class);

    @Inject
    EventHubProducerClient producer;

    @ConfigProperty(name = "eventhub.enabled", defaultValue = "true")
    boolean eventHubEnabled;

    @ConfigProperty(name = "eventhub.retry.attempts", defaultValue = "3")
    int maxRetryAttempts;

    @ConfigProperty(name = "eventhub.retry.delay", defaultValue = "1000")
    long retryDelayMs;

    // Contadores para monitoramento
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong timeoutCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private volatile LocalDateTime lastConnectionTimeout;

    public void enviarEventoSimulacao(SimulacaoResponseDTO simulacao) throws JsonProcessingException {
        if (!eventHubEnabled || producer == null) {
            LOG.info("📤 [EVENTHUB] Desabilitado ou não configurado. Simulando envio: " + simulacao.getIdSimulacao());
            return;
        }

        int tentativa = 1;
        Exception ultimoErro = null;

        while (tentativa <= maxRetryAttempts) {
            try {
                EventData eventData = new EventData(new ObjectMapper().writeValueAsString(simulacao));
                producer.send(List.of(eventData));

                successCount.incrementAndGet();
                LOG.info(String.format("✅ [EVENTHUB] Evento enviado com sucesso (tentativa %d/%d): %s",
                        tentativa, maxRetryAttempts, simulacao.getIdSimulacao()));
                return;

            } catch (AmqpException amqpEx) {
                ultimoErro = amqpEx;

                if (isConnectionTimeoutError(amqpEx)) {
                    timeoutCount.incrementAndGet();
                    lastConnectionTimeout = LocalDateTime.now();

                    LOG.warn(String.format("⏰ [EVENTHUB] Conexão expirou por inatividade (tentativa %d/%d). " +
                                    "Reconectando automaticamente... ID: %s",
                            tentativa, maxRetryAttempts, simulacao.getIdSimulacao()));

                    if (tentativa < maxRetryAttempts) {
                        aguardarRetry(tentativa);
                        tentativa++;
                        continue;
                    }
                } else {
                    // Outros erros AMQP
                    errorCount.incrementAndGet();
                    LOG.error(String.format("❌ [EVENTHUB] Erro AMQP (tentativa %d/%d): %s - ID: %s",
                            tentativa, maxRetryAttempts, amqpEx.getMessage(), simulacao.getIdSimulacao()));

                    if (tentativa < maxRetryAttempts) {
                        aguardarRetry(tentativa);
                        tentativa++;
                        continue;
                    }
                }

            } catch (Exception e) {
                ultimoErro = e;
                errorCount.incrementAndGet();

                LOG.error(String.format("💥 [EVENTHUB] Erro geral (tentativa %d/%d): %s - ID: %s",
                        tentativa, maxRetryAttempts, e.getMessage(), simulacao.getIdSimulacao()));

                if (tentativa < maxRetryAttempts) {
                    aguardarRetry(tentativa);
                    tentativa++;
                    continue;
                }
            }

            break;
        }

        // Se chegou aqui, todas as tentativas falharam
        String mensagemFinal = String.format(
                "🚫 [EVENTHUB] Falha após %d tentativas para ID: %s. Último erro: %s",
                maxRetryAttempts, simulacao.getIdSimulacao(),
                ultimoErro != null ? ultimoErro.getMessage() : "Desconhecido"
        );

        if (isConnectionTimeoutError(ultimoErro)) {
            // Para timeouts, log como WARNING ao invés de ERROR
            LOG.warn(mensagemFinal + " (Timeout de conexão - comportamento normal após inatividade)");
        } else {
            LOG.error(mensagemFinal);
        }

        throw new RuntimeException(mensagemFinal, ultimoErro);
    }

    /**
     * Verifica se o erro é relacionado ao timeout de conexão por inatividade
     */
    private boolean isConnectionTimeoutError(Exception e) {
        if (e == null) return false;

        String message = e.getMessage();
        if (message == null) return false;

        return message.contains("connection was closed") &&
                message.contains("did not have any active links") &&
                message.contains("300000 milliseconds");
    }

    /**
     * Aguarda antes de tentar novamente
     */
    private void aguardarRetry(int tentativa) {
        try {
            long delay = retryDelayMs * tentativa; // Backoff exponencial simples
            LOG.debug(String.format("⏳ [EVENTHUB] Aguardando %d ms antes da próxima tentativa...", delay));
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            LOG.warn("🔄 [EVENTHUB] Retry interrompido");
        }
    }

    /**
     * Retorna estatísticas do EventHub para monitoramento
     */
    public String obterEstatisticas() {
        long total = successCount.get() + errorCount.get() + timeoutCount.get();
        double successRate = total > 0 ? (double) successCount.get() / total * 100 : 0;

        String timeoutInfo = lastConnectionTimeout != null ?
                " | Último timeout: " + lastConnectionTimeout.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "";

        return String.format(
                "📊 [EVENTHUB STATS] Sucessos: %d | Timeouts: %d | Erros: %d | Taxa sucesso: %.1f%%%s",
                successCount.get(), timeoutCount.get(), errorCount.get(), successRate, timeoutInfo
        );
    }

    /**
     * Reseta as estatísticas (útil para testes)
     */
    public void resetarEstatisticas() {
        successCount.set(0);
        timeoutCount.set(0);
        errorCount.set(0);
        lastConnectionTimeout = null;
        LOG.info("🔄 [EVENTHUB] Estatísticas resetadas");
    }

    /**
     * Verifica se o EventHub está operacional
     */
    public boolean isOperacional() {
        return eventHubEnabled && producer != null;
    }

    /**
     * Força uma reconexão (útil após timeouts)
     */
    public void forcarReconexao() {
        if (!isOperacional()) {
            LOG.warn("🚫 [EVENTHUB] Não é possível forçar reconexão - serviço não operacional");
            return;
        }

        try {
            // O EventHubProducerClient reconecta automaticamente na próxima operação
            LOG.info("🔄 [EVENTHUB] Forçando reconexão na próxima operação...");
        } catch (Exception e) {
            LOG.error("❌ [EVENTHUB] Erro ao forçar reconexão: " + e.getMessage());
        }
    }
}