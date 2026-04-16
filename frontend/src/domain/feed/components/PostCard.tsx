import { type FC, useState, useRef } from "react";
import { motion } from "framer-motion";
import { iconScale, buttonTap } from "../../../global/constants/animations";
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
import { formatRelativeTime } from "../../../global/utils/date";
import ImageViewer from "./ImageViewer";

interface Props {
  post: PostResponse;
  currentUserId?: number;
  onPostDeleted?: () => void;
  onCommentClick?: (postId: number) => void;
  onPostUpdated?: (post: PostResponse) => void;
}

const PostCard: FC<Props> = ({
  post,
  currentUserId,
  onPostDeleted,
  onCommentClick,
  onPostUpdated,
}) => {
  const [liked, setLiked] = useState(post.isLiked);
  const [likeCount, setLikeCount] = useState(post.likeCount);
  const [bookmarked, setBookmarked] = useState(post.isBookmarked);
  const [showMenu, setShowMenu] = useState(false);
  const [imageIndex, setImageIndex] = useState(0);
  const [viewerOpen, setViewerOpen] = useState(false);

  // 스와이프 상태
  const [swipeX, setSwipeX] = useState(0);
  const startXRef = useRef(0);
  const isDraggingRef = useRef(false);
  const isSwipingRef = useRef(false);

  const isOwner = currentUserId === post.author.id;

  const handleLike = async () => {
    try {
      const res = await feedApi.toggleLike(post.id);
      setLiked(res.liked);
      setLikeCount((prev) => (res.liked ? prev + 1 : prev - 1));
    } catch {
      /* silent */
    }
  };

  const handleBookmark = async () => {
    try {
      const res = await feedApi.toggleBookmark(post.id);
      setBookmarked(res.bookmarked);
    } catch {
      /* silent */
    }
  };

  const handleDelete = async () => {
    if (!window.confirm("게시글을 삭제하시겠습니까?")) return;
    try {
      await feedApi.deletePost(post.id);
      onPostDeleted?.();
    } catch {
      /* silent */
    }
  };

  // 이미지 스와이프 핸들러
  const handleImageTouchStart = (e: React.TouchEvent) => {
    startXRef.current = e.touches[0].clientX;
    isDraggingRef.current = true;
    isSwipingRef.current = false;
    setSwipeX(0);
  };

  const handleImageTouchMove = (e: React.TouchEvent) => {
    if (!isDraggingRef.current) return;
    const diff = e.touches[0].clientX - startXRef.current;

    if (Math.abs(diff) > 10) {
      isSwipingRef.current = true;
    }

    // 경계 제한: 저항감 부여
    if (imageIndex === 0 && diff > 0) {
      setSwipeX(diff * 0.3);
      return;
    }
    if (imageIndex === post.imageUrls.length - 1 && diff < 0) {
      setSwipeX(diff * 0.3);
      return;
    }

    setSwipeX(diff);
  };

  const handleImageTouchEnd = () => {
    if (!isDraggingRef.current) return;
    isDraggingRef.current = false;

    const threshold = 50;
    if (swipeX < -threshold && imageIndex < post.imageUrls.length - 1) {
      setImageIndex((prev) => prev + 1);
    } else if (swipeX > threshold && imageIndex > 0) {
      setImageIndex((prev) => prev - 1);
    }

    setSwipeX(0);
  };

  const handleImageClick = () => {
    // 스와이프 중이면 클릭 무시
    if (isSwipingRef.current) return;
    setViewerOpen(true);
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
            <p className="text-sm font-semibold text-gray-800">
              {post.author.nickname}
            </p>
            <p className="text-[10px] text-gray-400">
              {formatRelativeTime(post.createdAt)}
            </p>
          </div>
        </div>
        {isOwner && (
          <div className="relative">
            <button
              onClick={() => setShowMenu(!showMenu)}
              className="p-1 text-gray-400"
            >
              <IoEllipsisHorizontal size={18} />
            </button>
            {showMenu && (
              <div className="absolute right-0 top-8 bg-white shadow-lg rounded-xl py-1 z-10 min-w-[100px]">
                <button
                  onClick={() => {
                    setShowMenu(false);
                    onPostUpdated?.(post);
                  }}
                  className="flex items-center gap-2 w-full px-4 py-2.5 text-xs text-gray-600 hover:bg-gray-50"
                >
                  <IoPencil size={14} /> 수정
                </button>
                <button
                  onClick={() => {
                    setShowMenu(false);
                    handleDelete();
                  }}
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
      <p className="px-4 text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">
        {post.content}
      </p>

      {/* Images */}
      {post.imageUrls.length > 0 && (
        <div className="mt-3 relative">
          <div
            className="aspect-square bg-gray-50 overflow-hidden"
            onTouchStart={handleImageTouchStart}
            onTouchMove={handleImageTouchMove}
            onTouchEnd={handleImageTouchEnd}
          >
            <div
              className="flex h-full"
              style={{
                transform: `translateX(calc(-${imageIndex * 100}% + ${swipeX}px))`,
                transition: swipeX !== 0
                  ? "none"
                  : "transform 300ms ease-out",
              }}
            >
              {post.imageUrls.map((url, i) => (
                <img
                  key={i}
                  src={url}
                  alt=""
                  className="w-full h-full object-cover cursor-pointer select-none flex-shrink-0"
                  onClick={handleImageClick}
                  draggable={false}
                />
              ))}
            </div>
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
          <motion.button
            onClick={handleLike}
            whileTap={buttonTap}
            className="flex items-center gap-1.5"
          >
            <motion.div animate={liked ? iconScale : {}}>
              {liked ? (
                <IoHeart size={20} className="text-red-500" />
              ) : (
                <IoHeartOutline size={20} className="text-gray-500" />
              )}
            </motion.div>
            {likeCount > 0 && (
              <span className="text-xs text-gray-500">{likeCount}</span>
            )}
          </motion.button>
          <motion.button
            onClick={() => onCommentClick?.(post.id)}
            whileTap={buttonTap}
            className="flex items-center gap-1.5"
          >
            <IoChatbubbleOutline size={19} className="text-gray-500" />
            {post.commentCount > 0 && (
              <span className="text-xs text-gray-500">{post.commentCount}</span>
            )}
          </motion.button>
        </div>
        <motion.button onClick={handleBookmark} whileTap={buttonTap}>
          <motion.div animate={bookmarked ? iconScale : {}}>
            {bookmarked ? (
              <IoBookmark size={19} className="text-primary" />
            ) : (
              <IoBookmarkOutline size={19} className="text-gray-500" />
            )}
          </motion.div>
        </motion.button>
      </div>

      {/* Image Viewer Modal */}
      {viewerOpen && (
        <ImageViewer
          imageUrls={post.imageUrls}
          initialIndex={imageIndex}
          onClose={() => setViewerOpen(false)}
        />
      )}
    </div>
  );
};

export default PostCard;
