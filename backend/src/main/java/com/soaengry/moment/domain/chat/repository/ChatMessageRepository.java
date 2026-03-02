package com.soaengry.moment.domain.chat.repository;

import com.soaengry.moment.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    Page<ChatMessage> findByWeddingIdOrderByCreatedAtDesc(Long weddingId, Pageable pageable);
}
