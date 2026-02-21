package com.soaengry.moment.domain.feed.repository;

import com.soaengry.moment.domain.feed.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
}
