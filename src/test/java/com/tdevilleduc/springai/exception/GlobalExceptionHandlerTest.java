package com.tdevilleduc.springai.exception;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger handlerLogger;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();

        handlerLogger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        handlerLogger.setLevel(Level.DEBUG);

        listAppender = new ListAppender<>();
        listAppender.start();
        handlerLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        handlerLogger.detachAppender(listAppender);
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

    @Test
    void handleGeneric_shouldLogOnlyMessageAtErrorLevel() {
        RuntimeException ex = new RuntimeException("détail sensible");

        handler.handleGeneric(ex);

        List<ILoggingEvent> errorLogs = listAppender.list.stream()
            .filter(e -> e.getLevel() == Level.ERROR)
            .toList();

        assertEquals(1, errorLogs.size());
        ILoggingEvent errorEvent = errorLogs.get(0);
        assertTrue(errorEvent.getFormattedMessage().contains("détail sensible"),
            "Le message d'erreur doit contenir le message de l'exception");
        assertNull(errorEvent.getThrowableProxy(),
            "La stack trace ne doit pas être attachée au log ERROR");
    }

    @Test
    void handleGeneric_shouldLogFullStacktraceAtDebugLevel() {
        RuntimeException ex = new RuntimeException("détail sensible");

        handler.handleGeneric(ex);

        List<ILoggingEvent> debugLogs = listAppender.list.stream()
            .filter(e -> e.getLevel() == Level.DEBUG)
            .toList();

        assertEquals(1, debugLogs.size());
        assertNotNull(debugLogs.get(0).getThrowableProxy(),
            "La stack trace doit être attachée au log DEBUG");
        assertEquals(RuntimeException.class.getName(),
            debugLogs.get(0).getThrowableProxy().getClassName());
    }
}
