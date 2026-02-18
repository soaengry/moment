import { type FC, useState } from "react";
import { IoImageOutline, IoClose } from "react-icons/io5";
import { feedApi } from "../api/feedApi";
import type { PostResponse, PostRequest } from "../types";

interface Props {
  onPostCreated?: (post: PostResponse) => void;
  editingPost?: PostResponse | null;
  onPostUpdated?: (post: PostResponse) => void;
  onCancel?: () => void;
}

const PostComposer: FC<Props> = ({ onPostCreated, editingPost, onPostUpdated, onCancel }) => {
  const [content, setContent] = useState(editingPost?.content ?? "");
  const [imageUrls, setImageUrls] = useState<string[]>(editingPost?.imageUrls ?? []);
  const [imageInput, setImageInput] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const maxChars = 200;
  const maxImages = 4;

  const handleAddImage = () => {
    if (!imageInput.trim() || imageUrls.length >= maxImages) return;
    setImageUrls((prev) => [...prev, imageInput.trim()]);
    setImageInput("");
  };

  const handleRemoveImage = (index: number) => {
    setImageUrls((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async () => {
    if (!content.trim() || isSubmitting) return;
    setIsSubmitting(true);
    try {
      const request: PostRequest = {
        content: content.trim(),
        imageUrls: imageUrls.length > 0 ? imageUrls : undefined,
      };

      if (editingPost) {
        const updated = await feedApi.updatePost(editingPost.id, request);
        onPostUpdated?.(updated);
      } else {
        const created = await feedApi.createPost(request);
        onPostCreated?.(created);
      }

      setContent("");
      setImageUrls([]);
    } catch { /* silent */ }
    finally {
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
        <span className={`text-[10px] ${content.length >= maxChars ? "text-red-400" : "text-gray-300"}`}>
          {content.length}/{maxChars}
        </span>
      </div>

      {/* Image URLs */}
      {imageUrls.length > 0 && (
        <div className="flex gap-2 mb-3 overflow-x-auto pb-1">
          {imageUrls.map((url, i) => (
            <div key={i} className="relative flex-shrink-0 w-16 h-16 rounded-lg overflow-hidden bg-gray-100">
              <img src={url} alt="" className="w-full h-full object-cover" />
              <button
                onClick={() => handleRemoveImage(i)}
                className="absolute -top-1 -right-1 w-5 h-5 bg-black/60 rounded-full flex items-center justify-center"
              >
                <IoClose size={12} className="text-white" />
              </button>
            </div>
          ))}
        </div>
      )}

      {/* Image URL input */}
      {imageUrls.length < maxImages && (
        <div className="flex gap-2 mb-3">
          <input
            type="text"
            value={imageInput}
            onChange={(e) => setImageInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleAddImage()}
            placeholder="이미지 URL 입력"
            className="flex-1 text-xs border border-gray-100 rounded-lg px-3 py-2 outline-none placeholder:text-gray-300"
          />
          <button
            onClick={handleAddImage}
            disabled={!imageInput.trim()}
            className="text-gray-400 hover:text-primary disabled:opacity-30"
          >
            <IoImageOutline size={20} />
          </button>
        </div>
      )}

      {/* Actions */}
      <div className="flex items-center justify-between">
        <span className="text-[10px] text-gray-300">
          {imageUrls.length}/{maxImages} 이미지
        </span>
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
            disabled={isSubmitting || !content.trim()}
            className="bg-primary text-white text-xs px-5 py-2 rounded-full disabled:opacity-40"
          >
            {editingPost ? "수정" : "게시"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default PostComposer;
