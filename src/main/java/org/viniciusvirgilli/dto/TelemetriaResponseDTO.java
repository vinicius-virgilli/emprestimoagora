package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de resposta para dados de telemetria
 */
public class TelemetriaResponseDTO {

    /**
     * Data de referência da telemetria
     */
    @JsonProperty("dataReferencia")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Data de referência é obrigatória")
    private LocalDate dataReferencia;

    /**
     * Lista de endpoints com dados de telemetria
     */
    @JsonProperty("listaEndpoints")
    @NotNull(message = "Lista de endpoints é obrigatória")
    @NotEmpty(message = "Lista de endpoints não pode estar vazia")
    @Valid
    private List<TelemetriaEndpointDTO> listaEndpoints;

    /**
     * Construtor padrão
     */
    public TelemetriaResponseDTO() {
    }

    /**
     * Construtor completo
     */
    public TelemetriaResponseDTO(LocalDate dataReferencia, List<TelemetriaEndpointDTO> listaEndpoints) {
        this.dataReferencia = dataReferencia;
        this.listaEndpoints = listaEndpoints;
    }

    // Getters e Setters
    public LocalDate getDataReferencia() {
        return dataReferencia;
    }

    public void setDataReferencia(LocalDate dataReferencia) {
        this.dataReferencia = dataReferencia;
    }

    public List<TelemetriaEndpointDTO> getListaEndpoints() {
        return listaEndpoints;
    }

    public void setListaEndpoints(List<TelemetriaEndpointDTO> listaEndpoints) {
        this.listaEndpoints = listaEndpoints;
    }

    @Override
    public String toString() {
        return "TelemetriaResponseDTO{" +
                "dataReferencia=" + dataReferencia +
                ", listaEndpoints=" + listaEndpoints +
                '}';
    }
}