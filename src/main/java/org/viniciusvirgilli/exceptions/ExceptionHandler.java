package org.viniciusvirgilli.exceptions;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.viniciusvirgilli.dto.ErroDetailDto;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Provider
public class ExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        Map<String, String> error = new HashMap<>();
        Throwable cause = getCause(exception);

        if (exception instanceof InvalidFormatException invalidFormatException) {

            List<JsonMappingException.Reference> path = invalidFormatException.getPath();
            String fieldName = "campo";
            if (path != null && !path.isEmpty()) {
                fieldName = path.get(path.size() - 1).getFieldName();
            }

            Class<?> targetType = invalidFormatException.getTargetType();
            if (Number.class.isAssignableFrom(targetType)) {
                error.put(fieldName, "O campo '" + fieldName + "' causou um erro de conversão, verifique o valor por favor!");
            } else {
                error.put(fieldName, "O campo '" + fieldName + "' causou um erro de conversão, verifique o valor por favor!");
            }

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErroDetailDto.builder()
                            .message(exception.getMessage())
                            .status(Response.Status.BAD_REQUEST.getStatusCode())
                            .cause(exception.toString())
                            .timestamp(new Date())
                            .build())
                    .build();
        }

        if (exception instanceof JsonParseException) {
            error.put("json", "O corpo da requisição contém JSON inválido.");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .build();
        }

        if (exception instanceof JsonMappingException mappingException) {
            error.put("json", "Erro ao mapear campos do JSON: " + mappingException.getOriginalMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErroDetailDto.builder()
                            .message(exception.getMessage())
                            .status(Response.Status.BAD_REQUEST.getStatusCode())
                            .cause(exception.toString())
                            .timestamp(new Date())
                            .build())
                    .build();
        }

        error.put("erro", "Erro interno do servidor: " + exception.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErroDetailDto.builder()
                        .message(exception.getMessage())
                        .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                        .cause(exception.toString())
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

