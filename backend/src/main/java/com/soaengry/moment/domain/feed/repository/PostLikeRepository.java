package com.soaengry.moment.domain.feed.repository;

import com.soaengry.moment.domain.feed.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    List<PostLike> findByUserIdAndPostIdIn(Long userId, List<Long> postIds);
}
