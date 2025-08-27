package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO para resposta de health check dos serviços
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HealthResponseDTO {

    /**
     * Status do serviço (OK, ERROR, etc.)
     */
    @JsonProperty("status")
    @NotNull(message = "Status é obrigatório")
    @Size(max = 20, message = "Status deve ter no máximo 20 caracteres")
    private String status;

    /**
     * Mensagem descritiva do status
     */
    @JsonProperty("mensagem")
    @NotNull(message = "Mensagem é obrigatória")
    @Size(max = 200, message = "Mensagem deve ter no máximo 200 caracteres")
    private String mensagem;
}