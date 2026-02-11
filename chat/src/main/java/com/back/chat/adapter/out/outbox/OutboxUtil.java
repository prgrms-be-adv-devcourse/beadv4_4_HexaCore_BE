package com.back.chat.adapter.out.outbox;

public final class OutboxUtil {
    private static final int MAX_LENGTH = 200;

    private OutboxUtil() {
    }

    public static String safeMsg(Throwable t) {
        if (t == null) {
            return "Unknown";
        }

        String msg = t.getMessage();
        if (msg == null || msg.isBlank()) {
            return t.getClass().getSimpleName();
        }

        return msg.length() <= MAX_LENGTH
                ? msg
                : msg.substring(0, MAX_LENGTH);
    }
}
