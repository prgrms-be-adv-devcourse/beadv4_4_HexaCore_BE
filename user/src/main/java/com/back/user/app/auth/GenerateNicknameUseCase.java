package com.back.user.app.auth;

import com.back.user.adapter.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenerateNicknameUseCase {
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    private static final List<String> ANIMALS = List.of("고양이","강아지","판다","수달","여우","호랑이","토끼","기니피그");

    public String generateUnique() {
        for (int i = 0; i < 10; i++) {
            String animal = ANIMALS.get(random.nextInt(ANIMALS.size()));
            String num = String.format("%04d", random.nextInt(10000));
            String uuid4 = UUID.randomUUID().toString().replace("-", "").substring(0, 4);

            String candidate = animal + "-" + num + "-" + uuid4;

            if (!userRepository.existsByNickname(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("닉네임 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }
}

