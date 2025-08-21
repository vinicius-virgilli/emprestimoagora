package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para resposta de volume por período
 */
public class VolumePeriodoResponseDTO {
    
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInicio;
    
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFim;
    
    @NotNull
    private List<VolumeSimuladoDTO> simulacoes;
    
    public VolumePeriodoResponseDTO() {
    }
    
    public VolumePeriodoResponseDTO(LocalDate dataInicio, LocalDate dataFim, List<VolumeSimuladoDTO> simulacoes) {
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.simulacoes = simulacoes;
    }
    
    public LocalDate getDataInicio() {
        return dataInicio;
    }
    
    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }
    
    public LocalDate getDataFim() {
        return dataFim;
    }
    
    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }
    
    public List<VolumeSimuladoDTO> getSimulacoes() {
        return simulacoes;
    }
    
    public void setSimulacoes(List<VolumeSimuladoDTO> simulacoes) {
        this.simulacoes = simulacoes;
    }
}