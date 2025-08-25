package org.viniciusvirgilli.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.ws.rs.core.Response;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.viniciusvirgilli.dto.CamposComProblemasDTO;

import java.util.Date;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidadorException extends RuntimeException {

    private int codigoHTTP = Response.Status.BAD_REQUEST.getStatusCode();
    private CamposComProblemasDTO camposComProblemas;

    public ValidadorException(CamposComProblemasDTO camposComProblemas) {
        super("Campos com problemas");
        this.camposComProblemas = camposComProblemas;
    }
}
