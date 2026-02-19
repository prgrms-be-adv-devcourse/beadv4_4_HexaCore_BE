package com.back.chat.adapter.in.kafka;

import com.back.common.event.Envelope;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
        if (s == null || s.isBlank()) return null;
        try { return UUID.fromString(s.trim()); }
        catch (IllegalArgumentException e) { return null; }
    }
}
