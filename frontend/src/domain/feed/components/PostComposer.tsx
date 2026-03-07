import { type FC, useState, useRef, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { scaleIn, buttonHover } from "../../../global/constants/animations";
import { IoImageOutline, IoClose } from "react-icons/io5";
import { feedApi } from "../api/feedApi";
import type { PostResponse, PostRequest } from "../types";

interface ImageItem {
  file?: File;
  preview: string;
  url?: string; // S3 URL (for existing images during edit)
}

interface Props {
  onPostCreated?: (post: PostResponse) => void;
  editingPost?: PostResponse | null;
  onPostUpdated?: (post: PostResponse) => void;
  onCancel?: () => void;
  weddingId?: number;
}

const PostComposer: FC<Props> = ({
  onPostCreated,
  editingPost,
  onPostUpdated,
  onCancel,
  weddingId,
}) => {
  const [content, setContent] = useState(editingPost?.content ?? "");
  const [images, setImages] = useState<ImageItem[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const maxChars = 200;
  const maxImages = 4;

  // 수정 모드일 때 기존 이미지 로드
  useEffect(() => {
    if (editingPost?.imageUrls) {
      setImages(
        editingPost.imageUrls.map((url) => ({
          preview: url,
          url,
        })),
      );
    }
  }, [editingPost]);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files) return;

    const remaining = maxImages - images.length;
    const newFiles = Array.from(files).slice(0, remaining);

    setIsUploading(true);
    try {
      const newImages: ImageItem[] = [];
      for (const file of newFiles) {
        const preview = URL.createObjectURL(file);
        const url = await feedApi.uploadImage(file);
        newImages.push({ file, preview, url });
      }
      setImages((prev) => [...prev, ...newImages]);
    } catch {
      /* silent */
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    }
  };

  const handleRemoveImage = (index: number) => {
    setImages((prev) => {
      const removed = prev[index];
      if (removed.file) {
        URL.revokeObjectURL(removed.preview);
      }
      return prev.filter((_, i) => i !== index);
    });
  };

  const handleSubmit = async () => {
    if (!content.trim() || isSubmitting) return;
    setIsSubmitting(true);
    try {
      const imageUrls = images
        .map((img) => img.url)
        .filter((url): url is string => !!url);

      const request: PostRequest = {
        content: content.trim(),
        imageUrls: imageUrls.length > 0 ? imageUrls : undefined,
      };

      if (editingPost) {
        const updated = weddingId
          ? await feedApi.updateWeddingPost(weddingId, editingPost.id, request)
          : await feedApi.updatePost(editingPost.id, request);
        onPostUpdated?.(updated);
      } else {
        const created = weddingId
          ? await feedApi.createWeddingPost(weddingId, request)
          : await feedApi.createPost(request);
        onPostCreated?.(created);
      }

      setContent("");
      setImages([]);
    } catch {
      /* silent */
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="bg-white border-b border-gray-100 p-4">
      <textarea
        value={content}
        onChange={(e) => setContent(e.target.value.slice(0, maxChars))}
        placeholder="무슨 일이 있으셨나요?"
        rows={3}
        className="w-full text-sm resize-none outline-none placeholder:text-gray-300"
      />

      {/* Character count */}
      <div className="flex justify-end mb-2">
        <span
          className={`text-[10px] ${content.length >= maxChars ? "text-red-400" : "text-gray-300"}`}
        >
          {content.length}/{maxChars}
        </span>
      </div>

      {/* Image previews */}
      <AnimatePresence>
        {images.length > 0 && (
          <div className="flex gap-3 mb-3 overflow-x-auto pb-1 pt-2">
            {images.map((img, i) => (
              <motion.div
                key={i}
                variants={scaleIn}
                initial="hidden"
                animate="visible"
                exit="hidden"
                className="relative flex-shrink-0"
              >
                <div className="w-16 h-16 rounded-lg overflow-hidden bg-gray-100">
                  <img
                    src={img.preview}
                    alt=""
                    className="w-full h-full object-cover"
                  />
                </div>
                <motion.button
                  onClick={() => handleRemoveImage(i)}
                  whileHover={buttonHover}
                  className="absolute -top-1.5 -right-1.5 w-5 h-5 bg-black/60 rounded-full flex items-center justify-center"
                >
                  <IoClose size={12} className="text-white" />
                </motion.button>
              </motion.div>
            ))}
          </div>
        )}
      </AnimatePresence>

      {/* Upload indicator */}
      {isUploading && (
        <div className="flex items-center gap-2 mb-3 text-xs text-gray-400">
          <div className="w-3.5 h-3.5 border-2 border-primary border-t-transparent rounded-full animate-spin" />
          이미지 업로드 중...
        </div>
      )}

      {/* Actions */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          {images.length < maxImages && (
            <button
              onClick={() => fileInputRef.current?.click()}
              disabled={isUploading}
              className="text-gray-400 hover:text-primary disabled:opacity-30"
            >
              <IoImageOutline size={22} />
            </button>
          )}
          <span className="text-[10px] text-gray-300">
            {images.length}/{maxImages}
          </span>
        </div>
        <div className="flex gap-2">
          {editingPost && (
            <button
              onClick={onCancel}
              className="text-xs text-gray-400 px-4 py-2 rounded-full border border-gray-200"
            >
              취소
            </button>
          )}
          <button
            onClick={handleSubmit}
            disabled={isSubmitting || isUploading || !content.trim()}
            className="bg-primary text-white text-xs px-5 py-2 rounded-full disabled:opacity-40"
          >
            {editingPost ? "수정" : "게시"}
          </button>
        </div>
      </div>

      <input
        ref={fileInputRef}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        multiple
        onChange={handleFileChange}
        className="hidden"
      />
    </div>
  );
};

export default PostComposer;
