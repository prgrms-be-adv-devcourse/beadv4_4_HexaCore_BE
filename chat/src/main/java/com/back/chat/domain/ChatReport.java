package com.back.chat.domain;

import com.back.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "chat_report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_report_message_reporter",
                        columnNames = {"chat_message_id", "reporter_user_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_message_id", nullable = false)
    private ChatMessage chatMessage;

    @Column(name = "reporter_user_id")
    private Long reporterUserId;

    @Column(name = "reported_user_id")
    private Long reportedUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason")
    private ChatReportReason reason;

    private ChatReport(ChatMessage chatMessage, Long reporterUserId, Long reportedUserId, ChatReportReason reason) {
        this.chatMessage = chatMessage;
        this.reporterUserId = reporterUserId;
        this.reportedUserId = reportedUserId;
        this.reason = reason;
    }

    public static ChatReport create(ChatMessage chatMessage, Long reporterUserId, Long reportedUserId, ChatReportReason reason) {
        return new ChatReport(chatMessage, reporterUserId, reportedUserId, reason);
    }
}



