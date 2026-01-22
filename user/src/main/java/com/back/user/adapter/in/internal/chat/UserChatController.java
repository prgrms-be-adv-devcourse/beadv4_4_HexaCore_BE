package com.back.user.adapter.in.internal.chat;

import com.back.user.app.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/chat/users")
@Tag(
        name = "User Internal - Chat",
        description = "Chat 모듈에서 호출하는 API"
)
public class UserChatController {

    private final UserFacade userFacade;

    @Operation(
            summary = "User blindCount 증가",
            description = """
                    Chat 서비스에서 사용자 메시지가 블라인드 처리될 때 호출되는 내부 API입니다.
                    
                    - 사용자의 blindCount를 1 증가시킵니다.
                    - blindCount가 임계치에 도달하면 채팅 제한이 적용됩니다.
                    """
    )
    @PostMapping("/{userId}/blind-count:increase")
    public void incrementBlindCount (@PathVariable Long userId){
        userFacade.incrementBlindCount(userId);
    }
}
