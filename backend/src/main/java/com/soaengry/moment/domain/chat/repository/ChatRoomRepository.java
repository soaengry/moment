package com.soaengry.moment.domain.chat.repository;

import com.soaengry.moment.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findByWeddingIdOrderByCreatedAtAsc(Long weddingId);
}
