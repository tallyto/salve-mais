package com.tallyto.gestorfinanceiro.core.domain.exceptions;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    public static final String MSG_ERRO_GENERICA_USUARIO_FINAL = "Ocorreu um erro interno inesperado no sistema. Tente novamente e se "
        + "o problema persistir, entre em contato com o administrador do sistema.";
    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Problem> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
            Problem.builder()
                .timestamp(OffsetDateTime.now())
                .message(ex.getMessage())
                .detail(request.getDescription(false))
                .build(),
            HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public final ResponseEntity<Problem> handlerBadCredentialException(Exception ex, WebRequest request) {
        log.warn("BadCredentialsException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
            Problem.builder()
                .timestamp(OffsetDateTime.now())
                .message(ex.getMessage())
                .detail(request.getDescription(false))
                .build(),
            HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public final ResponseEntity<Problem> handleNotFoundExceptions(Exception ex, WebRequest request) {
        log.info("ResourceNotFoundException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
            Problem.builder()
                .timestamp(OffsetDateTime.now())
                .message(ex.getMessage())
                .detail(request.getDescription(false))
                .build(),
            HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public final ResponseEntity<Problem> handleBadRequestException(Exception ex, WebRequest request) {
        log.warn("BadRequestException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
            Problem.builder()
                .timestamp(OffsetDateTime.now())
                .message(ex.getMessage())
                .detail(request.getDescription(false))
                .build(),
            HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ForbiddenException.class)
    public final ResponseEntity<Problem> handleForbiddenException(Exception ex, WebRequest request) {
        log.warn("ForbiddenException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
            Problem.builder()
                .timestamp(OffsetDateTime.now())
                .message(ex.getMessage())
                .detail(request.getDescription(false))
                .build(),
            HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public final ResponseEntity<Problem> handleDataIntegrityViolationException(Exception ex, WebRequest request) {
        log.error("DataIntegrityViolationException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
            Problem.builder()
                .timestamp(OffsetDateTime.now())
                .message(ex.getMessage())
                .detail(request.getDescription(false))
                .build(),
            HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        ProblemType problemType = ProblemType.DADOS_INVALIDOS;
        String detail = "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.";

        List<Problem.Object> problemObjects = ex.getBindingResult().getAllErrors().stream()
            .map(objectError -> {
                String message = messageSource.getMessage(objectError, LocaleContextHolder.getLocale());

                String name = objectError.getObjectName();

                if (objectError instanceof FieldError) {
                    name = ((FieldError) objectError).getField();
                }

                return Problem.Object.builder()
                    .name(name)
                    .userMessage(message)
                    .build();
            })
            .collect(Collectors.toList());

        Problem problem = createProblemBuilder(status, problemType, detail)
            .message(detail)
            .objects(problemObjects)
            .build();

        return handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request);
    }

    @SuppressWarnings("null")
    @Override
    @Nullable
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {

        if (body == null) {
            body = Problem.builder()
                .timestamp(OffsetDateTime.now())
                .title(ex.getMessage())
                .status(statusCode.value())
                .message(MSG_ERRO_GENERICA_USUARIO_FINAL)
                .build();
        } else if (body instanceof String) {
            body = Problem.builder()
                .timestamp(OffsetDateTime.now())
                .title((String) body)
                .status(statusCode.value())
                .message(MSG_ERRO_GENERICA_USUARIO_FINAL)
                .build();
        }

        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    private Problem.ProblemBuilder createProblemBuilder(HttpStatusCode status,
                                                        ProblemType problemType, String detail) {

        return Problem.builder()
            .timestamp(OffsetDateTime.now())
            .status(status.value())
            .type(problemType.getUri())
            .title(problemType.getTitle())
            .detail(detail);
    }

}
