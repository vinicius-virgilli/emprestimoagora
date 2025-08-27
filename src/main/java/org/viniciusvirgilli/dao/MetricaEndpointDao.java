package org.viniciusvirgilli.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import io.quarkus.hibernate.orm.PersistenceUnit;
import lombok.extern.slf4j.Slf4j;
import org.viniciusvirgilli.model.local.MetricaEndpoint;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class MetricaEndpointDao {

    @Inject
    @PersistenceUnit("local")
    EntityManager local;

    @Transactional
    public MetricaEndpoint salvar(MetricaEndpoint metrica) {
        try {
            if (metrica.getIdMetrica() == null) {
                local.persist(metrica);
                log.info("Nova métrica persistida para endpoint: {} na data: {}", 
                        metrica.getNomeEndpoint(), metrica.getDataReferencia());
            } else {
                metrica = local.merge(metrica);
                log.info("Métrica atualizada para endpoint: {} na data: {}", 
                        metrica.getNomeEndpoint(), metrica.getDataReferencia());
            }
            return metrica;
        } catch (Exception e) {
            log.error("Erro ao salvar métrica para endpoint: {} na data: {}", 
                    metrica.getNomeEndpoint(), metrica.getDataReferencia(), e);
            throw e;
        }
    }

    public Optional<MetricaEndpoint> buscarPorEndpointEData(String nomeEndpoint, LocalDate dataReferencia) {
        try {
            TypedQuery<MetricaEndpoint> query = local.createQuery(
                    "SELECT m FROM MetricaEndpoint m WHERE m.nomeEndpoint = :endpoint AND m.dataReferencia = :data",
                    MetricaEndpoint.class
            );
            query.setParameter("endpoint", nomeEndpoint);
            query.setParameter("data", dataReferencia);
            
            List<MetricaEndpoint> resultados = query.getResultList();
            return resultados.isEmpty() ? Optional.empty() : Optional.of(resultados.get(0));
        } catch (Exception e) {
            log.error("Erro ao buscar métrica para endpoint: {} na data: {}", nomeEndpoint, dataReferencia, e);
            return Optional.empty();
        }
    }

    public List<MetricaEndpoint> buscarPorData(LocalDate dataReferencia) {
        try {
            TypedQuery<MetricaEndpoint> query = local.createQuery(
                    "SELECT m FROM MetricaEndpoint m WHERE m.dataReferencia = :data ORDER BY m.nomeEndpoint",
                    MetricaEndpoint.class
            );
            query.setParameter("data", dataReferencia);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro ao buscar métricas para a data: {}", dataReferencia, e);
            return List.of();
        }
    }

    public List<MetricaEndpoint> buscarTodas() {
        try {
            TypedQuery<MetricaEndpoint> query = local.createQuery(
                    "SELECT m FROM MetricaEndpoint m ORDER BY m.dataReferencia DESC, m.nomeEndpoint",
                    MetricaEndpoint.class
            );
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro ao buscar todas as métricas", e);
            return List.of();
        }
    }

    @Transactional
    public void deletarPorData(LocalDate dataReferencia) {
        try {
            int deletados = local.createQuery(
                    "DELETE FROM MetricaEndpoint m WHERE m.dataReferencia = :data"
            )
            .setParameter("data", dataReferencia)
            .executeUpdate();
            
            log.info("Deletadas {} métricas para a data: {}", deletados, dataReferencia);
        } catch (Exception e) {
            log.error("Erro ao deletar métricas para a data: {}", dataReferencia, e);
            throw e;
        }
    }

    @Transactional
    public void deletarTodas() {
        try {
            int deletados = local.createQuery("DELETE FROM MetricaEndpoint").executeUpdate();
            log.info("Deletadas {} métricas do banco de dados", deletados);
        } catch (Exception e) {
            log.error("Erro ao deletar todas as métricas", e);
            throw e;
        }
    }
}