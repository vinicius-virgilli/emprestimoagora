package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para retorno da simulação de empréstimo
 * Modelo de envelope de retorno para simulação
 */
public class SimulacaoResponseDTO {

    /**
     * ID único da simulação
     */
    @JsonProperty("idSimulacao")
    @NotNull
    @Positive
    private Long idSimulacao;

    /**
     * Código do produto utilizado na simulação
     */
    @JsonProperty("codigoProduto")
    @NotNull
    @Positive
    private Integer codigoProduto;

    /**
     * Descrição do produto
     */
    @JsonProperty("descricaoProduto")
    @NotNull
    @Size(max = 200)
    private String descricaoProduto;

    /**
     * Taxa de juros do produto (percentual)
     */
    @JsonProperty("taxaJuros")
    @NotNull
    @Positive
    private BigDecimal taxaJuros;

    /**
     * Lista com os resultados das simulações (SAC e PRICE)
     */
    @JsonProperty("resultadosSimulacao")
    @NotNull
    @NotEmpty
    @Valid
    private List<ResultadoSimulacaoDTO> resultadosSimulacao;

    /**
     * Construtor padrão
     */
    public SimulacaoResponseDTO() {
    }

    /**
     * Construtor completo
     */
    public SimulacaoResponseDTO(Long idSimulacao, Integer codigoProduto, String descricaoProduto, 
                               BigDecimal taxaJuros, List<ResultadoSimulacaoDTO> resultadosSimulacao) {
        this.idSimulacao = idSimulacao;
        this.codigoProduto = codigoProduto;
        this.descricaoProduto = descricaoProduto;
        this.taxaJuros = taxaJuros;
        this.resultadosSimulacao = resultadosSimulacao;
    }

    // Getters e Setters
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

    public BigDecimal getTaxaJuros() {
        return taxaJuros;
    }

    public void setTaxaJuros(BigDecimal taxaJuros) {
        this.taxaJuros = taxaJuros;
    }

    public List<ResultadoSimulacaoDTO> getResultadosSimulacao() {
        return resultadosSimulacao;
    }

    public void setResultadosSimulacao(List<ResultadoSimulacaoDTO> resultadosSimulacao) {
        this.resultadosSimulacao = resultadosSimulacao;
    }

    @Override
    public String toString() {
        return "SimulacaoResponseDTO{" +
                "idSimulacao=" + idSimulacao +
                ", codigoProduto=" + codigoProduto +
                ", descricaoProduto='" + descricaoProduto + '\'' +
                ", taxaJuros=" + taxaJuros +
                ", resultadosSimulacao=" + resultadosSimulacao +
                '}';
    }
}