package org.viniciusvirgilli.service;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.viniciusvirgilli.entity.Produto;
import org.viniciusvirgilli.repository.ProdutoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Serviço responsável pelo gerenciamento de cache de produtos
 * com atualização automática a cada minuto
 */
@ApplicationScoped
public class CacheService {

    private static final Logger LOG = Logger.getLogger(CacheService.class);
    private static final String CACHE_NAME = "produtos-cache";

    @Inject
    ProdutoRepository produtoRepository;

    /**
     * Busca produtos compatíveis com cache
     * O resultado é armazenado em cache por 1 minuto
     */
    @CacheResult(cacheName = CACHE_NAME)
    public List<Produto> buscarProdutosCompativeis(BigDecimal valorDesejado, Short prazo) {
        LOG.infof("Buscando produtos compatíveis no banco de dados para valor: %s, prazo: %d", 
                 valorDesejado, prazo);
        
        return produtoRepository.findProdutosParaEmprestimo(valorDesejado, prazo);
    }

    /**
     * Invalida o cache automaticamente a cada 1 minuto
     * Isso força uma nova consulta ao banco de dados na próxima requisição
     */
    @Scheduled(every = "1m")
    @CacheInvalidate(cacheName = CACHE_NAME)
    public void invalidarCacheProdutos() {
        LOG.info("Cache de produtos invalidado automaticamente");
    }

    /**
     * Método para invalidar o cache manualmente se necessário
     */
    @CacheInvalidate(cacheName = CACHE_NAME)
    public void invalidarCacheManualmente() {
        LOG.info("Cache de produtos invalidado manualmente");
    }

    /**
     * Pré-carrega o cache com uma consulta genérica
     * Útil para inicialização da aplicação
     */
    public void preCarregarCache() {
        LOG.info("Pré-carregando cache de produtos");
        // Busca com valores padrão para popular o cache inicial
        buscarProdutosCompativeis(new BigDecimal("1000"), (short) 12);
    }
}