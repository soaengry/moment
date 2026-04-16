import { type FC, useState } from "react";
import { useNavigate } from "react-router-dom";
import { IoArrowBack } from "react-icons/io5";
import { feedApi } from "../../feed/api/feedApi";
import type { PostResponse } from "../../feed/types";
import PostCard from "../../feed/components/PostCard";
import CommentSheet from "../../feed/components/CommentSheet";
import { useAuthStore } from "../../auth/store/useAuthStore";
import { useScrollVisibility } from "../../../global/hooks/useScrollVisibility";
import { usePaginatedPosts } from "../hooks/usePaginatedPosts";

const MyPostsPage: FC = () => {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const headerVisible = useScrollVisibility();
  const [commentPostId, setCommentPostId] = useState<number | null>(null);

  const { posts, page, hasMore, isLoading, fetchPosts, setPosts } =
    usePaginatedPosts(feedApi.getMyPosts, "게시글을 불러오지 못했습니다.");

  const handleCommentCountChange = (postId: number, delta: number) => {
    setPosts((prev) =>
      prev.map((p) =>
        p.id === postId ? { ...p, commentCount: p.commentCount + delta } : p,
      ),
    );
  };

  return (
    <div className="max-w-lg mx-auto min-h-screen bg-[#faf9f6]">
      <header className={`sticky top-0 z-20 bg-white/80 backdrop-blur-md border-b border-gray-50 transition-transform duration-300 ${headerVisible ? "translate-y-0" : "-translate-y-full"}`}>
        <div className="flex items-center gap-3 px-4 py-3">
          <button onClick={() => navigate(-1)} className="text-gray-600">
            <IoArrowBack size={22} />
          </button>
          <h1 className="text-base font-semibold text-gray-800">내가 작성한 게시글</h1>
        </div>
      </header>

      <div>
        {posts.map((post: PostResponse) => (
          <PostCard
            key={post.id}
            post={post}
            currentUserId={user?.id}
            onPostDeleted={() => fetchPosts(0)}
            onCommentClick={setCommentPostId}
          />
        ))}
      </div>

      {hasMore && (
        <button
          onClick={() => fetchPosts(page + 1, true)}
          disabled={isLoading}
          className="w-full py-4 text-xs text-gray-400"
        >
          {isLoading ? "불러오는 중..." : "더보기"}
        </button>
      )}

      {posts.length === 0 && !isLoading && (
        <div className="text-center py-20">
          <p className="text-sm text-gray-300">작성한 게시글이 없습니다</p>
        </div>
      )}

      <div className="h-20" />

      {commentPostId !== null && (
        <CommentSheet
          postId={commentPostId}
          isOpen={true}
          onClose={() => setCommentPostId(null)}
          currentUserId={user?.id}
          onCommentCountChange={handleCommentCountChange}
        />
      )}
    </div>
  );
};

export default MyPostsPage;
