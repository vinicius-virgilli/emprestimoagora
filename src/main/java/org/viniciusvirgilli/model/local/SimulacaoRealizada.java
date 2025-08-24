package org.viniciusvirgilli.model.local;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Date;

import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "simulacao_realizada")
public class SimulacaoRealizada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_simulacao")
    private Long idSimulacao;

    @NotNull
    @Column(name = "codigo_produto")
    private Integer codigoProduto;

    @NotNull
    @Column(name = "descricao_produto", length = 140)
    private String descricaoProduto;

    @NotNull
    @Positive
    @Column(name = "prazo")
    private Short prazo;

    @NotNull
    @Positive
    @Column(name = "taxa_juros")
    private BigDecimal taxaJuros;

    @NotNull
    @Positive
    @Column(name = "valor_desejado")
    private BigDecimal valorDesejado;

    @NotNull
    @Positive
    @Column(name = "valor_total_sac")
    private BigDecimal valorTotalSAC;

    @NotNull
    @Positive
    @Column(name = "valor_total_price")
    private BigDecimal valorTotalPRICE;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_simulacao")
    private Date dataSimulacao;

    @Override
    public String toString() {
        return "SimulacaoRealizada{" +
                "idSimulacao=" + idSimulacao +
                ", codigoProduto=" + codigoProduto +
                ", descricaoProduto='" + descricaoProduto + '\'' +
                ", prazo=" + prazo +
                ", taxaJuros=" + taxaJuros +
                ", valorDesejado=" + valorDesejado +
                ", valorTotalSAC=" + valorTotalSAC +
                ", valorTotalPRICE=" + valorTotalPRICE +
                ", dataSimulacao=" + dataSimulacao +
                '}';
    }

}
