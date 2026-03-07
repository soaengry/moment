import { type FC } from "react";
import { motion } from "framer-motion";

interface SkeletonProps {
  className?: string;
  variant?: "text" | "circular" | "rectangular";
  width?: string | number;
  height?: string | number;
}

/**
 * Skeleton 로딩 컴포넌트
 *
 * 좌우로 이동하는 gradient 애니메이션을 통해 로딩 상태를 표시합니다.
 */
export const Skeleton: FC<SkeletonProps> = ({
  className = "",
  variant = "text",
  width,
  height,
}) => {
  const baseClasses = "bg-gray-200 overflow-hidden relative";

  const variantClasses = {
    text: "rounded",
    circular: "rounded-full",
    rectangular: "rounded-lg",
  };

  const style = {
    width: width || (variant === "circular" ? "40px" : "100%"),
    height: height || (variant === "text" ? "1em" : variant === "circular" ? "40px" : "100px"),
  };

  return (
    <div
      className={`${baseClasses} ${variantClasses[variant]} ${className}`}
      style={style}
    >
      <motion.div
        className="absolute inset-0 bg-gradient-to-r from-transparent via-white/40 to-transparent"
        animate={{
          x: ["-100%", "100%"],
        }}
        transition={{
          duration: 1.5,
          repeat: Infinity,
          ease: "linear",
        }}
      />
    </div>
  );
};

/**
 * 게시글 카드 스켈레톤
 */
export const PostCardSkeleton: FC = () => {
  return (
    <div className="bg-white border-b border-gray-50 p-4">
      <div className="flex items-center gap-3 mb-3">
        <Skeleton variant="circular" width={40} height={40} />
        <div className="flex-1">
          <Skeleton width="30%" height="16px" className="mb-1" />
          <Skeleton width="20%" height="12px" />
        </div>
      </div>
      <Skeleton width="100%" height="80px" className="mb-3" />
      <div className="space-y-2">
        <Skeleton width="90%" height="14px" />
        <Skeleton width="70%" height="14px" />
      </div>
    </div>
  );
};

/**
 * 방명록 카드 스켈레톤
 */
export const GuestbookSkeleton: FC = () => {
  return (
    <div className="bg-white rounded-xl p-4 shadow-sm space-y-2">
      <div className="flex items-center justify-between">
        <Skeleton width="25%" height="16px" />
        <Skeleton width="15%" height="12px" />
      </div>
      <Skeleton width="100%" height="14px" />
      <Skeleton width="80%" height="14px" />
    </div>
  );
};

/**
 * 리스트 아이템 스켈레톤
 */
export const ListItemSkeleton: FC = () => {
  return (
    <div className="flex items-center gap-3 p-3">
      <Skeleton variant="circular" width={48} height={48} />
      <div className="flex-1 space-y-2">
        <Skeleton width="60%" height="16px" />
        <Skeleton width="40%" height="12px" />
      </div>
    </div>
  );
};

export default Skeleton;
