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
 * DTO de resposta para dados de telemetria
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

}