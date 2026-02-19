package com.back.chat.adapter.out.repository;

import com.back.chat.domain.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {
    Optional<ChatRoom> findByBrandId(Long brandId);

    @Modifying
    @Query(value = """
        INSERT INTO chat_room (brand_id, created_at, updated_at)
        VALUES (:brandId, now(), now())
        ON CONFLICT (brand_id) DO NOTHING
        """, nativeQuery = true)
    int insertIfNotExists(@Param("brandId") Long brandId);
}
