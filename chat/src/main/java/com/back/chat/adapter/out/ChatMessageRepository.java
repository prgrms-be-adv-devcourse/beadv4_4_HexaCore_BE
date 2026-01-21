package com.back.chat.adapter.out;

import com.back.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {
    boolean existsByRoomIdAndUserIdAndContent(
            Long roomId,
            Long userId,
            String content
    );

    List<ChatMessage> findByRoomIdOrderByIdDesc(
            Long roomId,
            Pageable pageable
    );

    List<ChatMessage> findByRoomIdAndIdLessThanOrderByIdDesc(
            Long roomId,
            Long cursorMessageId,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update ChatMessage m
        set m.reportCount = m.reportCount + 1
        where m.id = :chatMessageId
    """)
    int incrementReportCount(@Param("chatMessageId") Long chatMessageId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update ChatMessage m
        set m.isBlinded = true
        where m.id = :chatMessageId
          and m.isBlinded = false
          and m.reportCount >= :threshold
    """)
    int blindIfReached(@Param("chatMessageId") Long chatMessageId,
                       @Param("threshold") int threshold);

}
