package org.viniciusvirgilli.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIEmprestimoAgoraException extends RuntimeException {

    private int codigoHTTP;
    private String mensagem;

    public APIEmprestimoAgoraException(String mensagem) {
        super(mensagem);
        this.codigoHTTP = 412; // código para quando pré-condição falhar
    }

    public APIEmprestimoAgoraException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public APIEmprestimoAgoraException(int codigoHTTP, String mensagem) {
        super(mensagem);
        this.codigoHTTP = codigoHTTP;
    }

}
