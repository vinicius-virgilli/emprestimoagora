package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de resposta para volume diário por produto
 */
public class VolumeDiarioResponseDTO {

    /**
     * Data de referência do volume
     */
    @JsonProperty("dataReferencia")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Data de referência é obrigatória")
    private LocalDate dataReferencia;

    /**
     * Lista de simulações por produto
     */
    @JsonProperty("simulacoes")
    @NotNull(message = "Lista de simulações é obrigatória")
    @NotEmpty(message = "Lista de simulações não pode estar vazia")
    @Valid
    private List<VolumeSimuladoDTO> simulacoes;

    /**
     * Construtor padrão
     */
    public VolumeDiarioResponseDTO() {
    }

    /**
     * Construtor completo
     */
    public VolumeDiarioResponseDTO(LocalDate dataReferencia, List<VolumeSimuladoDTO> simulacoes) {
        this.dataReferencia = dataReferencia;
        this.simulacoes = simulacoes;
    }

    // Getters e Setters
    public LocalDate getDataReferencia() {
        return dataReferencia;
    }

    public void setDataReferencia(LocalDate dataReferencia) {
        this.dataReferencia = dataReferencia;
    }

    public List<VolumeSimuladoDTO> getSimulacoes() {
        return simulacoes;
    }

    public void setSimulacoes(List<VolumeSimuladoDTO> simulacoes) {
        this.simulacoes = simulacoes;
    }

    @Override
    public String toString() {
        return "VolumeDiarioResponseDTO{" +
                "dataReferencia=" + dataReferencia +
                ", simulacoes=" + simulacoes +
                '}';
    }
}