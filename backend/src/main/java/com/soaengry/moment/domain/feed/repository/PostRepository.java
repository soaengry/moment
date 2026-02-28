package com.soaengry.moment.domain.feed.repository;

import com.soaengry.moment.domain.feed.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.images ORDER BY p.createdAt DESC")
    Page<Post> findAllWithUserAndImages(Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.images WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    Page<Post> findByUserIdWithImages(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.images WHERE p.weddingId = :weddingId ORDER BY p.createdAt DESC")
    Page<Post> findByWeddingIdWithUserAndImages(@Param("weddingId") Long weddingId, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.images WHERE p.user.id = :userId AND (:weddingId IS NULL OR p.weddingId = :weddingId) ORDER BY p.createdAt DESC")
    Page<Post> findByUserIdAndOptionalWeddingId(@Param("userId") Long userId, @Param("weddingId") Long weddingId, Pageable pageable);
}
