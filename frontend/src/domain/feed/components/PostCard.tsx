import { type FC, useState } from "react";
import {
  IoHeart,
  IoHeartOutline,
  IoChatbubbleOutline,
  IoBookmark,
  IoBookmarkOutline,
  IoEllipsisHorizontal,
  IoTrash,
  IoPencil,
} from "react-icons/io5";
import type { PostResponse } from "../types";
import { feedApi } from "../api/feedApi";

interface Props {
  post: PostResponse;
  currentUserId?: number;
  onPostDeleted?: () => void;
  onCommentClick?: (postId: number) => void;
  onPostUpdated?: (post: PostResponse) => void;
}

const PostCard: FC<Props> = ({ post, currentUserId, onPostDeleted, onCommentClick, onPostUpdated }) => {
  const [liked, setLiked] = useState(post.isLiked);
  const [likeCount, setLikeCount] = useState(post.likeCount);
  const [bookmarked, setBookmarked] = useState(post.isBookmarked);
  const [showMenu, setShowMenu] = useState(false);
  const [imageIndex, setImageIndex] = useState(0);

  const isOwner = currentUserId === post.author.id;

  const handleLike = async () => {
    try {
      const res = await feedApi.toggleLike(post.id);
      setLiked(res.liked);
      setLikeCount((prev) => (res.liked ? prev + 1 : prev - 1));
    } catch { /* silent */ }
  };

  const handleBookmark = async () => {
    try {
      const res = await feedApi.toggleBookmark(post.id);
      setBookmarked(res.bookmarked);
    } catch { /* silent */ }
  };

  const handleDelete = async () => {
    if (!confirm("게시글을 삭제하시겠습니까?")) return;
    try {
      await feedApi.deletePost(post.id);
      onPostDeleted?.();
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
    const days = Math.floor(hours / 24);
    if (days < 30) return `${days}일`;
    return `${d.getMonth() + 1}월 ${d.getDate()}일`;
  };

  return (
    <div className="bg-white border-b border-gray-50">
      {/* Header */}
      <div className="flex items-center justify-between px-4 pt-4 pb-2">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-gray-100 overflow-hidden flex-shrink-0">
            {post.author.profileImageUrl ? (
              <img
                src={post.author.profileImageUrl}
                alt={post.author.nickname}
                className="w-full h-full object-cover"
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center text-gray-400 text-sm font-medium">
                {post.author.nickname.charAt(0)}
              </div>
            )}
          </div>
          <div>
            <p className="text-sm font-semibold text-gray-800">{post.author.nickname}</p>
            <p className="text-[10px] text-gray-400">{formatTime(post.createdAt)}</p>
          </div>
        </div>
        {isOwner && (
          <div className="relative">
            <button onClick={() => setShowMenu(!showMenu)} className="p-1 text-gray-400">
              <IoEllipsisHorizontal size={18} />
            </button>
            {showMenu && (
              <div className="absolute right-0 top-8 bg-white shadow-lg rounded-xl py-1 z-10 min-w-[100px]">
                <button
                  onClick={() => { setShowMenu(false); onPostUpdated?.(post); }}
                  className="flex items-center gap-2 w-full px-4 py-2.5 text-xs text-gray-600 hover:bg-gray-50"
                >
                  <IoPencil size={14} /> 수정
                </button>
                <button
                  onClick={() => { setShowMenu(false); handleDelete(); }}
                  className="flex items-center gap-2 w-full px-4 py-2.5 text-xs text-red-500 hover:bg-gray-50"
                >
                  <IoTrash size={14} /> 삭제
                </button>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Content */}
      <p className="px-4 text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">{post.content}</p>

      {/* Images */}
      {post.imageUrls.length > 0 && (
        <div className="mt-3 relative">
          <div className="aspect-square bg-gray-50 overflow-hidden">
            <img
              src={post.imageUrls[imageIndex]}
              alt=""
              className="w-full h-full object-cover"
            />
          </div>
          {post.imageUrls.length > 1 && (
            <div className="absolute bottom-3 left-1/2 -translate-x-1/2 flex gap-1.5">
              {post.imageUrls.map((_, i) => (
                <button
                  key={i}
                  onClick={() => setImageIndex(i)}
                  className={`w-1.5 h-1.5 rounded-full transition-colors ${
                    i === imageIndex ? "bg-white" : "bg-white/50"
                  }`}
                />
              ))}
            </div>
          )}
        </div>
      )}

      {/* Actions */}
      <div className="flex items-center justify-between px-4 py-3">
        <div className="flex items-center gap-5">
          <button onClick={handleLike} className="flex items-center gap-1.5">
            {liked ? (
              <IoHeart size={20} className="text-red-500" />
            ) : (
              <IoHeartOutline size={20} className="text-gray-500" />
            )}
            {likeCount > 0 && <span className="text-xs text-gray-500">{likeCount}</span>}
          </button>
          <button
            onClick={() => onCommentClick?.(post.id)}
            className="flex items-center gap-1.5"
          >
            <IoChatbubbleOutline size={19} className="text-gray-500" />
            {post.commentCount > 0 && (
              <span className="text-xs text-gray-500">{post.commentCount}</span>
            )}
          </button>
        </div>
        <button onClick={handleBookmark}>
          {bookmarked ? (
            <IoBookmark size={19} className="text-primary" />
          ) : (
            <IoBookmarkOutline size={19} className="text-gray-500" />
          )}
        </button>
      </div>
    </div>
  );
};

export default PostCard;
