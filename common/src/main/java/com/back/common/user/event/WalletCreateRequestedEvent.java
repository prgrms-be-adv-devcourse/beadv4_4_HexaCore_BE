package com.back.common.user.event;

import com.back.common.event.EventName;

public record WalletCreateRequestedEvent(
         Long userId
) implements EventName {
}
