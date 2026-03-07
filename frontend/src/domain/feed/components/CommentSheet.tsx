import { type FC, useState, useEffect, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { bottomSheet, modalBackdrop, staggerContainer, staggerItem } from "../../../global/constants/animations";
import { IoClose, IoSend, IoTrash } from "react-icons/io5";
import { feedApi } from "../api/feedApi";
import type { CommentResponse } from "../types";

interface Props {
  postId: number;
  isOpen: boolean;
  onClose: () => void;
  currentUserId?: number;
  weddingId?: number;
  onCommentCountChange?: (postId: number, delta: number) => void;
}

const CommentSheet: FC<Props> = ({ postId, isOpen, onClose, currentUserId, weddingId, onCommentCountChange }) => {
  const [comments, setComments] = useState<CommentResponse[]>([]);
  const [content, setContent] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchComments = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = weddingId
        ? await feedApi.getWeddingComments(weddingId, postId)
        : await feedApi.getComments(postId);
      setComments(res.content);
    } catch { /* silent */ }
    finally { setIsLoading(false); }
  }, [postId, weddingId]);

  useEffect(() => {
    if (isOpen) fetchComments();
  }, [isOpen, fetchComments]);

  const handleSubmit = async () => {
    if (!content.trim() || isSubmitting) return;
    setIsSubmitting(true);
    try {
      weddingId
        ? await feedApi.createWeddingComment(weddingId, postId, { content: content.trim() })
        : await feedApi.createComment(postId, { content: content.trim() });
      setContent("");
      fetchComments();
      onCommentCountChange?.(postId, 1);
    } catch { /* silent */ }
    finally { setIsSubmitting(false); }
  };

  const handleDelete = async (commentId: number) => {
    try {
      await feedApi.deleteComment(commentId);
      fetchComments();
      onCommentCountChange?.(postId, -1);
    } catch { /* silent */ }
  };

  const formatTime = (dateStr: string) => {
    const d = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - d.getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 1) return "방금";
    if (mins < 60) return `${mins}분`;
    const hours = Math.floor(mins / 60);
    if (hours < 24) return `${hours}시간`;
    return `${d.getMonth() + 1}/${d.getDate()}`;
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-end justify-center">
          <motion.div
            variants={modalBackdrop}
            initial="hidden"
            animate="visible"
            exit="exit"
            className="absolute inset-0 bg-black/40"
            onClick={onClose}
          />
          <motion.div
            variants={bottomSheet}
            initial="hidden"
            animate="visible"
            exit="exit"
            className="relative w-full max-w-lg bg-white rounded-t-2xl max-h-[70vh] flex flex-col"
          >
        {/* Header */}
        <div className="flex items-center justify-between px-4 py-3 border-b border-gray-50">
          <h3 className="text-sm font-semibold text-gray-700">댓글</h3>
          <button onClick={onClose} className="text-gray-400">
            <IoClose size={20} />
          </button>
        </div>

        {/* Comments list */}
        <div className="flex-1 overflow-y-auto px-4 py-3">
          {isLoading ? (
            <div className="text-center py-8">
              <motion.div
                animate={{ rotate: 360 }}
                transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                className="w-5 h-5 border-2 border-primary border-t-transparent rounded-full mx-auto"
              />
            </div>
          ) : comments.length === 0 ? (
            <p className="text-center text-sm text-gray-300 py-8">첫 번째 댓글을 남겨보세요</p>
          ) : (
            <motion.div
              variants={staggerContainer}
              initial="hidden"
              animate="visible"
              className="space-y-4"
            >
              {comments.map((comment) => (
                <motion.div key={comment.id} variants={staggerItem} className="flex gap-3">
                <div className="w-8 h-8 rounded-full bg-gray-100 flex-shrink-0 overflow-hidden flex items-center justify-center">
                  {comment.author.profileImageUrl ? (
                    <img src={comment.author.profileImageUrl} alt="" className="w-full h-full object-cover" />
                  ) : (
                    <span className="text-xs text-gray-400">{comment.author.nickname.charAt(0)}</span>
                  )}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-semibold text-gray-700">{comment.author.nickname}</span>
                    <span className="text-[10px] text-gray-300">{formatTime(comment.createdAt)}</span>
                  </div>
                  <p className="text-sm text-gray-600 mt-0.5">{comment.content}</p>
                </div>
                {currentUserId === comment.author.id && (
                  <button
                    onClick={() => handleDelete(comment.id)}
                    className="text-gray-300 hover:text-red-400 flex-shrink-0 self-start mt-1"
                  >
                    <IoTrash size={14} />
                  </button>
                )}
              </motion.div>
              ))}
            </motion.div>
          )}
        </div>

        {/* Input */}
        <div className="flex items-center gap-2 px-4 py-3 border-t border-gray-50">
          <input
            type="text"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSubmit()}
            placeholder="댓글 달기..."
            maxLength={300}
            className="flex-1 text-sm outline-none placeholder:text-gray-300"
          />
          <button
            onClick={handleSubmit}
            disabled={isSubmitting || !content.trim()}
            className="text-primary disabled:opacity-30"
          >
            <IoSend size={18} />
          </button>
        </div>
      </motion.div>
    </div>
      )}
    </AnimatePresence>
  );
};

export default CommentSheet;
