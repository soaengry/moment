package com.soaengry.moment.domain.feed.dto.response;

import com.soaengry.moment.domain.feed.entity.Post;
import com.soaengry.moment.domain.feed.entity.PostImage;

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
        Long eventId,
        String eventSlug,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponse from(Post post, boolean isLiked, boolean isBookmarked) {
        return from(post, isLiked, isBookmarked, null);
    }

    public static PostResponse from(Post post, boolean isLiked, boolean isBookmarked, String eventSlug) {
        return new PostResponse(
                post.getId(),
                new AuthorInfo(
                        post.getUser().getId(),
                        post.getUser().getNickname(),
                        post.getUser().getProfileImageUrl()
                ),
                post.getContent(),
                post.getImages().stream().map(PostImage::getImageUrl).toList(),
                post.getLikeCount(),
                post.getCommentCount(),
                isLiked,
                isBookmarked,
                post.getEventId(),
                eventSlug,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public record AuthorInfo(
            Long id,
            String nickname,
            String profileImageUrl
    ) {
    }
}
