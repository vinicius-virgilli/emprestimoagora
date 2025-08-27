package org.viniciusvirgilli.service;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.viniciusvirgilli.dto.ListagemSimulacoesResponseDTO;
import org.viniciusvirgilli.enums.TipoSimulacao;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Serviço de cache para simulações com invalidação automática
 */
@Slf4j
@ApplicationScoped
public class SimulacaoCacheService {

    @ConfigProperty(name = "cache.simulacoes.ttl.minutes", defaultValue = "30")
    int cacheTtlMinutes;

    @ConfigProperty(name = "cache.simulacoes.max.size", defaultValue = "10000")
    int cacheMaxSize;

    @ConfigProperty(name = "cache.simulacoes.enabled", defaultValue = "true")
    boolean cacheEnabled;

    @ConfigProperty(name = "cache.simulacoes.cleanup.interval.minutes", defaultValue = "10")
    int cleanupIntervalMinutes;

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile LocalDateTime lastInvalidation = LocalDateTime.now();

    public void init() {
        if (cacheEnabled) {
            // Agenda limpeza automática do cache
            scheduler.scheduleAtFixedRate(this::cleanExpiredEntries, 
                cleanupIntervalMinutes, cleanupIntervalMinutes, TimeUnit.MINUTES);
            log.info("Cache de simulações inicializado - TTL: {}min, Max Size: {}, Cleanup: {}min", 
                cacheTtlMinutes, cacheMaxSize, cleanupIntervalMinutes);
        }
    }

    /**
     * Busca resultado no cache
     */
    public ListagemSimulacoesResponseDTO get(Integer pagina, Integer tamanhoPagina, TipoSimulacao tipo) {
        if (!cacheEnabled) {
            return null;
        }

        String key = buildCacheKey(pagina, tamanhoPagina, tipo);
        CacheEntry entry = cache.get(key);

        if (entry == null) {
            log.debug("Cache MISS para chave: {}", key);
            return null;
        }

        // Verifica se entrada expirou
        if (isExpired(entry)) {
            cache.remove(key);
            log.debug("Cache EXPIRED para chave: {}", key);
            return null;
        }

        // Verifica se cache foi invalidado após criação da entrada
        if (entry.createdAt.isBefore(lastInvalidation)) {
            cache.remove(key);
            log.debug("Cache INVALIDATED para chave: {}", key);
            return null;
        }

        log.debug("Cache HIT para chave: {}", key);
        entry.lastAccessed = LocalDateTime.now();
        return entry.data;
    }

    /**
     * Armazena resultado no cache
     */
    public void put(Integer pagina, Integer tamanhoPagina, TipoSimulacao tipo, 
                   ListagemSimulacoesResponseDTO data) {
        if (!cacheEnabled || data == null) {
            return;
        }

        // Verifica limite de tamanho do cache
        if (cache.size() >= cacheMaxSize) {
            evictOldestEntries();
        }

        String key = buildCacheKey(pagina, tamanhoPagina, tipo);
        CacheEntry entry = new CacheEntry(data);
        cache.put(key, entry);
        
        log.debug("Cache PUT para chave: {} - {} registros", key, data.getQtdRegistrosPagina());
    }

    /**
     * Invalida todo o cache quando uma nova simulação é salva
     */
    public void invalidateAll() {
        if (!cacheEnabled) {
            return;
        }

        int sizeBefore = cache.size();
        cache.clear();
        lastInvalidation = LocalDateTime.now();
        
        log.info("Cache invalidado completamente - {} entradas removidas", sizeBefore);
    }

    /**
     * Invalida cache específico por tipo
     */
    public void invalidateByType(TipoSimulacao tipo) {
        if (!cacheEnabled) {
            return;
        }

        String typePrefix = tipo.name() + "_";
        AtomicInteger removed = new AtomicInteger();
        
        cache.entrySet().removeIf(entry -> {
            if (entry.getKey().contains(typePrefix)) {
                removed.getAndIncrement();
                return true;
            }
            return false;
        });
        
        log.debug("Cache invalidado para tipo {} - {} entradas removidas", tipo, removed);
    }

    /**
     * Remove entradas expiradas
     */
    private void cleanExpiredEntries() {
        AtomicInteger removed = new AtomicInteger();
        LocalDateTime now = LocalDateTime.now();
        
        cache.entrySet().removeIf(entry -> {
            if (isExpired(entry.getValue())) {
                removed.getAndIncrement();
                return true;
            }
            return false;
        });
        
        if (removed.get() > 0) {
            log.debug("Limpeza automática do cache - {} entradas expiradas removidas", removed);
        }
    }

    /**
     * Remove entradas mais antigas quando cache atinge limite
     */
    private void evictOldestEntries() {
        int toRemove = cacheMaxSize / 4; // Remove 25% das entradas (2500 entradas)
        
        cache.entrySet().stream()
            .sorted((e1, e2) -> e1.getValue().lastAccessed.compareTo(e2.getValue().lastAccessed))
            .limit(toRemove)
            .forEach(entry -> cache.remove(entry.getKey()));
            
        log.debug("Cache eviction - {} entradas mais antigas removidas", toRemove);
    }

    private String buildCacheKey(Integer pagina, Integer tamanhoPagina, TipoSimulacao tipo) {
        return String.format("%s_%d_%d", tipo.name(), pagina, tamanhoPagina);
    }

    private boolean isExpired(CacheEntry entry) {
        return ChronoUnit.MINUTES.between(entry.createdAt, LocalDateTime.now()) > cacheTtlMinutes;
    }

    /**
     * Retorna estatísticas do cache
     */
    public CacheStats getStats() {
        return new CacheStats(
            cache.size(),
            cacheMaxSize,
            cacheTtlMinutes,
            cacheEnabled,
            lastInvalidation
        );
    }

    /**
     * Classe interna para entrada do cache
     */
    private static class CacheEntry {
        final ListagemSimulacoesResponseDTO data;
        final LocalDateTime createdAt;
        volatile LocalDateTime lastAccessed;

        CacheEntry(ListagemSimulacoesResponseDTO data) {
            this.data = data;
            this.createdAt = LocalDateTime.now();
            this.lastAccessed = LocalDateTime.now();
        }
    }

    /**
     * Classe para estatísticas do cache
     */
    public static class CacheStats {
        public final int currentSize;
        public final int maxSize;
        public final int ttlMinutes;
        public final boolean enabled;
        public final LocalDateTime lastInvalidation;

        public CacheStats(int currentSize, int maxSize, int ttlMinutes, 
                         boolean enabled, LocalDateTime lastInvalidation) {
            this.currentSize = currentSize;
            this.maxSize = maxSize;
            this.ttlMinutes = ttlMinutes;
            this.enabled = enabled;
            this.lastInvalidation = lastInvalidation;
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}