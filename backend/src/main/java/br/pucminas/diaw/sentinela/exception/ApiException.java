package br.pucminas.diaw.sentinela.exception;

import br.pucminas.diaw.sentinela.dto.ApiError;
import java.util.List;
import org.springframework.http.HttpStatus;

/** Excecao de negocio traduzida em resposta HTTP pelo GlobalExceptionHandler. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final List<ApiError.FieldIssue> fieldErrors;

    public ApiException(HttpStatus status, String message) {
        this(status, message, List.of());
    }

    public ApiException(HttpStatus status, String message, List<ApiError.FieldIssue> fieldErrors) {
        super(message);
        this.status = status;
        this.fieldErrors = fieldErrors;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<ApiError.FieldIssue> getFieldErrors() {
        return fieldErrors;
    }

    public static ApiException conflict(String field, String message) {
        return new ApiException(HttpStatus.CONFLICT, message, List.of(new ApiError.FieldIssue(field, message)));
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, message);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, message);
    }

    public static ApiException tooManyRequests(String message) {
        return new ApiException(HttpStatus.TOO_MANY_REQUESTS, message);
    }

    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message);
    }

    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    public static ApiException badRequest(String field, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message, List.of(new ApiError.FieldIssue(field, message)));
    }
}
