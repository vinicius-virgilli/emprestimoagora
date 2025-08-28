package org.viniciusvirgilli.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serviço responsável pelo controle de rate limiting
 * Implementa throttling por IP e endpoint para proteger contra sobrecarga
 */
@Slf4j
@ApplicationScoped
public class RateLimitService {

    @ConfigProperty(name = "ratelimit.requests.per.second", defaultValue = "550")
    int requestsPerSecond;

    @ConfigProperty(name = "ratelimit.requests.per.minute", defaultValue = "60")
    int requestsPerMinute;

    @ConfigProperty(name = "ratelimit.requests.per.hour", defaultValue = "1000")
    int requestsPerHour;

    @ConfigProperty(name = "ratelimit.enabled", defaultValue = "true")
    boolean rateLimitEnabled;

    @ConfigProperty(name = "ratelimit.block.duration.minutes", defaultValue = "5")
    int blockDurationMinutes;

    // Cache em memória para contadores por IP
    private final ConcurrentHashMap<String, RateLimitCounter> ipCounters = new ConcurrentHashMap<>();
    
    // Cache em memória para IPs bloqueados
    private final ConcurrentHashMap<String, LocalDateTime> blockedIps = new ConcurrentHashMap<>();

    /**
     * Verifica se uma requisição deve ser permitida baseada no rate limiting
     * 
     * @param clientIp IP do cliente
     * @param endpoint Endpoint sendo acessado
     * @return true se a requisição deve ser permitida, false caso contrário
     */
    public boolean isRequestAllowed(String clientIp, String endpoint) {
        if (!rateLimitEnabled) {
            return true;
        }

        // Verifica se o IP está bloqueado
        if (isIpBlocked(clientIp)) {
            log.warn("[RATE LIMIT] IP {} esta bloqueado ate {}", clientIp, blockedIps.get(clientIp));
            return false;
        }

        // Limpa contadores expirados
        cleanExpiredCounters();

        // Obtém ou cria contador para o IP
        RateLimitCounter counter = ipCounters.computeIfAbsent(clientIp, k -> new RateLimitCounter());

        // Verifica limite por segundo
        if (counter.getRequestsInLastSecond() >= requestsPerSecond) {
            log.warn("[RATE LIMIT] IP {} excedeu limite de {} requisicoes por segundo", clientIp, requestsPerSecond);
            blockIp(clientIp);
            return false;
        }

        // Verifica limite por minuto
        if (counter.getRequestsInLastMinute() >= requestsPerMinute) {
            log.warn("[RATE LIMIT] IP {} excedeu limite de {} requisicoes por minuto", clientIp, requestsPerMinute);
            blockIp(clientIp);
            return false;
        }

        // Verifica limite por hora
        if (counter.getRequestsInLastHour() >= requestsPerHour) {
            log.warn("[RATE LIMIT] IP {} excedeu limite de {} requisicoes por hora", clientIp, requestsPerHour);
            blockIp(clientIp);
            return false;
        }

        // Incrementa contadores
        counter.incrementRequests();
        
        log.debug("[RATE LIMIT] IP {} - Requisicoes no ultimo segundo: {}, minuto: {}, hora: {}", 
                 clientIp, counter.getRequestsInLastSecond(), counter.getRequestsInLastMinute(), counter.getRequestsInLastHour());

        return true;
    }

    /**
     * Verifica se um IP está bloqueado
     */
    private boolean isIpBlocked(String clientIp) {
        LocalDateTime blockedUntil = blockedIps.get(clientIp);
        if (blockedUntil != null) {
            if (LocalDateTime.now().isBefore(blockedUntil)) {
                return true;
            } else {
                // Remove o bloqueio expirado
                blockedIps.remove(clientIp);
                ipCounters.remove(clientIp); // Reset contadores
            }
        }
        return false;
    }

    /**
     * Bloqueia um IP por um período determinado
     */
    private void blockIp(String clientIp) {
        LocalDateTime blockUntil = LocalDateTime.now().plusMinutes(blockDurationMinutes);
        blockedIps.put(clientIp, blockUntil);
        log.warn("[RATE LIMIT] IP {} bloqueado ate {}", clientIp, blockUntil);
    }

