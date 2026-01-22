package com.back.chat.adapter.out;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "userClient", url = "${user-service.url}")
public interface UserClient {

    @PostMapping("/internal/users/{userId}/blind-count:increment")
    void incrementBlindCount(
            @PathVariable("userId") Long userId,
            @RequestParam("messageId") Long messageId
    );

    @GetMapping("/internal/users/{userId}/chat-restricted")
    LocalDateTime getChatRestrictedUntil(@PathVariable("userId") Long userId);
}
