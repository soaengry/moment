import { type FC, useState, useEffect, useCallback } from "react";
import { feedApi } from "../api/feedApi";
import { handleApiError } from "../../../global/utils/errorHandler";
import type { PostResponse } from "../types";
import PostCard from "./PostCard";
import PostComposer from "./PostComposer";
import CommentSheet from "./CommentSheet";
import { useAuthStore } from "../../auth/store/useAuthStore";
import { attendanceApi } from "../../attendance/api/attendanceApi";
import { IoClose } from "react-icons/io5";

interface Props {
  eventId: number;
  eventOwnerId: number;
}

const WeddingFeedTab: FC<Props> = ({ eventId, eventOwnerId }) => {
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [nextCursor, setNextCursor] = useState<number | undefined>(undefined);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [commentPostId, setCommentPostId] = useState<number | null>(null);
  const [editingPost, setEditingPost] = useState<PostResponse | null>(null);
  const [isParticipant, setIsParticipant] = useState(false);

  const user = useAuthStore((s) => s.user);

  useEffect(() => {
    if (!user) { setIsParticipant(false); return; }
    attendanceApi.getMyAttendances()
      .then((list) => setIsParticipant(list.some((a) => a.eventId === eventId)))
      .catch(() => setIsParticipant(false));
  }, [user, eventId]);

  const canPost = !!user && (user.id === eventOwnerId || isParticipant);

  const fetchPosts = useCallback(async (cursor?: number, append = false) => {
    setIsLoading(true);
    try {
      const res = await feedApi.getEventFeed(eventId, cursor);
      if (append) {
        setPosts((prev) => [...prev, ...res.content]);
      } else {
        setPosts(res.content);
      }
      setNextCursor(res.nextCursor ?? undefined);
      setHasMore(res.hasNext);
    } catch (error) {
      handleApiError(error, "게시글을 불러오지 못했습니다.");
    } finally { setIsLoading(false); }
  }, [eventId]);

  useEffect(() => {
    fetchPosts(undefined);
  }, [fetchPosts]);

  const handleRefresh = () => {
    setEditingPost(null);
    fetchPosts(undefined);
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
      {/* Composer — 참석자 또는 주최자만 */}
      {canPost && (
        <PostComposer
          eventId={eventId}
          onPostCreated={handleRefresh}
        />
      )}

      {/* Edit Modal */}
      {editingPost && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="w-full max-w-lg bg-white rounded-2xl shadow-xl">
            <div className="flex items-center justify-between px-4 pt-4 pb-2 border-b border-gray-100">
              <h2 className="text-sm font-semibold text-gray-800">게시글 수정</h2>
              <button onClick={() => setEditingPost(null)} className="p-1 text-gray-400">
                <IoClose size={20} />
              </button>
            </div>
            <PostComposer
              eventId={eventId}
              editingPost={editingPost}
              onPostUpdated={handleRefresh}
              onCancel={() => setEditingPost(null)}
            />
          </div>
        </div>
      )}

      {/* Posts */}
      <div>
        {posts.map((post) => (
          <PostCard
            key={post.id}
            post={post}
            currentUserId={user?.id}
            onPostDeleted={handleRefresh}
            onCommentClick={setCommentPostId}
            onPostUpdated={setEditingPost}
          />
        ))}
      </div>

      {/* Load more */}
      {hasMore && (
        <button
          onClick={() => nextCursor != null && fetchPosts(nextCursor, true)}
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
          onCommentCountChange={handleCommentCountChange}
        />
      )}
    </div>
  );
};

export default WeddingFeedTab;