    /**
     * Remove contadores expirados para liberar memória
     */
    private void cleanExpiredCounters() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        ipCounters.entrySet().removeIf(entry -> 
            entry.getValue().getLastRequestTime().isBefore(oneHourAgo));
    }

    /**
     * Obtém estatísticas de rate limiting para um IP
     */
    public RateLimitStats getStats(String clientIp) {
        RateLimitCounter counter = ipCounters.get(clientIp);
        if (counter == null) {
            return new RateLimitStats(0, 0, 0, false, null);
        }

        LocalDateTime blockedUntil = blockedIps.get(clientIp);
        boolean isBlocked = blockedUntil != null && LocalDateTime.now().isBefore(blockedUntil);

        return new RateLimitStats(
            counter.getRequestsInLastSecond(),
            counter.getRequestsInLastMinute(),
            counter.getRequestsInLastHour(),
            isBlocked,
            blockedUntil
        );
    }

    /**
     * Classe interna para armazenar contadores de requisições
     */
    private static class RateLimitCounter {
        private final AtomicInteger requestsInLastSecond = new AtomicInteger(0);
        private final AtomicInteger requestsInLastMinute = new AtomicInteger(0);
        private final AtomicInteger requestsInLastHour = new AtomicInteger(0);
        private final AtomicLong lastRequestTime = new AtomicLong(System.currentTimeMillis());
        private final AtomicLong lastSecondReset = new AtomicLong(System.currentTimeMillis());
        private final AtomicLong lastMinuteReset = new AtomicLong(System.currentTimeMillis());
        private final AtomicLong lastHourReset = new AtomicLong(System.currentTimeMillis());

        public void incrementRequests() {
            long now = System.currentTimeMillis();
            lastRequestTime.set(now);

            // Reset contador de segundo se necessário
            if (now - lastSecondReset.get() >= 1_000) { // 1 segundo
                requestsInLastSecond.set(0);
                lastSecondReset.set(now);
            }

            // Reset contador de minuto se necessário
            if (now - lastMinuteReset.get() >= 60_000) { // 1 minuto
                requestsInLastMinute.set(0);
                lastMinuteReset.set(now);
            }

            // Reset contador de hora se necessário
            if (now - lastHourReset.get() >= 3_600_000) { // 1 hora
                requestsInLastHour.set(0);
                lastHourReset.set(now);
            }

            requestsInLastSecond.incrementAndGet();
            requestsInLastMinute.incrementAndGet();
            requestsInLastHour.incrementAndGet();
        }

        public int getRequestsInLastSecond() {
            // Verifica se precisa resetar
            if (System.currentTimeMillis() - lastSecondReset.get() >= 1_000) {
                return 0;
            }
            return requestsInLastSecond.get();
        }

        public int getRequestsInLastMinute() {
            // Verifica se precisa resetar
            if (System.currentTimeMillis() - lastMinuteReset.get() >= 60_000) {
                return 0;
            }
            return requestsInLastMinute.get();
        }

        public int getRequestsInLastHour() {
            // Verifica se precisa resetar
            if (System.currentTimeMillis() - lastHourReset.get() >= 3_600_000) {
                return 0;
            }
            return requestsInLastHour.get();
        }

        public LocalDateTime getLastRequestTime() {
            return LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(lastRequestTime.get()),
                java.time.ZoneId.systemDefault()
            );
        }
    }

    /**
     * Classe para retornar estatísticas de rate limiting
     */
    public static class RateLimitStats {
        private final int requestsInLastSecond;
        private final int requestsInLastMinute;
        private final int requestsInLastHour;
        private final boolean isBlocked;
        private final LocalDateTime blockedUntil;

        public RateLimitStats(int requestsInLastSecond, int requestsInLastMinute, int requestsInLastHour, 
                             boolean isBlocked, LocalDateTime blockedUntil) {
            this.requestsInLastSecond = requestsInLastSecond;
            this.requestsInLastMinute = requestsInLastMinute;
            this.requestsInLastHour = requestsInLastHour;
            this.isBlocked = isBlocked;
            this.blockedUntil = blockedUntil;
        }

        // Getters
        public int getRequestsInLastSecond() { return requestsInLastSecond; }
        public int getRequestsInLastMinute() { return requestsInLastMinute; }
        public int getRequestsInLastHour() { return requestsInLastHour; }
        public boolean isBlocked() { return isBlocked; }
        public LocalDateTime getBlockedUntil() { return blockedUntil; }
    }
}