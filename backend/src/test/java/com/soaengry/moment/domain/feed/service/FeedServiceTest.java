package com.soaengry.moment.domain.feed.service;

import com.soaengry.moment.domain.feed.dto.request.CommentRequest;
import com.soaengry.moment.domain.feed.dto.request.PostRequest;
import com.soaengry.moment.domain.feed.dto.response.CommentResponse;
import com.soaengry.moment.domain.feed.dto.response.PostResponse;
import com.soaengry.moment.domain.feed.entity.Post;
import com.soaengry.moment.domain.feed.exception.FeedErrorCode;
import com.soaengry.moment.domain.feed.exception.FeedException;
import com.soaengry.moment.domain.feed.repository.*;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class FeedServiceTest {

    @Autowired
    private FeedService feedService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @Autowired
    private com.soaengry.moment.domain.guestbook.repository.GuestbookEntryRepository guestbookEntryRepository;

    @Autowired
    private com.soaengry.moment.domain.attendance.repository.AttendanceRepository attendanceRepository;

    @BeforeEach
    void setUp() {
        // 테스트 전 데이터 정리 (FK 제약 조건 순서 고려)
        commentRepository.deleteAll();
        postLikeRepository.deleteAll();
        bookmarkRepository.deleteAll();
        postImageRepository.deleteAll();
        postRepository.deleteAll();
        guestbookEntryRepository.deleteAll();
        attendanceRepository.deleteAll();
        userRepository.deleteAll();

        // 사용자 생성
        user1 = User.builder()
                .email("user1@example.com")
                .password("password")
                .nickname("사용자1")
                .isEmailVerified(true)
                .build();
        user1 = userRepository.saveAndFlush(user1);

        user2 = User.builder()
                .email("user2@example.com")
                .password("password")
                .nickname("사용자2")
                .isEmailVerified(true)
                .build();
        user2 = userRepository.saveAndFlush(user2);
    }

    // ==================== Post CRUD (13 tests) ====================

    @Test
    @DisplayName("게시글 작성")
    void createPost_Success() {
        // given
        PostRequest request = new PostRequest("첫 번째 게시글", null);

        // when
        PostResponse result = feedService.createPost(user1.getId(), request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("첫 번째 게시글");
        assertThat(result.author().id()).isEqualTo(user1.getId());
        assertThat(result.author().nickname()).isEqualTo("사용자1");

        System.out.println("✅ 게시글 작성 성공");
    }

    @Test
    @DisplayName("게시글 작성 (이미지 포함)")
    void createPost_WithImages_Success() {
        // given
        PostRequest request = new PostRequest(
                "이미지 포함 게시글",
                List.of("https://s3.amazonaws.com/img1.jpg", "https://s3.amazonaws.com/img2.jpg")
        );

        // when
        PostResponse result = feedService.createPost(user1.getId(), request);

        // then
        assertThat(result.imageUrls()).hasSize(2);
        assertThat(result.imageUrls()).contains("https://s3.amazonaws.com/img1.jpg", "https://s3.amazonaws.com/img2.jpg");

        System.out.println("✅ 게시글 작성 성공 (이미지 포함)");
    }

    @Test
    @DisplayName("게시글 작성 실패 - 사용자 없음")
    void createPost_UserNotFound_Fail() {
        // given
        PostRequest request = new PostRequest("내용", null);

        // when & then
        assertThatThrownBy(() -> feedService.createPost(999L, request))
                .isInstanceOf(FeedException.class)
                .hasMessage(FeedErrorCode.UNAUTHORIZED_ACCESS.getMessage());

        System.out.println("✅ 사용자 없음 테스트 통과");
    }

    @Test
    @DisplayName("피드 조회 (페이징)")
    void getFeed_Pageable_Success() {
        // given
        feedService.createPost(user1.getId(), new PostRequest("게시글 1", null));
        feedService.createPost(user2.getId(), new PostRequest("게시글 2", null));

        // when
        Page<PostResponse> result = feedService.getFeed(user1.getId(), PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);

        System.out.println("✅ 피드 조회 성공 (페이징)");
    }

    @Test
    @DisplayName("빈 피드")
    void getFeed_Empty_Success() {
        // when
        Page<PostResponse> result = feedService.getFeed(user1.getId(), PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).isEmpty();

        System.out.println("✅ 빈 피드 조회 성공");
    }

    @Test
    @DisplayName("특정 사용자 게시글 조회")
    void getUserPosts_Success() {
        // given
        feedService.createPost(user1.getId(), new PostRequest("user1 게시글 1", null));
        feedService.createPost(user1.getId(), new PostRequest("user1 게시글 2", null));
        feedService.createPost(user2.getId(), new PostRequest("user2 게시글", null));

        // when
        Page<PostResponse> result = feedService.getUserPosts(user1.getId(), user1.getId(), PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(p -> p.author().id().equals(user1.getId()));

        System.out.println("✅ 특정 사용자 게시글 조회 성공");
    }

    @Test
    @DisplayName("게시글 상세 조회")
    void getPost_Success() {
        // given
        PostResponse post = feedService.createPost(user1.getId(), new PostRequest("상세 조회 테스트", null));

        // when
        PostResponse result = feedService.getPost(post.id(), user1.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("상세 조회 테스트");

        System.out.println("✅ 게시글 상세 조회 성공");
    }

    @Test
    @DisplayName("게시글 조회 실패 - Not Found")
    void getPost_NotFound_Fail() {
        // when & then
        assertThatThrownBy(() -> feedService.getPost(999L, user1.getId()))
                .isInstanceOf(FeedException.class)
                .hasMessage(FeedErrorCode.POST_NOT_FOUND.getMessage());

        System.out.println("✅ 게시글 Not Found 테스트 통과");
    }

    @Test
    @DisplayName("게시글 수정")
    void updatePost_Success() {
        // given
        PostResponse post = feedService.createPost(user1.getId(), new PostRequest("원본 내용", null));
        PostRequest updateRequest = new PostRequest("수정된 내용", null);

        // when
        PostResponse result = feedService.updatePost(user1.getId(), post.id(), updateRequest);

        // then
        assertThat(result.content()).isEqualTo("수정된 내용");

        System.out.println("✅ 게시글 수정 성공");
    }

    @Test
    @DisplayName("게시글 수정 - 이미지 교체")
    void updatePost_ReplaceImages_Success() {
        // given
        PostResponse post = feedService.createPost(user1.getId(),
                new PostRequest("원본", List.of("https://s3.amazonaws.com/old.jpg")));

        PostRequest updateRequest = new PostRequest(
                "수정",
                List.of("https://s3.amazonaws.com/new1.jpg", "https://s3.amazonaws.com/new2.jpg")
        );

        // when
        PostResponse result = feedService.updatePost(user1.getId(), post.id(), updateRequest);

        // then
        assertThat(result.imageUrls()).hasSize(2);
        assertThat(result.imageUrls()).doesNotContain("https://s3.amazonaws.com/old.jpg");
        assertThat(result.imageUrls()).contains("https://s3.amazonaws.com/new1.jpg", "https://s3.amazonaws.com/new2.jpg");

        System.out.println("✅ 게시글 이미지 교체 성공");
    }

    @Test
    @DisplayName("게시글 수정 실패 - 권한 없음")
    void updatePost_Unauthorized_Fail() {
        // given
        PostResponse post = feedService.createPost(user1.getId(), new PostRequest("내용", null));
        PostRequest updateRequest = new PostRequest("수정 시도", null);

        // when & then
        assertThatThrownBy(() -> feedService.updatePost(user2.getId(), post.id(), updateRequest))
                .isInstanceOf(FeedException.class)
                .hasMessage(FeedErrorCode.UNAUTHORIZED_ACCESS.getMessage());

        System.out.println("✅ 권한 없음 테스트 통과");
    }

    @Test
    @DisplayName("게시글 삭제")
    void deletePost_Success() {
        // given
        PostResponse post = feedService.createPost(user1.getId(), new PostRequest("삭제할 게시글", null));

        // when
        feedService.deletePost(user1.getId(), post.id());

        // then
        assertThat(postRepository.existsById(post.id())).isFalse();

        System.out.println("✅ 게시글 삭제 성공");
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 권한 없음")
    void deletePost_Unauthorized_Fail() {
        // given
        PostResponse post = feedService.createPost(user1.getId(), new PostRequest("내용", null));

        // when & then
        assertThatThrownBy(() -> feedService.deletePost(user2.getId(), post.id()))
                .isInstanceOf(FeedException.class)
                .hasMessage(FeedErrorCode.UNAUTHORIZED_ACCESS.getMessage());

        System.out.println("✅ 삭제 권한 없음 테스트 통과");
    }

    // ==================== Like/Bookmark (8 tests) ====================

    @Test
    @DisplayName("좋아요 추가")
    void toggleLike_Add_Success() {
        // given
        PostResponse post = feedService.createPost(user1.getId(), new PostRequest("내용", null));

        // when
        boolean result = feedService.toggleLike(user2.getId(), post.id());

        // then
        assertThat(result).isTrue();
        assertThat(postLikeRepository.existsByPostIdAndUserId(post.id(), user2.getId())).isTrue();

        System.out.println("✅ 좋아요 추가 성공");
    }

    @Test
    @DisplayName("좋아요 취소")
    void toggleLike_Remove_Success() {
        // given
        PostResponse post = feedService.createPost(user1.getId(), new PostRequest("내용", null));
        feedService.toggleLike(user2.getId(), post.id()); // 추가

        // when
        boolean result = feedService.toggleLike(user2.getId(), post.id()); // 취소

        // then
        assertThat(result).isFalse();
        assertThat(postLikeRepository.existsByPostIdAndUserId(post.id(), user2.getId())).isFalse();

        System.out.println("✅ 좋아요 취소 성공");
    }

    @Test
    @DisplayName("좋아요 실패 - 게시글 없음")
    void toggleLike_PostNotFound_Fail() {
        // when & then
        assertThatThrownBy(() -> feedService.toggleLike(user1.getId(), 999L))
                .isInstanceOf(FeedException.class)
                .hasMessage(FeedErrorCode.POST_NOT_FOUND.getMessage());

        System.out.println("✅ 게시글 없음 테스트 통과");
    }

    @Test
    @DisplayName("좋아요 카운트 증가 확인")
    void toggleLike_IncrementCount_Success() {
        // given
        PostResponse post = feedService.createPost(user1.getId(), new PostRequest("내용", null));

        // when
        feedService.toggleLike(user2.getId(), post.id());

        // then
        Post updatedPost = postRepository.findById(post.id()).orElseThrow();
        assertThat(updatedPost.getLikeCount()).isEqualTo(1);

        System.out.println("✅ 좋아요 카운트 증가 확인");
    }

    @Test
    @DisplayName("북마크 추가")
    void toggleBookmark_Add_Success() {
        // given
        PostResponse post = feedService.createPost(user1.getId(), new PostRequest("내용", null));

        // when
        boolean result = feedService.toggleBookmark(user2.getId(), post.id());

        // then
        assertThat(result).isTrue();
        assertThat(bookmarkRepository.existsByPostIdAndUserId(post.id(), user2.getId())).isTrue();

        System.out.println("✅ 북마크 추가 성공");
    }

    @Test
    @DisplayName("북마크 취소")
    void toggleBookmark_Remove_Success() {
        // given
        PostResponse post = feedService.createPost(user1.getId(), new PostRequest("내용", null));
        feedService.toggleBookmark(user2.getId(), post.id()); // 추가

        // when
        boolean result = feedService.toggleBookmark(user2.getId(), post.id()); // 취소

        // then
        assertThat(result).isFalse();
        assertThat(bookmarkRepository.existsByPostIdAndUserId(post.id(), user2.getId())).isFalse();

        System.out.println("✅ 북마크 취소 성공");
    }

    @Test
    @DisplayName("북마크 실패 - 게시글 없음")
    void toggleBookmark_PostNotFound_Fail() {
        // when & then
        assertThatThrownBy(() -> feedService.toggleBookmark(user1.getId(), 999L))
                .isInstanceOf(FeedException.class)
                .hasMessage(FeedErrorCode.POST_NOT_FOUND.getMessage());

        System.out.println("✅ 북마크 게시글 없음 테스트 통과");
    }

    @Test
    @DisplayName("북마크한 게시글 목록 조회")
    void getBookmarkedPosts_Success() {
        // given
        PostResponse post1 = feedService.createPost(user1.getId(), new PostRequest("게시글 1", null));
        PostResponse post2 = feedService.createPost(user1.getId(), new PostRequest("게시글 2", null));
        feedService.toggleBookmark(user2.getId(), post1.id());
        feedService.toggleBookmark(user2.getId(), post2.id());

        // when
        Page<PostResponse> result = feedService.getBookmarkedPosts(user2.getId(), PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(PostResponse::isBookmarked);

        System.out.println("✅ 북마크한 게시글 목록 조회 성공");
    }

    // ==================== Comment (8 tests) ====================

    @Test
    @DisplayName("댓글 작성")
    void createComment_Success() {
        // given
        PostResponse post = feedService.createPost(user1.getId(), new PostRequest("게시글", null));
        CommentRequest request = new CommentRequest("댓글 내용");

        // when
        CommentResponse result = feedService.createComment(user2.getId(), post.id(), request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("댓글 내용");
        assertThat(result.author().id()).isEqualTo(user2.getId());

        System.out.println("✅ 댓글 작성 성공");
    }

    @Test
    @DisplayName("댓글 작성 실패 - 게시글 없음")
    void createComment_PostNotFound_Fail() {
        // given
        CommentRequest request = new CommentRequest("댓글");

        // when & then
        assertThatThrownBy(() -> feedService.createComment(user1.getId(), 999L, request))
                .isInstanceOf(FeedException.class)
                .hasMessage(FeedErrorCode.POST_NOT_FOUND.getMessage());

        System.out.println("✅ 댓글 작성 실패 테스트 통과");
    }

    @Test
    @DisplayName("댓글 카운트 증가 확인")
    void createComment_IncrementCount_Success() {
        // given
        PostResponse post = feedService.createPost(user1.getId(), new PostRequest("게시글", null));
        CommentRequest request = new CommentRequest("댓글");

        // when
        feedService.createComment(user2.getId(), post.id(), request);

        // then
        Post updatedPost = postRepository.findById(post.id()).orElseThrow();
        assertThat(updatedPost.getCommentCount()).isEqualTo(1);

        System.out.println("✅ 댓글 카운트 증가 확인");
    }

    @Test
    @DisplayName("댓글 목록 조회")
    void getComments_Success() {
        // given
        PostResponse post = feedService.createPost(user1.getId(), new PostRequest("게시글", null));
        feedService.createComment(user2.getId(), post.id(), new CommentRequest("댓글 1"));
        feedService.createComment(user2.getId(), post.id(), new CommentRequest("댓글 2"));

        // when
        Page<CommentResponse> result = feedService.getComments(post.id(), PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);

        System.out.println("✅ 댓글 목록 조회 성공");
    }

    @Test
    @DisplayName("빈 댓글 목록")
    void getComments_Empty_Success() {
        // given
        PostResponse post = feedService.createPost(user1.getId(), new PostRequest("게시글", null));

        // when
        Page<CommentResponse> result = feedService.getComments(post.id(), PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).isEmpty();

        System.out.println("✅ 빈 댓글 목록 조회 성공");
    }

    @Test
    @DisplayName("댓글 수정")
    void updateComment_Success() {
        // given
        PostResponse post = feedService.createPost(user1.getId(), new PostRequest("게시글", null));
        CommentResponse comment = feedService.createComment(user2.getId(), post.id(), new CommentRequest("원본"));
        CommentRequest updateRequest = new CommentRequest("수정됨");

        // when
        CommentResponse result = feedService.updateComment(user2.getId(), comment.id(), updateRequest);

        // then
        assertThat(result.content()).isEqualTo("수정됨");

        System.out.println("✅ 댓글 수정 성공");
    }

    @Test
    @DisplayName("댓글 수정 실패 - 권한 없음")
    void updateComment_Unauthorized_Fail() {
        // given
        PostResponse post = feedService.createPost(user1.getId(), new PostRequest("게시글", null));
        CommentResponse comment = feedService.createComment(user2.getId(), post.id(), new CommentRequest("댓글"));
        CommentRequest updateRequest = new CommentRequest("수정 시도");

        // when & then
        assertThatThrownBy(() -> feedService.updateComment(user1.getId(), comment.id(), updateRequest))
                .isInstanceOf(FeedException.class)
                .hasMessage(FeedErrorCode.UNAUTHORIZED_ACCESS.getMessage());

        System.out.println("✅ 댓글 수정 권한 없음 테스트 통과");
    }

    @Test
    @DisplayName("댓글 삭제 + 카운트 감소")
    void deleteComment_DecrementCount_Success() {
        // given
        PostResponse post = feedService.createPost(user1.getId(), new PostRequest("게시글", null));
        CommentResponse comment = feedService.createComment(user2.getId(), post.id(), new CommentRequest("댓글"));

        // when
        feedService.deleteComment(user2.getId(), comment.id());

        // then
        assertThat(commentRepository.existsById(comment.id())).isFalse();

        Post updatedPost = postRepository.findById(post.id()).orElseThrow();
        assertThat(updatedPost.getCommentCount()).isEqualTo(0);

        System.out.println("✅ 댓글 삭제 + 카운트 감소 성공");
    }

    // ==================== My Page (6 tests) ====================

    @Test
    @DisplayName("내 게시글 목록")
    void getMyPosts_Success() {
        // given
        feedService.createPost(user1.getId(), new PostRequest("내 게시글 1", null));
        feedService.createPost(user1.getId(), new PostRequest("내 게시글 2", null));
        feedService.createPost(user2.getId(), new PostRequest("타인 게시글", null));

        // when
        Page<PostResponse> result = feedService.getMyPosts(user1.getId(), null, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(p -> p.author().id().equals(user1.getId()));

        System.out.println("✅ 내 게시글 목록 조회 성공");
    }

    @Test
    @DisplayName("내 게시글 필터링 (eventId=null이면 전체)")
    void getMyPosts_FilteredByEvent_Null_Success() {
        // given
        feedService.createPost(user1.getId(), new PostRequest("내 게시글 1", null));
        feedService.createPost(user1.getId(), new PostRequest("내 게시글 2", null));
        feedService.createPost(user2.getId(), new PostRequest("타인 게시글", null));

        // when - eventId=null → 전체 조회
        Page<PostResponse> result = feedService.getMyPosts(user1.getId(), null, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(p -> p.author().id().equals(user1.getId()));

        System.out.println("✅ 내 게시글 전체 조회 성공 (eventId=null)");
    }

    @Test
    @DisplayName("내 북마크 목록")
    void getMyBookmarks_Success() {
        // given
        PostResponse post1 = feedService.createPost(user2.getId(), new PostRequest("게시글 1", null));
        PostResponse post2 = feedService.createPost(user2.getId(), new PostRequest("게시글 2", null));
        feedService.toggleBookmark(user1.getId(), post1.id());
        feedService.toggleBookmark(user1.getId(), post2.id());

        // when
        Page<PostResponse> result = feedService.getMyBookmarks(user1.getId(), null, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(PostResponse::isBookmarked);

        System.out.println("✅ 내 북마크 목록 조회 성공");
    }

    @Test
    @DisplayName("내가 좋아요한 게시글")
    void getMyLikes_Success() {
        // given
        PostResponse post1 = feedService.createPost(user2.getId(), new PostRequest("게시글 1", null));
        PostResponse post2 = feedService.createPost(user2.getId(), new PostRequest("게시글 2", null));
        feedService.toggleLike(user1.getId(), post1.id());
        feedService.toggleLike(user1.getId(), post2.id());

        // when
        Page<PostResponse> result = feedService.getMyLikes(user1.getId(), null, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(PostResponse::isLiked);

        System.out.println("✅ 내가 좋아요한 게시글 조회 성공");
    }

    @Test
    @DisplayName("내 댓글 목록")
    void getMyComments_Success() {
        // given
        PostResponse post1 = feedService.createPost(user2.getId(), new PostRequest("게시글 1", null));
        PostResponse post2 = feedService.createPost(user2.getId(), new PostRequest("게시글 2", null));
        feedService.createComment(user1.getId(), post1.id(), new CommentRequest("댓글 1"));
        feedService.createComment(user1.getId(), post2.id(), new CommentRequest("댓글 2"));

        // when
        Page<CommentResponse> result = feedService.getMyComments(user1.getId(), null, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(c -> c.author().id().equals(user1.getId()));

        System.out.println("✅ 내 댓글 목록 조회 성공");
    }

    @Test
    @DisplayName("enrichPostResponses (좋아요/북마크 상태 배치 로드)")
    void enrichPostResponses_Success() {
        // given
        PostResponse post1 = feedService.createPost(user1.getId(), new PostRequest("게시글 1", null));
        PostResponse post2 = feedService.createPost(user1.getId(), new PostRequest("게시글 2", null));
        feedService.toggleLike(user2.getId(), post1.id());
        feedService.toggleBookmark(user2.getId(), post2.id());

        // when
        Page<PostResponse> result = feedService.getFeed(user2.getId(), PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
        PostResponse likedPost = result.getContent().stream()
                .filter(p -> p.id().equals(post1.id())).findFirst().orElseThrow();
        PostResponse bookmarkedPost = result.getContent().stream()
                .filter(p -> p.id().equals(post2.id())).findFirst().orElseThrow();

        assertThat(likedPost.isLiked()).isTrue();
        assertThat(bookmarkedPost.isBookmarked()).isTrue();

        System.out.println("✅ enrichPostResponses 배치 로드 성공");
    }
}
