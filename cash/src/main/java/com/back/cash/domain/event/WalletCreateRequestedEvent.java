package com.back.cash.domain.event;

import com.back.common.event.EventName;

public record WalletCreateRequestedEvent(
         Long userId
) implements EventName {
}
