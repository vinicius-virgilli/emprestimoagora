package org.viniciusvirgilli.dao;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.viniciusvirgilli.model.Produto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class ProdutoDao implements PanacheRepository<Produto> {

    @PersistenceContext(name = "default")
    EntityManager em;

    /**
     * Busca produto por código
     * @param codigoProduto código do produto
     * @return Optional com o produto encontrado
     */
    public Optional<Produto> findByCodigoProduto(Integer codigoProduto) {
        return find("codigoProduto", codigoProduto).firstResultOptional();
    }

    /**
     * Busca produtos por nome (busca parcial)
     * @param nomeProduto nome ou parte do nome do produto
     * @return lista de produtos encontrados
     */
    public List<Produto> findByNomeProduto(String nomeProduto) {
        return find("nomeProduto like ?1", "%" + nomeProduto + "%").list();
    }

    /**
     * Busca produtos por faixa de taxa de juros
     * @param taxaMinima taxa mínima
     * @param taxaMaxima taxa máxima
     * @return lista de produtos na faixa especificada
     */
    public List<Produto> findByFaixaTaxaJuros(BigDecimal taxaMinima, BigDecimal taxaMaxima) {
        return find("percentualTaxaJuros >= ?1 and percentualTaxaJuros <= ?2", taxaMinima, taxaMaxima).list();
    }

    /**
     * Busca produtos por faixa de meses
     * @param mesesMinimo número mínimo de meses
     * @param mesesMaximo número máximo de meses
     * @return lista de produtos na faixa especificada
     */
    public List<Produto> findByFaixaMeses(Short mesesMinimo, Short mesesMaximo) {
        return find("numeroMinimoMeses >= ?1 and (numeroMaximoMeses <= ?2 or numeroMaximoMeses is null)", 
                   mesesMinimo, mesesMaximo).list();
    }

    /**
     * Busca produtos por faixa de valor
     * @param valorMinimo valor mínimo
     * @param valorMaximo valor máximo
     * @return lista de produtos na faixa especificada
     */
    public List<Produto> findByFaixaValor(BigDecimal valorMinimo, BigDecimal valorMaximo) {
        return find("valorMinimo <= ?1 and (valorMaximo >= ?2 or valorMaximo is null)", 
                   valorMinimo, valorMaximo).list();
    }

    /**
     * Busca produtos adequados para um empréstimo específico
     * @param valorEmprestimo valor do empréstimo desejado
     * @param prazoMeses prazo em meses desejado
     * @return lista de produtos adequados
     */
    public List<Produto> findProdutosParaEmprestimo(BigDecimal valorEmprestimo, Short prazoMeses) {
        return find("valorMinimo <= ?1 and (valorMaximo >= ?1 or valorMaximo is null) " +
                   "and numeroMinimoMeses <= ?2 and (numeroMaximoMeses >= ?2 or numeroMaximoMeses is null)", 
                   valorEmprestimo, prazoMeses).list();
    }

    /**
     * Lista todos os produtos ordenados por taxa de juros
     * @return lista de produtos ordenada por taxa crescente
     */
    public List<Produto> findAllOrderByTaxaJuros() {
        return find("order by percentualTaxaJuros asc").list();
    }

    /**
     * Lista todos os produtos ordenados por valor mínimo
     * @return lista de produtos ordenada por valor mínimo crescente
     */
    public List<Produto> findAllOrderByValorMinimo() {
        return find("order by valorMinimo asc").list();
    }

    /**
     * Verifica se existe produto com o código especificado
     * @param codigoProduto código do produto
     * @return true se existe, false caso contrário
     */
    public boolean existsByCodigoProduto(Integer codigoProduto) {
        return count("codigoProduto", codigoProduto) > 0;
    }

    /**
     * Remove produto por código
     * @param codigoProduto código do produto
     * @return número de registros removidos
     */
    public long deleteByCodigoProduto(Integer codigoProduto) {
        return delete("codigoProduto", codigoProduto);
    }
}