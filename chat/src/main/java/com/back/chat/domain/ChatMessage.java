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

    @Column(name = "is_blinded", nullable = false)
    private boolean isBlinded = false;

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
        this.isBlinded = false;
    }

    public static ChatMessage create(
            Long roomId,
            Long userId,
            String content
    ) {
        return new ChatMessage(roomId, userId, content);
    }

}
