package org.digit.notify.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.digit.notify.app.controller.dto.ErrorDto;
import org.digit.notify.app.exception.DuplicateConfigException;
import org.digit.notify.app.exception.DuplicateMappingException;
import org.digit.notify.app.exception.EntityNotFoundException;
import org.digit.notify.app.exception.ValidationException;
import org.digit.notify.app.template.TemplateRenderException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDto handleNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        return error("NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler({DuplicateConfigException.class, DuplicateMappingException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDto handleConflict(RuntimeException ex, HttpServletRequest req) {
        return error("CONFLICT", ex.getMessage(), req);
    }

    @ExceptionHandler({ValidationException.class, ConstraintViolationException.class,
                       MethodArgumentNotValidException.class, MissingRequestHeaderException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDto handleBadRequest(Exception ex, HttpServletRequest req) {
        return error("BAD_REQUEST", ex.getMessage(), req);
    }

    @ExceptionHandler(TemplateRenderException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorDto handleRenderError(TemplateRenderException ex, HttpServletRequest req) {
        return error("RENDER_ERROR", ex.getMessage(), req);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception on {} {}", req.getMethod(), req.getRequestURI(), ex);
        return error("INTERNAL_ERROR", "An unexpected error occurred", req);
    }

    private ErrorDto error(String code, String message, HttpServletRequest req) {
        return new ErrorDto(code, message, Instant.now().toString(),
            (String) req.getAttribute("X-Request-ID"));
    }
}
