package com.back.chat.adapter.in.kafka;

import com.back.common.event.Envelope;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class KafkaListenerUtil {

    private KafkaListenerUtil() {
    }
    public static String safeEventId(Envelope<?> envelope) {
        try {
            if (envelope == null || envelope.header() == null) return "null";
            Object eventId = envelope.header().eventId();
            return eventId == null ? "null" : String.valueOf(eventId);
        } catch (Exception ignore) {
            return "null";
        }
    }

    public static void ackAfterCommit(Acknowledgment ack) {
        if (ack == null) return;

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            ack.acknowledge();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                ack.acknowledge();
            }
        });
    }

    public static UUID safeUuid(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isBlank()) return null;
        try { return UUID.fromString(v); }
        catch (IllegalArgumentException e) { return null; }
    }

    public static String safeMsg(Throwable t) {
        if (t == null) return "";
        String msg = t.getMessage();
        return (msg == null) ? "" : msg;
    }

    public static String safeValueAsString(Object value) {
        if (value == null) return null;
        if (value instanceof String s) return s;
        if (value instanceof byte[] bytes) return new String(bytes, StandardCharsets.UTF_8);
        return String.valueOf(value);
    }

    public static String stackTraceToString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max);
    }

    public static Throwable rootCause(Throwable t) {
        if (t == null) return null;
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) cur = cur.getCause();
        return cur;
    }
}
