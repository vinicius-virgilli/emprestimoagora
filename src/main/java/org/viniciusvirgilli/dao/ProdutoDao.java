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

    // Métodos básicos de persistência
    public void persist(Produto entity) {
        em.persist(entity);
    }

    public Produto merge(Produto entity) {
        return em.merge(entity);
    }

    public void remove(Produto entity) {
        em.remove(entity);
    }

    public Produto findById(Long id) {
        return em.find(Produto.class, id);
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
     * Busca produtos por nome (busca parcial)
     * @param nomeProduto nome ou parte do nome do produto
     * @return lista de produtos encontrados
     */
    public List<Produto> findByNomeProduto(String nomeProduto) {
        return em.createQuery(
            "SELECT p FROM Produto p WHERE p.nomeProduto LIKE :nomeProduto",
            Produto.class
        )
        .setParameter("nomeProduto", "%" + nomeProduto + "%")
        .getResultList();
    }

    /**
     * Busca produtos por faixa de taxa de juros
     * @param taxaMinima taxa mínima
     * @param taxaMaxima taxa máxima
     * @return lista de produtos na faixa especificada
     */
    public List<Produto> findByFaixaTaxaJuros(BigDecimal taxaMinima, BigDecimal taxaMaxima) {
        return em.createQuery(
            "SELECT p FROM Produto p WHERE p.percentualTaxaJuros >= :taxaMinima AND p.percentualTaxaJuros <= :taxaMaxima",
            Produto.class
        )
        .setParameter("taxaMinima", taxaMinima)
        .setParameter("taxaMaxima", taxaMaxima)
        .getResultList();
    }

    /**
     * Busca produtos por faixa de meses
     * @param mesesMinimo número mínimo de meses
     * @param mesesMaximo número máximo de meses
     * @return lista de produtos na faixa especificada
     */
    public List<Produto> findByFaixaMeses(Short mesesMinimo, Short mesesMaximo) {
        return em.createQuery(
            "SELECT p FROM Produto p WHERE p.numeroMinimoMeses >= :mesesMinimo AND (p.numeroMaximoMeses <= :mesesMaximo OR p.numeroMaximoMeses IS NULL)",
            Produto.class
        )
        .setParameter("mesesMinimo", mesesMinimo)
        .setParameter("mesesMaximo", mesesMaximo)
        .getResultList();
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
     * Busca produtos adequados para um empréstimo específico
     * @param valorEmprestimo valor do empréstimo desejado
     * @param prazoMeses prazo em meses desejado
     * @return lista de produtos adequados
     */
    public List<Produto> findProdutosParaEmprestimo(BigDecimal valorEmprestimo, Short prazoMeses) {
        return em.createQuery(
            "SELECT p FROM Produto p WHERE p.valorMinimo <= :valorEmprestimo AND (p.valorMaximo >= :valorEmprestimo OR p.valorMaximo IS NULL) " +
            "AND p.numeroMinimoMeses <= :prazoMeses AND (p.numeroMaximoMeses >= :prazoMeses OR p.numeroMaximoMeses IS NULL)",
            Produto.class
        )
        .setParameter("valorEmprestimo", valorEmprestimo)
        .setParameter("prazoMeses", prazoMeses)
        .getResultList();
    }

    /**
     * Lista todos os produtos ordenados por taxa de juros
     * @return lista de produtos ordenada por taxa crescente
     */
    public List<Produto> findAllOrderByTaxaJuros() {
        return em.createQuery(
            "SELECT p FROM Produto p ORDER BY p.percentualTaxaJuros ASC",
            Produto.class
        ).getResultList();
    }

    /**
     * Lista todos os produtos ordenados por valor mínimo
     * @return lista de produtos ordenada por valor mínimo crescente
     */
    public List<Produto> findAllOrderByValorMinimo() {
        return em.createQuery(
            "SELECT p FROM Produto p ORDER BY p.valorMinimo ASC",
            Produto.class
        ).getResultList();
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

    /**
     * Remove produto por código
     * @param codigoProduto código do produto
     * @return número de registros removidos
     */
    public long deleteByCodigoProduto(Integer codigoProduto) {
        return em.createQuery(
            "DELETE FROM Produto p WHERE p.codigoProduto = :codigoProduto"
        )
        .setParameter("codigoProduto", codigoProduto)
        .executeUpdate();
    }
}