package org.viniciusvirgilli.dao;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.viniciusvirgilli.model.hack.Produto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class ProdutoDao {

    @PersistenceContext(name = "default")
    EntityManager em;

    public void persist(Produto entity) {
        em.persist(entity);
    }

    public List<Produto> findAll() {
        return em.createQuery(
            "SELECT p FROM Produto p ORDER BY p.codigoProduto",
            Produto.class
        ).getResultList();
    }

    public long count() {
        return em.createQuery(
            "SELECT COUNT(p) FROM Produto p",
            Long.class
        ).getSingleResult();
    }

    /**
     * Busca produto por código
     * @param codigoProduto código do produto
     * @return Optional com o produto encontrado
     */
    public Optional<Produto> findByCodigoProduto(Integer codigoProduto) {
        List<Produto> resultado = em.createQuery(
            "SELECT p FROM Produto p WHERE p.codigoProduto = :codigoProduto",
            Produto.class
        )
        .setParameter("codigoProduto", codigoProduto)
        .getResultList();
        
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    /**
     * Busca produtos por faixa de valor
     * @param valorMinimo valor mínimo
     * @param valorMaximo valor máximo
     * @return lista de produtos na faixa especificada
     */
    public List<Produto> findByFaixaValor(BigDecimal valorMinimo, BigDecimal valorMaximo) {
        return em.createQuery(
            "SELECT p FROM Produto p WHERE p.valorMinimo <= :valorMinimo AND (p.valorMaximo >= :valorMaximo OR p.valorMaximo IS NULL)",
            Produto.class
        )
        .setParameter("valorMinimo", valorMinimo)
        .setParameter("valorMaximo", valorMaximo)
        .getResultList();
    }

    /**
     * Verifica se existe produto com o código especificado
     * @param codigoProduto código do produto
     * @return true se existe, false caso contrário
     */
    public boolean existsByCodigoProduto(Integer codigoProduto) {
        Long count = em.createQuery(
            "SELECT COUNT(p) FROM Produto p WHERE p.codigoProduto = :codigoProduto",
            Long.class
        )
        .setParameter("codigoProduto", codigoProduto)
        .getSingleResult();
        
        return count > 0;
    }
}