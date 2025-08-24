package org.viniciusvirgilli.service;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.viniciusvirgilli.model.hack.Produto;
import org.viniciusvirgilli.dao.ProdutoDao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serviço responsável pelo cache do método findAll() da entidade Produto
 * com estratégia de renovação sob demanda (expire-after-access)
 */
@ApplicationScoped
public class ProdutoCacheService {

    private static final Logger LOG = Logger.getLogger(ProdutoCacheService.class);

    @Inject
    ProdutoDao produtoDao;

    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private volatile LocalDateTime lastAccess;

    /**
     * Busca todos os produtos com cache sob demanda
     * Cache expira 5 minutos após o último acesso
     */
    @CacheResult(cacheName = "produtos-findAll-cache")
    @Transactional
    public List<Produto> findAllProdutos() {
        lastAccess = LocalDateTime.now();
        
        // Detectar se é cache hit ou miss baseado no comportamento do Quarkus Cache
        boolean isCacheHit = lastAccess != null && 
            java.time.Duration.between(lastAccess, LocalDateTime.now()).toMinutes() < 5;
        
        if (isCacheHit) {
            cacheHits.incrementAndGet();
            LOG.infof("⚡ [CACHE HIT] Produtos retornados do cache. Último acesso: %s. Total hits: %d", 
                     lastAccess.format(DateTimeFormatter.ofPattern("HH:mm:ss")), cacheHits.get());
        } else {
            cacheMisses.incrementAndGet();
            LOG.infof("🔍 [CACHE MISS] Consultando banco de dados. Total misses: %d", cacheMisses.get());
        }
        
        List<Produto> produtos = produtoDao.findAll();
        LOG.infof("[BANCO] %d produtos carregados às %s",
                 produtos.size(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        
        return produtos;
    }
}