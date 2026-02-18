package com.soaengry.moment.domain.feed.dto.response;

import com.soaengry.moment.domain.feed.entity.Post;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Long id,
        AuthorInfo author,
        String content,
        List<String> imageUrls,
        Integer likeCount,
        Integer commentCount,
        Boolean isLiked,
        Boolean isBookmarked,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record AuthorInfo(
            Long id,
            String nickname,
            String profileImageUrl
    ) {}

    public static PostResponse from(Post post, boolean isLiked, boolean isBookmarked) {
        return new PostResponse(
                post.getId(),
                new AuthorInfo(
                        post.getUser().getId(),
                        post.getUser().getNickname(),
                        post.getUser().getProfileImageUrl()
                ),
                post.getContent(),
                post.getImages().stream().map(img -> img.getImageUrl()).toList(),
                post.getLikeCount(),
                post.getCommentCount(),
                isLiked,
                isBookmarked
        );
    }
}
