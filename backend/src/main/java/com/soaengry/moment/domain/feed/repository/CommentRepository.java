package com.soaengry.moment.domain.feed.repository;

import com.soaengry.moment.domain.feed.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId ORDER BY c.createdAt ASC")
    Page<Comment> findByPostIdWithUser(@Param("postId") Long postId, Pageable pageable);
}
