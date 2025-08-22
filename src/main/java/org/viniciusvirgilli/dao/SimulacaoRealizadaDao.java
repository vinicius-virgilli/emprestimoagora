package org.viniciusvirgilli.dao;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.viniciusvirgilli.model.SimulacaoRealizada;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SimulacaoRealizadaDao implements PanacheRepository<SimulacaoRealizada> {

    @PersistenceContext(name = "local")
    EntityManager local;

    public Optional<SimulacaoRealizada> findByIdSimulacao(Long idSimulacao) {
        return find("idSimulacao", idSimulacao).firstResultOptional();
    }

    public List<SimulacaoRealizada> findAllPaginado(int pagina, int tamanhoPagina) {
        return findAll()
            .page(Page.of(pagina, tamanhoPagina))
            .list();
    }

    public List<SimulacaoRealizada> findByData(LocalDate data) {
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.plusDays(1).atStartOfDay();
        
        return list(
            "dataSimulacao >= ?1 AND dataSimulacao < ?2",
            inicioDia, fimDia
        );
    }

    public List<SimulacaoRealizada> findByProduto(Integer codigoProduto) {
        return list("codigoProduto", codigoProduto);
    }

    public List<SimulacaoRealizada> findByFaixaValor(java.math.BigDecimal valorMinimo, java.math.BigDecimal valorMaximo) {
        return list(
            "valorDesejado >= ?1 AND valorDesejado <= ?2",
            valorMinimo, valorMaximo
        );
    }

    public List<Object[]> findVolumePorProdutoPorDia(LocalDate dataReferencia) {
        LocalDateTime inicioDia = dataReferencia.atStartOfDay();
        LocalDateTime fimDia = dataReferencia.plusDays(1).atStartOfDay();
        
        return getEntityManager().createQuery(
            "SELECT s.codigoProduto, " +
            "       s.descricaoProduto, " +
            "       AVG(s.taxaJuros) as taxaMediaJuros, " +
            "       AVG((s.valorTotalSAC + s.valorTotalPRICE) / 2 / s.prazoMeses) as valorMedioPrestacao, " +
            "       SUM(s.valorDesejado) as valorTotalDesejado, " +
            "       AVG(s.valorTotalSAC / s.prazoMeses) as valorMedioPrestacaoSAC, " +
            "       SUM(s.valorTotalSAC) as valorTotalCreditoSAC, " +
            "       AVG(s.valorTotalPRICE / s.prazoMeses) as valorMedioPrestacaoPRICE, " +
            "       SUM(s.valorTotalPRICE) as valorTotalCreditoPRICE " +
            "FROM SimulacaoRealizada s " +
            "WHERE s.dataSimulacao >= :inicioDia AND s.dataSimulacao < :fimDia " +
            "GROUP BY s.codigoProduto, s.descricaoProduto " +
            "ORDER BY s.codigoProduto",
            Object[].class
        )
        .setParameter("inicioDia", inicioDia)
        .setParameter("fimDia", fimDia)
        .getResultList();
    }

    public List<Object[]> findVolumePorProdutoPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicioDateTime = dataInicio.atStartOfDay();
        LocalDateTime fimDateTime = dataFim.plusDays(1).atStartOfDay();
        
        return getEntityManager().createQuery(
            "SELECT s.codigoProduto, " +
            "       s.descricaoProduto, " +
            "       AVG(s.taxaJuros) as taxaMediaJuros, " +
            "       AVG((s.valorTotalSAC + s.valorTotalPRICE) / 2 / s.prazoMeses) as valorMedioPrestacao, " +
            "       SUM(s.valorDesejado) as valorTotalDesejado, " +
            "       AVG(s.valorTotalSAC / s.prazoMeses) as valorMedioPrestacaoSAC, " +
            "       SUM(s.valorTotalSAC) as valorTotalCreditoSAC, " +
            "       AVG(s.valorTotalPRICE / s.prazoMeses) as valorMedioPrestacaoPRICE, " +
            "       SUM(s.valorTotalPRICE) as valorTotalCreditoPRICE " +
            "FROM SimulacaoRealizada s " +
            "WHERE s.dataSimulacao >= :inicioDateTime AND s.dataSimulacao < :fimDateTime " +
            "GROUP BY s.codigoProduto, s.descricaoProduto " +
            "ORDER BY s.codigoProduto",
            Object[].class
        )
        .setParameter("inicioDateTime", inicioDateTime)
        .setParameter("fimDateTime", fimDateTime)
        .getResultList();
    }

    public Object[] findEstatisticasPorDia(LocalDate dataReferencia) {
        LocalDateTime inicioDia = dataReferencia.atStartOfDay();
        LocalDateTime fimDia = dataReferencia.plusDays(1).atStartOfDay();
        
        List<Object[]> resultado = getEntityManager().createQuery(
            "SELECT COUNT(s) as qtdRequisicoes, " +
            "       AVG(1.5) as tempoMedio, " +  // Simulando tempo médio de 1.5s
            "       MIN(0.8) as tempoMinimo, " +   // Simulando tempo mínimo de 0.8s
            "       MAX(3.2) as tempoMaximo, " +   // Simulando tempo máximo de 3.2s
            "       100.0 as percentualSucesso " + // Simulando 100% de sucesso
            "FROM SimulacaoRealizada s " +
            "WHERE s.dataSimulacao >= :inicioDia AND s.dataSimulacao < :fimDia",
            Object[].class
        )
        .setParameter("inicioDia", inicioDia)
        .setParameter("fimDia", fimDia)
        .getResultList();
        
        return resultado.isEmpty() ? new Object[]{0, 1.5, 0.8, 3.2, 100.0} : resultado.get(0);
    }

    public List<SimulacaoRealizada> findByPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return list(
            "dataSimulacao >= ?1 AND dataSimulacao <= ?2",
            dataInicio, dataFim
        );
    }

    public long countByProduto(Integer codigoProduto) {
        return count("codigoProduto", codigoProduto);
    }

    public long countByData(LocalDate data) {
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.plusDays(1).atStartOfDay();
        
        return count(
            "dataSimulacao >= ?1 AND dataSimulacao < ?2",
            inicioDia, fimDia
        );
    }

    public int deleteSimulacoesAntigas(LocalDateTime dataLimite) {
        return (int) delete("dataSimulacao < ?1", dataLimite);
    }

    public List<SimulacaoRealizada> findUltimasSimulacoes(int limite) {
        return find("ORDER BY dataSimulacao DESC")
            .page(Page.ofSize(limite))
            .list();
    }

    public List<Object[]> findProdutosMaisSimulados(int limite) {
        return getEntityManager().createQuery(
            "SELECT s.codigoProduto, s.descricaoProduto, COUNT(s) as total " +
            "FROM SimulacaoRealizada s " +
            "GROUP BY s.codigoProduto, s.descricaoProduto " +
            "ORDER BY COUNT(s) DESC",
            Object[].class
        )
        .setMaxResults(limite)
        .getResultList();
    }

    public java.math.BigDecimal calcularValorMedioSimulado(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.plusDays(1).atStartOfDay();
        
        Object resultado = getEntityManager().createQuery(
            "SELECT AVG(s.valorDesejado) " +
            "FROM SimulacaoRealizada s " +
            "WHERE s.dataSimulacao >= :inicio AND s.dataSimulacao < :fim"
        )
        .setParameter("inicio", inicio)
        .setParameter("fim", fim)
        .getSingleResult();
        
        return resultado != null ? (java.math.BigDecimal) resultado : java.math.BigDecimal.ZERO;
    }
}