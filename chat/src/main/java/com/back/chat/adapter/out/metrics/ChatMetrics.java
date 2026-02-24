package com.back.chat.adapter.out.metrics;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ChatMetrics {

    private final MeterRegistry registry;

    // Gauge
    private final AtomicInteger outboxRetryBacklog = new AtomicInteger(0);

    // Fixed counters (no tags)
    private final Counter wsSendTotal;
    private final Counter blindTriggerTotal;

    // Cache for tagged counters/timers
    private final ConcurrentHashMap<String, Counter> counterCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> timerCache = new ConcurrentHashMap<>();

    public ChatMetrics(MeterRegistry registry) {
        this.registry = registry;

        Gauge.builder("resello_chat_outbox_retry_backlog", outboxRetryBacklog, AtomicInteger::get)
                .description("Outbox FAILED records waiting for retry publish")
                .register(registry);

        this.wsSendTotal = Counter.builder("resello_chat_ws_send_total")
                .description("Total WS send attempts")
                .register(registry);

        this.blindTriggerTotal = Counter.builder("resello_chat_blind_trigger_total")
                .description("Total blind triggers (threshold reached)")
                .register(registry);
    }

    // ---------- Timer ----------
    public Timer timer(String name, Tags tags) {
        // key는 name + tags 조합
        String key = name + "|" + tags.toString();
        return timerCache.computeIfAbsent(key, k ->
                Timer.builder(name)
                        .tags(tags)
                        .publishPercentileHistogram()
                        .register(registry)
        );
    }

    public void recordRunnable(String name, Tags tags, Runnable task) {
        timer(name, tags).record(task);
    }

    public <T> T recordCallable(String name, Tags tags, Callable<T> task) {
        try {
            return timer(name, tags).recordCallable(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------- Counters ----------
    public void incWsSendTotal() {
        wsSendTotal.increment();
    }

    public void incBlindTrigger() {
        blindTriggerTotal.increment();
    }

    public void incOutboxPublishFail(String eventType, String cause) {
        // 태그 값은 반드시 제한된 셋으로 (폭발 방지)
        String safeEventType = safeTag(eventType);
        String safeCause = safeTag(cause);

        String key = "resello_chat_outbox_publish_fail_total|eventType=" + safeEventType + "|cause=" + safeCause;

        Counter c = counterCache.computeIfAbsent(key, k ->
                Counter.builder("resello_chat_outbox_publish_fail_total")
                        .tags("eventType", safeEventType, "cause", safeCause)
                        .register(registry)
        );
        c.increment();
    }

    // ---------- Gauge setter ----------
    public void setOutboxRetryBacklog(int failedCount) {
        outboxRetryBacklog.set(Math.max(failedCount, 0));
    }

    private String safeTag(String v) {
        if (v == null || v.isBlank()) return "UNKNOWN";
        // 너무 길면 시계열/카디널리티 위험 + Prometheus 라벨 가독성 저하
        if (v.length() > 50) return v.substring(0, 50);
        return v;
    }
}
