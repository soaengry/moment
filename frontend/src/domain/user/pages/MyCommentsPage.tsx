import { type FC, useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { IoArrowBack, IoTrash } from "react-icons/io5";
import { feedApi } from "../../feed/api/feedApi";
import { formatRelativeTime } from "../../../global/utils/date";
import type { CommentResponse } from "../../feed/types";
import { useScrollVisibility } from "../../../global/hooks/useScrollVisibility";
import { handleApiError } from "../../../global/utils/errorHandler";

const MyCommentsPage: FC = () => {
  const navigate = useNavigate();
  const headerVisible = useScrollVisibility();
  const [comments, setComments] = useState<CommentResponse[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(false);

  const fetchComments = useCallback(async (pageNum: number, append = false) => {
    setIsLoading(true);
    try {
      const res = await feedApi.getMyComments(pageNum);
      if (append) {
        setComments((prev) => [...prev, ...res.content]);
      } else {
        setComments(res.content);
      }
      setHasMore(!res.last);
      setPage(pageNum);
    } catch (error) {
      handleApiError(error, "댓글을 불러오지 못했습니다.");
    } finally { setIsLoading(false); }
  }, []);

  useEffect(() => {
    fetchComments(0);
  }, [fetchComments]);

  const handleDelete = async (commentId: number) => {
    try {
      await feedApi.deleteComment(commentId);
      setComments((prev) => prev.filter((c) => c.id !== commentId));
    } catch (error) {
      handleApiError(error, "댓글 삭제에 실패했습니다.");
    }
  };

  return (
    <div className="max-w-lg mx-auto min-h-screen bg-[#faf9f6]">
      <header className={`sticky top-0 z-20 bg-white/80 backdrop-blur-md border-b border-gray-50 transition-transform duration-300 ${headerVisible ? "translate-y-0" : "-translate-y-full"}`}>
        <div className="flex items-center gap-3 px-4 py-3">
          <button onClick={() => navigate(-1)} className="text-gray-600">
            <IoArrowBack size={22} />
          </button>
          <h1 className="text-base font-semibold text-gray-800">내 댓글</h1>
        </div>
      </header>

      <div className="divide-y divide-gray-50">
        {comments.map((comment) => (
          <div key={comment.id} className="bg-white px-4 py-4">
            <div className="flex items-center justify-between mb-1.5">
              <div className="flex items-center gap-2">
                <span className="text-xs font-semibold text-gray-700">
                  {comment.author.nickname}
                </span>
                <span className="text-[10px] text-gray-300">
                  {formatRelativeTime(comment.createdAt)}
                </span>
              </div>
              <button
                onClick={() => handleDelete(comment.id)}
                className="text-gray-300 hover:text-red-400"
              >
                <IoTrash size={14} />
              </button>
            </div>
            <p className="text-sm text-gray-600">{comment.content}</p>
            <p className="text-[10px] text-gray-300 mt-1.5">
              게시글 #{comment.postId}
            </p>
          </div>
        ))}
      </div>

      {hasMore && (
        <button
          onClick={() => fetchComments(page + 1, true)}
          disabled={isLoading}
          className="w-full py-4 text-xs text-gray-400"
        >
          {isLoading ? "불러오는 중..." : "더보기"}
        </button>
      )}

      {comments.length === 0 && !isLoading && (
        <div className="text-center py-20">
          <p className="text-sm text-gray-300">작성한 댓글이 없습니다</p>
        </div>
      )}

      <div className="h-20" />
    </div>
  );
};

export default MyCommentsPage;
