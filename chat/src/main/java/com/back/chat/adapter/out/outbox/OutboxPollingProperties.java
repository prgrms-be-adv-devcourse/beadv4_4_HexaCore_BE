package com.back.chat.adapter.out.outbox;

public class OutboxPollingProperties {

    private OutboxPollingProperties(){
    }

    // ===== polling / retry =====
    public static final int BATCH_SIZE = 50;
    public static final int MAX_RETRY = 5;
    public static final int RETRY_BASE_DELAY_SECONDS = 2;
    public static final int RETRY_MAX_DELAY_SECONDS = 60;
    public static final long failedPollIntervalMs = 2000;

    // ===== Kafka topics =====
    public static final String CHAT_BLIND_REQUESTED_TOPIC = "chat.blind.requested";
    public static final String CHAT_BLIND_DLT_REQUESTED_TOPIC = "chat.blind.dlt.requested";
}
