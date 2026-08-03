package com.github.marcelorodrigo.dutytracker.gateway.controllers;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.slf4j.event.KeyValuePair;

public final class TestLogCapture implements AutoCloseable {

    private final Logger logger;
    private final ListAppender<ILoggingEvent> appender;

    private TestLogCapture(Class<?> loggerType) {
        logger = (Logger) LoggerFactory.getLogger(loggerType);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    public static TestLogCapture forClass(Class<?> loggerType) {
        return new TestLogCapture(loggerType);
    }

    public List<ILoggingEvent> eventsWithMessage(String message) {
        return appender.list.stream()
                .filter(event -> message.equals(event.getFormattedMessage()))
                .toList();
    }

    public List<KeyValuePair> keyValuePairsForMessage(String message) {
        return eventsWithMessage(message).stream()
                .flatMap(event -> event.getKeyValuePairs().stream())
                .toList();
    }

    @Override
    public void close() {
        logger.detachAppender(appender);
        appender.stop();
    }
}
