package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;
import java.util.List;

/**
 * DTO para retornar campos com problemas de validação
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CamposComProblemasDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Brazil/East")
    private Date timestamp;

    private int status;
    private String message;
    
    @JsonProperty("Campo(s) com problema(s):")
    private List<String> campos;
    
    /**
     * Construtor de conveniência
     * @param campos lista de campos com problemas
     */
    public static CamposComProblemasDTO of(List<String> campos) {
        return new CamposComProblemasDTO(new Date(), 400, "Algo de errado não está certo!", campos);
    }
    
    @Override
    public String toString() {
        return String.join(System.lineSeparator(), campos);
    }
}