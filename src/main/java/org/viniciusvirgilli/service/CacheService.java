package org.viniciusvirgilli.service;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.viniciusvirgilli.model.hack.Produto;
import org.viniciusvirgilli.dao.ProdutoDao;

import java.math.BigDecimal;
import java.util.List;

/**
 * Serviço responsável pelo gerenciamento de cache de produtos
 * com atualização automática a cada minuto
 */
@ApplicationScoped
public class CacheService {

    private static final Logger LOG = Logger.getLogger(CacheService.class);
    private static final String CACHE_NAME = "produtos-cache";

    @Inject
    ProdutoDao produtoDao;

    /**
     * Busca produtos compatíveis com cache
     * O resultado é armazenado em cache por 1 minuto
     */
    @CacheResult(cacheName = CACHE_NAME)
    public List<Produto> buscarProdutosCompativeis(BigDecimal valorDesejado, Short prazo) {
        LOG.infof("Buscando produtos compatíveis no banco de dados para valor: %s, prazo: %d", 
                 valorDesejado, prazo);
        
        return produtoDao.findProdutosParaEmprestimo(valorDesejado, prazo);
    }

}