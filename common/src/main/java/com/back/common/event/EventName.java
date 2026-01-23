package com.back.common.event;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface EventName {
    @JsonIgnore
    default String getEventName() {
        return this.getClass().getSimpleName();
    }
}
