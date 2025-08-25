package org.viniciusvirgilli.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.viniciusvirgilli.dto.ErroDetailDto;

import java.util.Date;

@Provider
public class ExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        Throwable cause = getCause(e);
        
        if (cause instanceof ValidadorException validadorException) {
            return Response.status(validadorException.getCodigoHTTP())
                    .entity(validadorException.getCamposComProblemas())
                    .status(Response.Status.BAD_REQUEST.getStatusCode())
                    .build();
        }
        
        if (cause instanceof APIEmprestimoAgoraException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErroDetailDto.builder()
                            .message(exception.getMessage())
                            .status(Response.Status.BAD_REQUEST.getStatusCode())
                            .timestamp(new Date())
                            .build())
                    .build();
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErroDetailDto.builder()
                        .message(e.getMessage())
                        .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                        .timestamp(new Date())
                        .build())
                .build();
    }

    private Throwable getCause(Throwable throwable) {
        Throwable cause;
        while ((cause = throwable.getCause()) != null) {
            throwable = cause;
        }
        return throwable;
    }
}

