package org.viniciusvirgilli.dao;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.viniciusvirgilli.dto.ListagemSimulacoesResponseDTO;
import org.viniciusvirgilli.dto.SimulacaoResumoDTO;
import org.viniciusvirgilli.enums.TipoSimulacao;
import org.viniciusvirgilli.model.local.SimulacaoRealizada;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SimulacaoRealizadaDao {

    @Inject
    @PersistenceUnit("local")
    EntityManager local;

    @Transactional
    public SimulacaoRealizada salvar(SimulacaoRealizada simulacaoRealizada) {
        if (simulacaoRealizada.getIdSimulacao() == null) {
            local.persist(simulacaoRealizada);
        } else {
            simulacaoRealizada = local.merge(simulacaoRealizada);
        }
        return simulacaoRealizada;
    }

    public void persist(SimulacaoRealizada entity) {
        local.persist(entity);
    }

    public SimulacaoRealizada merge(SimulacaoRealizada entity) {
        return local.merge(entity);
    }

    public void remove(SimulacaoRealizada entity) {
        local.remove(entity);
    }

    public SimulacaoRealizada findById(Long id) {
        return local.find(SimulacaoRealizada.class, id);
    }

    public List<SimulacaoRealizada> findAll() {
        return local.createQuery(
                "SELECT s FROM SimulacaoRealizada s ORDER BY s.dataSimulacao DESC",
                SimulacaoRealizada.class
        ).getResultList();
    }

    public long count() {
        return local.createQuery(
                "SELECT COUNT(s) FROM SimulacaoRealizada s",
                Long.class
        ).getSingleResult();
    }

    public Optional<SimulacaoRealizada> findByIdSimulacao(Long idSimulacao) {
        List<SimulacaoRealizada> resultado = local.createQuery(
                        "SELECT s FROM SimulacaoRealizada s WHERE s.idSimulacao = :idSimulacao",
                        SimulacaoRealizada.class
                )
                .setParameter("idSimulacao", idSimulacao)
                .getResultList();

        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public List<SimulacaoRealizada> findAllPaginado(int pagina, int tamanhoPagina) {
        return local.createQuery(
                        "SELECT s FROM SimulacaoRealizada s ORDER BY s.dataSimulacao DESC",
                        SimulacaoRealizada.class
                )
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }

    public List<SimulacaoRealizada> findByData(LocalDate data) {
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.plusDays(1).atStartOfDay();

        return local.createQuery(
                        "SELECT s FROM SimulacaoRealizada s WHERE s.dataSimulacao >= :inicioDia AND s.dataSimulacao < :fimDia",
                        SimulacaoRealizada.class
                )
                .setParameter("inicioDia", inicioDia)
                .setParameter("fimDia", fimDia)
                .getResultList();
    }

    public List<SimulacaoRealizada> findByProduto(Integer codigoProduto) {
        return local.createQuery(
                        "SELECT s FROM SimulacaoRealizada s WHERE s.codigoProduto = :codigoProduto",
                        SimulacaoRealizada.class
                )
                .setParameter("codigoProduto", codigoProduto)
                .getResultList();
    }

    public List<SimulacaoRealizada> findByFaixaValor(java.math.BigDecimal valorMinimo, java.math.BigDecimal valorMaximo) {
        return local.createQuery(
                        "SELECT s FROM SimulacaoRealizada s WHERE s.valorDesejado >= :valorMinimo AND s.valorDesejado <= :valorMaximo",
                        SimulacaoRealizada.class
                )
                .setParameter("valorMinimo", valorMinimo)
                .setParameter("valorMaximo", valorMaximo)
                .getResultList();
    }

    public List<Object[]> findVolumePorProdutoPorDia(LocalDate dataReferencia) {
        LocalDateTime inicioDia = dataReferencia.atStartOfDay();
        LocalDateTime fimDia = dataReferencia.plusDays(1).atStartOfDay();

        return local.createQuery(
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

        return local.createQuery(
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

        List<Object[]> resultado = local.createQuery(
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
        return local.createQuery(
                        "SELECT s FROM SimulacaoRealizada s WHERE s.dataSimulacao >= :dataInicio AND s.dataSimulacao <= :dataFim",
                        SimulacaoRealizada.class
                )
                .setParameter("dataInicio", dataInicio)
                .setParameter("dataFim", dataFim)
                .getResultList();
    }

    public long countByProduto(Integer codigoProduto) {
        return local.createQuery(
                        "SELECT COUNT(s) FROM SimulacaoRealizada s WHERE s.codigoProduto = :codigoProduto",
                        Long.class
                )
                .setParameter("codigoProduto", codigoProduto)
                .getSingleResult();
    }

    public long countByData(LocalDate data) {
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.plusDays(1).atStartOfDay();

        return local.createQuery(
                        "SELECT COUNT(s) FROM SimulacaoRealizada s WHERE s.dataSimulacao >= :inicioDia AND s.dataSimulacao < :fimDia",
                        Long.class
                )
                .setParameter("inicioDia", inicioDia)
                .setParameter("fimDia", fimDia)
                .getSingleResult();
    }

    public int deleteSimulacoesAntigas(LocalDateTime dataLimite) {
        return local.createQuery(
                        "DELETE FROM SimulacaoRealizada s WHERE s.dataSimulacao < :dataLimite"
                )
                .setParameter("dataLimite", dataLimite)
                .executeUpdate();
    }

    public List<SimulacaoRealizada> findUltimasSimulacoes(int limite) {
        return local.createQuery(
                        "SELECT s FROM SimulacaoRealizada s ORDER BY s.dataSimulacao DESC",
                        SimulacaoRealizada.class
                )
                .setMaxResults(limite)
                .getResultList();
    }

    public List<Object[]> findProdutosMaisSimulados(int limite) {
        return local.createQuery(
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

        Object resultado = local.createQuery(
                        "SELECT AVG(s.valorDesejado) " +
                                "FROM SimulacaoRealizada s " +
                                "WHERE s.dataSimulacao >= :inicio AND s.dataSimulacao < :fim"
                )
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .getSingleResult();

        return resultado != null ? (java.math.BigDecimal) resultado : java.math.BigDecimal.ZERO;
    }

    public ListagemSimulacoesResponseDTO buscaPaginada(Integer pagina, Integer tamanhoPagina, TipoSimulacao tipoEmprestimo) {
        // Monta a query base
        String jpql = "SELECT s FROM SimulacaoRealizada s";
        String countJpql = "SELECT COUNT(s) FROM SimulacaoRealizada s";

        jpql += " ORDER BY s.idSimulacao ASC";

        // Cria a query de busca paginada
        TypedQuery<SimulacaoRealizada> query = local.createQuery(jpql, SimulacaoRealizada.class);
        TypedQuery<Long> countQuery = local.createQuery(countJpql, Long.class);

        // Aplica paginação
        query.setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina);

        List<SimulacaoRealizada> simulacoes = query.getResultList();
        long totalRegistros = countQuery.getSingleResult();

        // Converte para DTO
        List<SimulacaoResumoDTO> resumos = simulacoes.stream()
                .map(simulacao -> SimulacaoResumoDTO.builder()
                        .idSimulacao(simulacao.getIdSimulacao())
                        .valorDesejado(simulacao.getValorDesejado())
                        .prazo(simulacao.getPrazo())
                        .valorTotalParcelas(tipoEmprestimo == TipoSimulacao.SAC ?
                                simulacao.getValorTotalSAC() : simulacao.getValorTotalPRICE())
                        .build())
                .toList();

        // Retorna o DTO de resposta
        return ListagemSimulacoesResponseDTO.builder()
                .pagina(pagina)
                .qtdRegistros(Math.toIntExact(totalRegistros))
                .qtdRegistrosPagina(resumos.size())
                .registros(resumos)
                .build();
    }


}