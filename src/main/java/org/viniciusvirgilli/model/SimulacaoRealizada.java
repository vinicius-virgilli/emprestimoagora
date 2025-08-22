package org.viniciusvirgilli.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "SIMULACAO_REALIZADA")
public class SimulacaoRealizada extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "id_simulacao", unique = true)
    private Long idSimulacao;

    @NotNull
    @Column(name = "codigo_produto")
    private Integer codigoProduto;

    @NotNull
    @Column(name = "descricao_produto", length = 200)
    private String descricaoProduto;

    @NotNull
    @Positive
    @Column(name = "valor_desejado", precision = 15, scale = 2)
    private BigDecimal valorDesejado;

    @NotNull
    @Positive
    @Column(name = "prazo_meses")
    private Short prazoMeses;

    @NotNull
    @Column(name = "taxa_juros", precision = 10, scale = 9)
    private BigDecimal taxaJuros;

    @NotNull
    @Column(name = "valor_total_sac", precision = 15, scale = 2)
    private BigDecimal valorTotalSAC;

    @NotNull
    @Column(name = "valor_total_price", precision = 15, scale = 2)
    private BigDecimal valorTotalPRICE;

    @NotNull
    @Column(name = "data_simulacao")
    private LocalDateTime dataSimulacao;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    public SimulacaoRealizada(Long idSimulacao, Integer codigoProduto, String descricaoProduto, BigDecimal valorDesejado, Short prazo, BigDecimal taxaJuros, BigDecimal valorTotalSAC, BigDecimal valorTotalPRICE, LocalDateTime dataSimulacao) {
        this.idSimulacao = idSimulacao;
        this.codigoProduto = codigoProduto;
        this.descricaoProduto = descricaoProduto;
        this.valorDesejado = valorDesejado;
        this.prazoMeses = prazo;
        this.taxaJuros = taxaJuros;
        this.valorTotalSAC = valorTotalSAC;
        this.valorTotalPRICE = valorTotalPRICE;
        this.dataSimulacao = dataSimulacao;
        this.dataCriacao = LocalDateTime.now();
    }

}