package com.tdevilleduc.springai.exception;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private TestListAppender testAppender;
    private Logger handlerLogger;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();

        handlerLogger = (Logger) LogManager.getLogger(GlobalExceptionHandler.class);
        handlerLogger.setLevel(Level.DEBUG);

        testAppender = new TestListAppender("TestAppender");
        testAppender.start();
        handlerLogger.addAppender(testAppender);
    }

    @AfterEach
    void tearDown() {
        handlerLogger.removeAppender(testAppender);
        testAppender.stop();
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

        List<LogEvent> errorLogs = testAppender.getEvents().stream()
            .filter(e -> e.getLevel() == Level.ERROR)
            .toList();

        assertEquals(1, errorLogs.size());
        LogEvent errorEvent = errorLogs.get(0);
        assertTrue(errorEvent.getMessage().getFormattedMessage().contains("détail sensible"),
            "Le message d'erreur doit contenir le message de l'exception");
        assertNull(errorEvent.getThrown(),
            "La stack trace ne doit pas être attachée au log ERROR");
    }

    @Test
    void handleGeneric_shouldLogFullStacktraceAtDebugLevel() {
        RuntimeException ex = new RuntimeException("détail sensible");

        handler.handleGeneric(ex);

        List<LogEvent> debugLogs = testAppender.getEvents().stream()
            .filter(e -> e.getLevel() == Level.DEBUG)
            .toList();

        assertEquals(1, debugLogs.size());
        Throwable thrown = debugLogs.get(0).getThrown();
        assertNotNull(thrown, "La stack trace doit être attachée au log DEBUG");
        assertEquals(RuntimeException.class, thrown.getClass());
    }

    private static class TestListAppender extends AbstractAppender {

        private final List<LogEvent> events = new ArrayList<>();

        protected TestListAppender(String name) {
            super(name, null, null, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            events.add(event.toImmutable());
        }

        public List<LogEvent> getEvents() {
            return events;
        }
    }
}
