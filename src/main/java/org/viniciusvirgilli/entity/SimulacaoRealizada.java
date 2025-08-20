package org.viniciusvirgilli.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

/**
 * Entidade que representa uma simulação realizada e persistida no banco local
 */
@Entity
@Table(name = "simulacao_realizada")
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

    // Construtores
    public SimulacaoRealizada() {
        this.dataCriacao = LocalDateTime.now();
    }

    public SimulacaoRealizada(Long idSimulacao, Integer codigoProduto, String descricaoProduto,
                             BigDecimal valorDesejado, Short prazoMeses, BigDecimal taxaJuros,
                             BigDecimal valorTotalSAC, BigDecimal valorTotalPRICE,
                             LocalDateTime dataSimulacao) {
        this();
        this.idSimulacao = idSimulacao;
        this.codigoProduto = codigoProduto;
        this.descricaoProduto = descricaoProduto;
        this.valorDesejado = valorDesejado;
        this.prazoMeses = prazoMeses;
        this.taxaJuros = taxaJuros;
        this.valorTotalSAC = valorTotalSAC;
        this.valorTotalPRICE = valorTotalPRICE;
        this.dataSimulacao = dataSimulacao;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdSimulacao() {
        return idSimulacao;
    }

    public void setIdSimulacao(Long idSimulacao) {
        this.idSimulacao = idSimulacao;
    }

    public Integer getCodigoProduto() {
        return codigoProduto;
    }

    public void setCodigoProduto(Integer codigoProduto) {
        this.codigoProduto = codigoProduto;
    }

    public String getDescricaoProduto() {
        return descricaoProduto;
    }

    public void setDescricaoProduto(String descricaoProduto) {
        this.descricaoProduto = descricaoProduto;
    }

    public BigDecimal getValorDesejado() {
        return valorDesejado;
    }

    public void setValorDesejado(BigDecimal valorDesejado) {
        this.valorDesejado = valorDesejado;
    }

    public Short getPrazoMeses() {
        return prazoMeses;
    }

    public void setPrazoMeses(Short prazoMeses) {
        this.prazoMeses = prazoMeses;
    }

    public BigDecimal getTaxaJuros() {
        return taxaJuros;
    }

    public void setTaxaJuros(BigDecimal taxaJuros) {
        this.taxaJuros = taxaJuros;
    }

    public BigDecimal getValorTotalSAC() {
        return valorTotalSAC;
    }

    public void setValorTotalSAC(BigDecimal valorTotalSAC) {
        this.valorTotalSAC = valorTotalSAC;
    }

    public BigDecimal getValorTotalPRICE() {
        return valorTotalPRICE;
    }

    public void setValorTotalPRICE(BigDecimal valorTotalPRICE) {
        this.valorTotalPRICE = valorTotalPRICE;
    }

    public LocalDateTime getDataSimulacao() {
        return dataSimulacao;
    }

    public void setDataSimulacao(LocalDateTime dataSimulacao) {
        this.dataSimulacao = dataSimulacao;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    @Override
    public String toString() {
        return "SimulacaoRealizada{" +
                "id=" + id +
                ", idSimulacao=" + idSimulacao +
                ", codigoProduto=" + codigoProduto +
                ", descricaoProduto='" + descricaoProduto + '\'' +
                ", valorDesejado=" + valorDesejado +
                ", prazoMeses=" + prazoMeses +
                ", taxaJuros=" + taxaJuros +
                ", valorTotalSAC=" + valorTotalSAC +
                ", valorTotalPRICE=" + valorTotalPRICE +
                ", dataSimulacao=" + dataSimulacao +
                ", dataCriacao=" + dataCriacao +
                '}';
    }
}