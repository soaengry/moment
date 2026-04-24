package com.soaengry.moment.domain.feed.repository;

import com.soaengry.moment.domain.feed.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId " +
           "AND (:cursor IS NULL OR c.id > :cursor) ORDER BY c.id ASC")
    List<Comment> findByPostIdCursor(@Param("postId") Long postId, @Param("cursor") Long cursor, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user JOIN FETCH c.post WHERE c.user.id = :userId " +
           "AND (:eventId IS NULL OR c.post.eventId = :eventId) AND (:cursor IS NULL OR c.id < :cursor) ORDER BY c.id DESC")
    List<Comment> findByUserIdAndOptionalEventIdCursor(@Param("userId") Long userId, @Param("eventId") Long eventId, @Param("cursor") Long cursor, Pageable pageable);
}
