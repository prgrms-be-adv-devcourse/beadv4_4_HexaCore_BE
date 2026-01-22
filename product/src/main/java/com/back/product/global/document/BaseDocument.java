package com.back.product.global.document;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class BaseDocument<ID> implements Persistable<ID> {
    @Id
    private ID id;

    @Field(
            type = FieldType.Date,
            format = DateFormat.date_time
    )
    @CreatedDate
    private LocalDateTime createdAt;

    @Field(
            type = FieldType.Date,
            format =  DateFormat.date_time
    )
    @LastModifiedDate
    private LocalDateTime modifiedAt;

    @Override
    public boolean isNew() {
        return id == null || (createdAt == null && modifiedAt == null);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BaseDocument<?> that = (BaseDocument<?>) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }
}
