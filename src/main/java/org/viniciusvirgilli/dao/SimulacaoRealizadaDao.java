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

    public SimulacaoRealizada findById(Long id) {
        return local.find(SimulacaoRealizada.class, id);
    }

    public ListagemSimulacoesResponseDTO buscaPaginada(Integer pagina, Integer tamanhoPagina, TipoSimulacao tipoEmprestimo) {

        String jpql = "SELECT s FROM SimulacaoRealizada s";
        String countJpql = "SELECT COUNT(s) FROM SimulacaoRealizada s";

        jpql += " ORDER BY s.idSimulacao ASC";

        TypedQuery<SimulacaoRealizada> query = local.createQuery(jpql, SimulacaoRealizada.class);
        TypedQuery<Long> countQuery = local.createQuery(countJpql, Long.class);

        query.setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina);

        List<SimulacaoRealizada> simulacoes = query.getResultList();
        long totalRegistros = countQuery.getSingleResult();

        List<SimulacaoResumoDTO> resumos = simulacoes.stream()
                .map(simulacao -> SimulacaoResumoDTO.builder()
                        .idSimulacao(simulacao.getIdSimulacao())
                        .valorDesejado(simulacao.getValorDesejado())
                        .prazo(simulacao.getPrazo())
                        .valorTotalParcelas(tipoEmprestimo == TipoSimulacao.SAC ?
                                simulacao.getValorTotalSAC() : simulacao.getValorTotalPRICE())
                        .build())
                .toList();

        return ListagemSimulacoesResponseDTO.builder()
                .pagina(pagina)
                .qtdRegistros(Math.toIntExact(totalRegistros))
                .qtdRegistrosPagina(resumos.size())
                .registros(resumos)
                .build();
    }

    public List<SimulacaoRealizada> findVolumePorProdutoPorDia(LocalDate date, int codigoProduto) {
    // ✅ CORREÇÃO: Converter LocalDate para LocalDateTime e usar intervalo
    LocalDateTime inicioDia = date.atStartOfDay();
    LocalDateTime fimDia = date.plusDays(1).atStartOfDay();

    return local.createQuery(
            "SELECT s FROM SimulacaoRealizada s WHERE s.dataSimulacao >= :inicioDia AND s.dataSimulacao < :fimDia AND s.codigoProduto = :codigoProduto",
            SimulacaoRealizada.class
    )
            .setParameter("inicioDia", inicioDia)
            .setParameter("fimDia", fimDia)
            .setParameter("codigoProduto", codigoProduto)
            .getResultList();
}
}