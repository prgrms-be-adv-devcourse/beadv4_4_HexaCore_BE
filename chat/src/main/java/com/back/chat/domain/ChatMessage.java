package com.back.chat.domain;

import com.back.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_status", nullable = false)
    private MessageStatus messageStatus = MessageStatus.NORMAL;

    @Column(name = "report_count", nullable = false)
    private int reportCount = 0;

    private ChatMessage(
            Long roomId,
            Long userId,
            String content
    ) {
        this.roomId = roomId;
        this.userId = userId;
        this.content = content;
        this.messageStatus = MessageStatus.NORMAL;
    }

    public static ChatMessage create(
            Long roomId,
            Long userId,
            String content
    ) {
        return new ChatMessage(roomId, userId, content);
    }

}
