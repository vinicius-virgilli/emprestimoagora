package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de resposta para volume diário por produto
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VolumeDiarioResponseDTO {

    @JsonProperty("dataReferencia")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Data de referência é obrigatória")
    private LocalDate dataReferencia;

    @JsonProperty("simulacoes")
    @NotNull(message = "Lista de simulações é obrigatória")
    @NotEmpty(message = "Lista de simulações não pode estar vazia")
    @Valid
    private List<VolumeSimuladoDTO> simulacoes;

}