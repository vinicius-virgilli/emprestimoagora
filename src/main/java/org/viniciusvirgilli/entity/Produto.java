package org.viniciusvirgilli.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Entidade Produto representando a tabela dbo.PRODUTO
 */
@Entity
@Table(name = "PRODUTO")
public class Produto extends PanacheEntityBase {

    /**
     * Código do produto (Chave primária)
     */
    @Id
    @Column(name = "CO_PRODUTO")
    @NotNull
    public Integer codigoProduto;

    /**
     * Nome do produto
     */
    @Column(name = "NO_PRODUTO", length = 200)
    @NotNull
    @Size(max = 200)
    public String nomeProduto;

    /**
     * Percentual da taxa de juros
     */
    @Column(name = "PC_TAXA_JUROS", precision = 10, scale = 9)
    @NotNull
    public BigDecimal percentualTaxaJuros;

    /**
     * Número mínimo de meses
     */
    @Column(name = "NU_MINIMO_MESES")
    @NotNull
    public Short numeroMinimoMeses;

    /**
     * Número máximo de meses (pode ser nulo)
     */
    @Column(name = "NU_MAXIMO_MESES")
    public Short numeroMaximoMeses;

    /**
     * Valor mínimo
     */
    @Column(name = "VR_MINIMO", precision = 18, scale = 2)
    @NotNull
    public BigDecimal valorMinimo;

    /**
     * Valor máximo (pode ser nulo)
     */
    @Column(name = "VR_MAXIMO", precision = 18, scale = 2)
    public BigDecimal valorMaximo;

    /**
     * Construtor padrão
     */
    public Produto() {
    }

    /**
     * Construtor com todos os campos obrigatórios
     */
    public Produto(Integer codigoProduto, String nomeProduto, BigDecimal percentualTaxaJuros, 
                   Short numeroMinimoMeses, BigDecimal valorMinimo) {
        this.codigoProduto = codigoProduto;
        this.nomeProduto = nomeProduto;
        this.percentualTaxaJuros = percentualTaxaJuros;
        this.numeroMinimoMeses = numeroMinimoMeses;
        this.valorMinimo = valorMinimo;
    }

    /**
     * Construtor completo
     */
    public Produto(Integer codigoProduto, String nomeProduto, BigDecimal percentualTaxaJuros, 
                   Short numeroMinimoMeses, Short numeroMaximoMeses, 
                   BigDecimal valorMinimo, BigDecimal valorMaximo) {
        this.codigoProduto = codigoProduto;
        this.nomeProduto = nomeProduto;
        this.percentualTaxaJuros = percentualTaxaJuros;
        this.numeroMinimoMeses = numeroMinimoMeses;
        this.numeroMaximoMeses = numeroMaximoMeses;
        this.valorMinimo = valorMinimo;
        this.valorMaximo = valorMaximo;
    }

    @Override
    public String toString() {
        return "Produto{" +
                "codigoProduto=" + codigoProduto +
                ", nomeProduto='" + nomeProduto + '\'' +
                ", percentualTaxaJuros=" + percentualTaxaJuros +
                ", numeroMinimoMeses=" + numeroMinimoMeses +
                ", numeroMaximoMeses=" + numeroMaximoMeses +
                ", valorMinimo=" + valorMinimo +
                ", valorMaximo=" + valorMaximo +
                '}';
    }
}