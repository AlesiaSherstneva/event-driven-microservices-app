package com.microservices.common.api.handler;

import com.microservices.common.model.ElasticQueryWebClientResponseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ElasticQueryWebClientExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticQueryWebClientExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public String handleException(AccessDeniedException ex, Model model) {
        LOGGER.error("Access denied exception!");

        model.addAttribute("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        model.addAttribute("error_description", "You are not authorized to access this resource!");

        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleException(IllegalArgumentException ex, Model model) {
        LOGGER.error("Illegal argument exception!", ex);

        model.addAttribute("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        model.addAttribute("error_description", String.format("Illegal argument exception! %s", ex.getMessage()));

        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        LOGGER.error("Internal server error!", ex);

        model.addAttribute("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        model.addAttribute("error_description", "A server error occurred!");

        return "error";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleException(RuntimeException ex, Model model) {
        LOGGER.error("Service runtime exception!", ex);

        model.addAttribute("elasticQueryWebClientRequestModel",
                ElasticQueryWebClientResponseModel.builder().build());
        model.addAttribute("error", String.format("Could not get response! %s", ex.getMessage()));
        model.addAttribute("error_description", String.format("Service runtime exception! %s", ex.getMessage()));

        return "home";
    }

    @ExceptionHandler(BindException.class)
    public String handleException(BindException ex, Model model) {
        LOGGER.error("Method argument validation exception!", ex);

        Map<String,String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        model.addAttribute("elasticQueryWebClientRequestModel",
                ElasticQueryWebClientResponseModel.builder().build());
        model.addAttribute("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        model.addAttribute("error_description", errors);

        return "home";
    }
}