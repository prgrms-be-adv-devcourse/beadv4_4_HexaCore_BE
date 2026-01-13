package com.back.notification.domain;

import com.back.common.entity.MongoBaseEntity;
import com.back.notification.domain.enums.Type;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "notifications")
public class Notification extends MongoBaseEntity {

    @Id
    private String id;

    private Long userId;

    private Type type;

    private Map<String, Object> content;

    private String deepLink;

    private boolean isRead;

}
