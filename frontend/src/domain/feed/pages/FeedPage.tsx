import { type FC, useState, useEffect, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { staggerContainer, staggerItem, modalBackdrop, modalContent } from "../../../global/constants/animations";
import { feedApi } from "../api/feedApi";
import type { PostResponse } from "../types";
import PostCard from "../components/PostCard";
import PostComposer from "../components/PostComposer";
import CommentSheet from "../components/CommentSheet";
import { useAuthStore } from "../../auth/store/useAuthStore";
import { useScrollVisibility } from "../../../global/hooks/useScrollVisibility";
import { handleApiError } from "../../../global/utils/errorHandler";
import { IoArrowBack, IoBookmarkOutline, IoClose } from "react-icons/io5";
import { useNavigate } from "react-router-dom";

const FeedPage: FC = () => {
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [activeTab, setActiveTab] = useState<"feed" | "bookmarks">("feed");
  const [commentPostId, setCommentPostId] = useState<number | null>(null);
  const [editingPost, setEditingPost] = useState<PostResponse | null>(null);

  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const headerVisible = useScrollVisibility();

  const fetchPosts = useCallback(async (pageNum: number, append = false) => {
    setIsLoading(true);
    try {
      const res = activeTab === "feed"
        ? await feedApi.getFeed(pageNum)
        : await feedApi.getBookmarks(pageNum);

      if (append) {
        setPosts((prev) => [...prev, ...res.content]);
      } else {
        setPosts(res.content);
      }
      setHasMore(!res.last);
      setPage(pageNum);
    } catch (error) {
      handleApiError(error, "게시글을 불러오지 못했습니다.");
    } finally { setIsLoading(false); }
  }, [activeTab]);

  useEffect(() => {
    fetchPosts(0);
  }, [fetchPosts]);

  const handlePostCreated = () => {
    fetchPosts(0);
  };

  const handlePostUpdated = () => {
    setEditingPost(null);
    fetchPosts(0);
  };

  const handlePostDeleted = () => {
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
    <div className="min-h-screen bg-[#faf9f6]">
      <div className="max-w-lg mx-auto">
        {/* Header */}
        <header className={`sticky top-0 z-20 bg-white/80 backdrop-blur-md border-b border-gray-50 transition-transform duration-300 ${headerVisible ? "translate-y-0" : "-translate-y-full"}`}>
          <div className="flex items-center justify-between px-4 py-3">
            <div className="flex items-center gap-3">
              <button onClick={() => navigate(-1)} className="text-gray-600">
                <IoArrowBack size={22} />
              </button>
              <h1 className="text-lg font-bold text-gray-800">피드</h1>
            </div>
            <button
              onClick={() => setActiveTab(activeTab === "feed" ? "bookmarks" : "feed")}
              className={`p-1.5 rounded-lg ${activeTab === "bookmarks" ? "text-primary" : "text-gray-400"}`}
            >
              <IoBookmarkOutline size={20} />
            </button>
          </div>
          {/* Tabs */}
          <div className="flex border-b border-gray-50">
            <button
              onClick={() => setActiveTab("feed")}
              className={`flex-1 py-2.5 text-xs font-medium transition-colors ${
                activeTab === "feed"
                  ? "text-primary border-b-2 border-primary"
                  : "text-gray-400"
              }`}
            >
              타임라인
            </button>
            <button
              onClick={() => setActiveTab("bookmarks")}
              className={`flex-1 py-2.5 text-xs font-medium transition-colors ${
                activeTab === "bookmarks"
                  ? "text-primary border-b-2 border-primary"
                  : "text-gray-400"
              }`}
            >
              북마크
            </button>
          </div>
        </header>

        {/* Composer */}
        {activeTab === "feed" && (
          <PostComposer
            onPostCreated={handlePostCreated}
          />
        )}

        {/* Posts */}
        <motion.div
          variants={staggerContainer}
          initial="hidden"
          animate="visible"
        >
          {posts.map((post) => (
            <motion.div key={post.id} variants={staggerItem}>
              <PostCard
                post={post}
                currentUserId={user?.id}
                onPostDeleted={handlePostDeleted}
                onCommentClick={setCommentPostId}
                onPostUpdated={setEditingPost}
              />
            </motion.div>
          ))}
        </motion.div>

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
            <p className="text-sm text-gray-300">
              {activeTab === "feed" ? "아직 게시글이 없습니다" : "북마크한 게시글이 없습니다"}
            </p>
          </div>
        )}

        {/* Bottom space for nav */}
        <div className="h-20" />
      </div>

      {/* Edit Modal */}
      <AnimatePresence>
        {editingPost && (
          <motion.div
            variants={modalBackdrop}
            initial="hidden"
            animate="visible"
            exit="exit"
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
          >
            <motion.div
              variants={modalContent}
              initial="hidden"
              animate="visible"
              exit="exit"
              className="w-full max-w-lg bg-white rounded-2xl shadow-xl"
            >
            <div className="flex items-center justify-between px-4 pt-4 pb-2 border-b border-gray-100">
              <h2 className="text-sm font-semibold text-gray-800">게시글 수정</h2>
              <button onClick={() => setEditingPost(null)} className="p-1 text-gray-400">
                <IoClose size={20} />
              </button>
            </div>
            <PostComposer
              editingPost={editingPost}
              onPostUpdated={handlePostUpdated}
              onCancel={() => setEditingPost(null)}
            />
          </motion.div>
        </motion.div>
        )}
      </AnimatePresence>

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

export default FeedPage;
