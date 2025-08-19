package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.viniciusvirgilli.enums.TipoSimulacao;

import java.util.List;

/**
 * DTO representando o resultado de uma simulação (SAC ou PRICE)
 */
public class ResultadoSimulacaoDTO {

    /**
     * Tipo da simulação (SAC ou PRICE)
     */
    @JsonProperty("tipo")
    @NotNull
    private TipoSimulacao tipo;

    /**
     * Lista de parcelas da simulação
     */
    @JsonProperty("parcelas")
    @NotNull
    @NotEmpty
    @Valid
    private List<ParcelaDTO> parcelas;

    /**
     * Construtor padrão
     */
    public ResultadoSimulacaoDTO() {
    }

    /**
     * Construtor completo
     */
    public ResultadoSimulacaoDTO(TipoSimulacao tipo, List<ParcelaDTO> parcelas) {
        this.tipo = tipo;
        this.parcelas = parcelas;
    }

    // Getters e Setters
    public TipoSimulacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoSimulacao tipo) {
        this.tipo = tipo;
    }

    public List<ParcelaDTO> getParcelas() {
        return parcelas;
    }

    public void setParcelas(List<ParcelaDTO> parcelas) {
        this.parcelas = parcelas;
    }

    @Override
    public String toString() {
        return "ResultadoSimulacaoDTO{" +
                "tipo=" + tipo +
                ", parcelas=" + parcelas +
                '}';
    }
}