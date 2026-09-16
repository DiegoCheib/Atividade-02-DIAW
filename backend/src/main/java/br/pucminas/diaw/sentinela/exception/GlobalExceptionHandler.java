package br.pucminas.diaw.sentinela.exception;

import br.pucminas.diaw.sentinela.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Erros de @Valid viram uma lista de campos que o formulario React destaca. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldIssue> issues = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String field = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
                    return new ApiError.FieldIssue(field, error.getDefaultMessage());
                })
                .toList();

        ApiError body = ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "Verifique os dados informados", request.getRequestURI(), issues);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApi(ApiException ex, HttpServletRequest request) {
        ApiError body = ApiError.of(ex.getStatus().value(), ex.getStatus().getReasonPhrase(),
                ex.getMessage(), request.getRequestURI(), ex.getFieldErrors());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ApiError body = ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "Corpo da requisicao invalido", request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        ApiError body = ApiError.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorized",
                "Usuario ou senha invalidos", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ApiError body = ApiError.of(HttpStatus.FORBIDDEN.value(), "Forbidden",
                "Voce nao tem permissao para acessar este recurso", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /** Ultima barreira: nunca vaza stack trace para o cliente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Erro nao tratado em {}", request.getRequestURI(), ex);
        ApiError body = ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                "Erro inesperado. Tente novamente em instantes.", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
