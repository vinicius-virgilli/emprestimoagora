package org.viniciusvirgilli.model.local;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "metrica_endpoint")
public class MetricaEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metrica")
    private Long idMetrica;

    @NotNull
    @Column(name = "nome_endpoint", length = 100)
    private String nomeEndpoint;

    @NotNull
    @Column(name = "data_referencia")
    private LocalDate dataReferencia;

    @NotNull
    @PositiveOrZero
    @Column(name = "total_requisicoes")
    private Long totalRequisicoes;

    @NotNull
    @PositiveOrZero
    @Column(name = "requisicoes_sucesso")
    private Long requisicoesSuccesso;

    @NotNull
    @PositiveOrZero
    @Column(name = "requisicoes_erro")
    private Long requisicoesErro;

    @NotNull
    @PositiveOrZero
    @Column(name = "duracao_total_ms", precision = 18, scale = 2)
    private BigDecimal duracaoTotalMs;

    @NotNull
    @PositiveOrZero
    @Column(name = "duracao_minima_ms", precision = 18, scale = 2)
    private BigDecimal duracaoMinimaMs;

    @NotNull
    @PositiveOrZero
    @Column(name = "duracao_maxima_ms", precision = 18, scale = 2)
    private BigDecimal duracaoMaximaMs;

    @NotNull
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @NotNull
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    /**
     * Calcula o tempo médio de resposta em milissegundos
     */
    public BigDecimal calcularTempoMedio() {
        if (totalRequisicoes == 0) {
            return BigDecimal.ZERO;
        }
        return duracaoTotalMs.divide(BigDecimal.valueOf(totalRequisicoes), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calcula o percentual de sucesso
     */
    public BigDecimal calcularPercentualSucesso() {
        if (totalRequisicoes == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(requisicoesSuccesso)
                .divide(BigDecimal.valueOf(totalRequisicoes), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}