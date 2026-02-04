package com.back.chat.adapter.out.outbox;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatOutboxRepositoryImpl implements ChatOutboxRepositoryCustom{

    @PersistenceContext
    private final EntityManager em;

    @Override
    @Transactional
    public List<ChatOutbox> claimBatch(LocalDateTime now, int batchSize, int maxRetry) {

        String sql = """
            with cte as (
                select id
                from chat_outbox
                where status in ('PENDING','FAILED')
                  and retry_count < :maxRetry
                  and next_attempt_at <= :now
                order by id
                limit :batchSize
                for update skip locked
            )
            update chat_outbox o
            set status = 'PROCESSING',
                processing_started_at = :now
            from cte
            where o.id = cte.id
            returning o.*
        """;

        @SuppressWarnings("unchecked")
        List<ChatOutbox> claimed = em.createNativeQuery(sql, ChatOutbox.class)
                .setParameter("now", now)
                .setParameter("batchSize", batchSize)
                .setParameter("maxRetry", maxRetry)
                .getResultList();

        return claimed;
    }

        @Override
        public List<ChatOutbox> findProcessingBatch(int batchSize) {
            String sql = """
            select *
            from chat_outbox
            where status = 'PROCESSING'
            order by id
            limit :batchSize
        """;

            @SuppressWarnings("unchecked")
            List<ChatOutbox> list = em.createNativeQuery(sql, ChatOutbox.class)
                    .setParameter("batchSize", batchSize)
                    .getResultList();

            return list;
        }

        @Override
        @Transactional
        public int recoverStuckProcessing(LocalDateTime cutoff, LocalDateTime now) {

            String sql = """
            update chat_outbox
            set status = 'FAILED',
                next_attempt_at = :now,
                last_error = 'processing timeout',
                processing_started_at = null
            where status = 'PROCESSING'
              and processing_started_at is not null
              and processing_started_at < :cutoff
        """;

            int updated = em.createNativeQuery(sql)
                    .setParameter("now", now)
                    .setParameter("cutoff", cutoff)
                    .executeUpdate();

            em.clear();

            return updated;
        }
    }
