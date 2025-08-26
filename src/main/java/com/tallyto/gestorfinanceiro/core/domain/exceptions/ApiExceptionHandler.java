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

    @ExceptionHandler(EntityInUseException.class)
    public final ResponseEntity<Problem> handleEntityInUseException(EntityInUseException ex, WebRequest request) {
        log.warn("EntityInUseException: {}", ex.getMessage(), ex);
        
        Problem problem = Problem.builder()
                .timestamp(OffsetDateTime.now())
                .message(ex.getMessage())
                .userMessage(ex.getMessage())
                .detail(request.getDescription(false))
                .status(HttpStatus.CONFLICT.value())
                .type(ProblemType.ENTIDADE_EM_USO.getUri())
                .title(ProblemType.ENTIDADE_EM_USO.getTitle())
                .build();
                
        return new ResponseEntity<>(problem, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public final ResponseEntity<Problem> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
        log.error("DataIntegrityViolationException: {}", ex.getMessage(), ex);
        
        String mensagemUsuario = MSG_ERRO_GENERICA_USUARIO_FINAL;
        ProblemType problemType = ProblemType.ERRO_DE_SISTEMA;
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        // Verifica se é um erro de violação de constraint de chave estrangeira
        if (ex.getMessage() != null && ex.getMessage().contains("violates foreign key constraint")) {
            String mensagemErro = ex.getMessage().toLowerCase();
            problemType = ProblemType.ENTIDADE_EM_USO;
            
            // Extrai o nome da tabela e da constraint
            String tabelaReferenciada = extrairTabela(mensagemErro);
            String constraint = extrairConstraint(mensagemErro);
            
            if (tabelaReferenciada != null) {
                // Mapeia nomes técnicos para nomes amigáveis
                String entidadeAmigavel = mapearEntidade(tabelaReferenciada);
                mensagemUsuario = String.format(
                    "Não é possível excluir este registro pois está sendo utilizado em %s.", 
                    entidadeAmigavel
                );
            } else {
                mensagemUsuario = "Não é possível excluir este registro pois está sendo utilizado em outro lugar do sistema.";
            }
            
            log.info("Violação de chave estrangeira detectada: {}, constraint: {}", tabelaReferenciada, constraint);
        }
        
        Problem problem = Problem.builder()
                .timestamp(OffsetDateTime.now())
                .message(mensagemUsuario)
                .userMessage(mensagemUsuario)
                .detail(request.getDescription(false))
                .status(status.value())
                .type(problemType.getUri())
                .title(problemType.getTitle())
                .build();
                
        return new ResponseEntity<>(problem, status);
    }
    
    /**
     * Extrai o nome da tabela a partir da mensagem de erro
     */
    private String extrairTabela(String mensagemErro) {
        if (mensagemErro.contains("table \"")) {
            int inicio = mensagemErro.indexOf("table \"") + 7;
            int fim = mensagemErro.indexOf("\"", inicio);
            if (fim > inicio) {
                return mensagemErro.substring(inicio, fim);
            }
        }
        return null;
    }
    
    /**
     * Extrai o nome da constraint a partir da mensagem de erro
     */
    private String extrairConstraint(String mensagemErro) {
        if (mensagemErro.contains("constraint [")) {
            int inicio = mensagemErro.indexOf("constraint [") + 12;
            int fim = mensagemErro.indexOf("]", inicio);
            if (fim > inicio) {
                return mensagemErro.substring(inicio, fim);
            }
        }
        return null;
    }
    
    /**
     * Mapeia nomes técnicos de tabelas para nomes amigáveis
     */
    private String mapearEntidade(String tabela) {
        return switch (tabela.toLowerCase()) {
            case "conta_fixa" -> "Contas Fixas";
            case "provento" -> "Proventos";
            case "despesa" -> "Despesas";
            case "fatura" -> "Faturas";
            case "cartao_credito" -> "Cartões de Crédito";
            case "categoria" -> "Categorias";
            case "transacao" -> "Transações";
            case "conta" -> "Contas";
            case "reserva_emergencia" -> "Reservas de Emergência";
            default -> tabela;
        };
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
