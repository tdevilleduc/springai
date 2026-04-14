package com.tdevilleduc.springai.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleIllegalArgument_shouldReturn400() {
        ProblemDetail result = handler.handleIllegalArgument(
            new IllegalArgumentException("message invalide"));

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
    }

    @Test
    void handleIllegalArgument_shouldSetTitle() {
        ProblemDetail result = handler.handleIllegalArgument(
            new IllegalArgumentException("message invalide"));

        assertEquals("Requête invalide", result.getTitle());
    }

    @Test
    void handleIllegalArgument_shouldSetDetailFromException() {
        ProblemDetail result = handler.handleIllegalArgument(
            new IllegalArgumentException("message invalide"));

        assertEquals("message invalide", result.getDetail());
    }

    @Test
    void handleGeneric_shouldReturn500() {
        ProblemDetail result = handler.handleGeneric(new RuntimeException("erreur inattendue"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
    }

    @Test
    void handleGeneric_shouldSetTitle() {
        ProblemDetail result = handler.handleGeneric(new RuntimeException("erreur inattendue"));

        assertEquals("Erreur interne", result.getTitle());
    }

    @Test
    void handleGeneric_shouldSetGenericDetail() {
        ProblemDetail result = handler.handleGeneric(new RuntimeException("erreur inattendue"));

        assertEquals("Une erreur inattendue s'est produite.", result.getDetail());
    }
}
