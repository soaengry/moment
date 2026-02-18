package com.soaengry.moment.domain.chat.repository;

import com.soaengry.moment.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m JOIN FETCH m.user WHERE m.chatRoom.id = :roomId ORDER BY m.createdAt DESC")
    Page<ChatMessage> findByRoomIdWithUser(@Param("roomId") Long roomId, Pageable pageable);
}
