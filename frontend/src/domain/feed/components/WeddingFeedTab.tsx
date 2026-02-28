import { type FC, useState, useEffect, useCallback } from "react";
import { feedApi } from "../api/feedApi";
import type { PostResponse } from "../types";
import PostCard from "./PostCard";
import PostComposer from "./PostComposer";
import CommentSheet from "./CommentSheet";
import { useAuthStore } from "../../auth/store/useAuthStore";

interface Props {
  weddingId: number;
}

const WeddingFeedTab: FC<Props> = ({ weddingId }) => {
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [commentPostId, setCommentPostId] = useState<number | null>(null);
  const [editingPost, setEditingPost] = useState<PostResponse | null>(null);

  const user = useAuthStore((s) => s.user);

  const fetchPosts = useCallback(async (pageNum: number, append = false) => {
    setIsLoading(true);
    try {
      const res = await feedApi.getWeddingFeed(weddingId, pageNum);
      if (append) {
        setPosts((prev) => [...prev, ...res.content]);
      } else {
        setPosts(res.content);
      }
      setHasMore(!res.last);
      setPage(pageNum);
    } catch { /* silent */ }
    finally { setIsLoading(false); }
  }, [weddingId]);

  useEffect(() => {
    fetchPosts(0);
  }, [fetchPosts]);

  const handleRefresh = () => {
    setEditingPost(null);
    fetchPosts(0);
  };

  const handleCommentCountChange = (postId: number, delta: number) => {
    setPosts((prev) =>
      prev.map((p) =>
        p.id === postId ? { ...p, commentCount: p.commentCount + delta } : p,
      ),
    );
  };

  return (
    <div>
      {/* Composer — 로그인한 유저만 */}
      {user && (
        <PostComposer
          weddingId={weddingId}
          onPostCreated={handleRefresh}
          editingPost={editingPost}
          onPostUpdated={handleRefresh}
          onCancel={() => setEditingPost(null)}
        />
      )}

      {/* Posts */}
      <div>
        {posts.map((post) => (
          <PostCard
            key={post.id}
            post={post}
            currentUserId={user?.id}
            weddingId={weddingId}
            onPostDeleted={handleRefresh}
            onCommentClick={setCommentPostId}
            onPostUpdated={setEditingPost}
          />
        ))}
      </div>

      {/* Load more */}
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
          <p className="text-sm text-gray-300">아직 게시글이 없습니다</p>
        </div>
      )}

      {/* Comment Sheet */}
      {commentPostId !== null && (
        <CommentSheet
          postId={commentPostId}
          isOpen={true}
          onClose={() => setCommentPostId(null)}
          currentUserId={user?.id}
          weddingId={weddingId}
          onCommentCountChange={handleCommentCountChange}
        />
      )}
    </div>
  );
};

export default WeddingFeedTab;
