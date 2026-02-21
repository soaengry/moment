import { type FC, useState, useEffect, useCallback } from "react";
import { IoSend, IoLockClosed, IoTrash, IoPencil } from "react-icons/io5";
import { guestbookApi } from "../api/guestbookApi";
import type { GuestbookEntry, GuestbookRequest } from "../types";

interface Props {
  weddingId: number;
  currentUserId: number | null;
  hostUserIds: number[];
}

const GuestbookSection: FC<Props> = ({ weddingId, currentUserId, hostUserIds }) => {
  const [entries, setEntries] = useState<GuestbookEntry[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(false);

  // 작성 폼
  const [authorName, setAuthorName] = useState("");
  const [content, setContent] = useState("");
  const [password, setPassword] = useState("");
  const [isSecret, setIsSecret] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 수정 상태
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editContent, setEditContent] = useState("");

  // 삭제 상태
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [deletePassword, setDeletePassword] = useState("");

  const isHost = currentUserId !== null && hostUserIds.includes(currentUserId);
  const isAuthor = (entry: GuestbookEntry) =>
    currentUserId !== null && entry.userId !== null && entry.userId === currentUserId;

  const fetchEntries = useCallback(async (pageNum: number, append = false) => {
    setIsLoading(true);
    try {
      const res = await guestbookApi.getEntries(weddingId, pageNum);
      if (append) {
        setEntries((prev) => [...prev, ...res.content]);
      } else {
        setEntries(res.content);
      }
      setHasMore(!res.last);
      setPage(pageNum);
    } catch {
      // silent
    } finally {
      setIsLoading(false);
    }
  }, [weddingId]);

  useEffect(() => {
    fetchEntries(0);
  }, [fetchEntries]);

  // 등록: 이름 + 비밀번호 모두 필수
  const handleSubmit = async () => {
    if (!authorName.trim() || !password.trim() || !content.trim()) return;
    setIsSubmitting(true);
    try {
      const request: GuestbookRequest = {
        authorName: authorName.trim(),
        content: content.trim(),
        password: password.trim(),
        isSecret,
      };
      await guestbookApi.createEntry(weddingId, request);
      setAuthorName("");
      setContent("");
      setPassword("");
      setIsSecret(false);
      fetchEntries(0);
    } catch {
      // silent
    } finally {
      setIsSubmitting(false);
    }
  };

  // 수정: 본인만 가능
  const handleUpdate = async (entryId: number) => {
    if (!editContent.trim()) return;
    try {
      const entry = entries.find((e) => e.id === entryId);
      if (!entry) return;
      await guestbookApi.updateEntry(weddingId, entryId, {
        authorName: entry.authorName,
        content: editContent.trim(),
        isSecret: entry.isSecret,
      });
      setEditingId(null);
      setEditContent("");
      fetchEntries(0);
    } catch {
      // silent
    }
  };

  // 삭제: 본인은 비밀번호 입력 후
  const handleDelete = async (entryId: number) => {
    try {
      await guestbookApi.deleteEntry(weddingId, entryId, deletePassword || undefined);
      setDeletingId(null);
      setDeletePassword("");
      fetchEntries(0);
    } catch {
      alert("삭제에 실패했습니다. 비밀번호를 확인해주세요.");
    }
  };

  // 호스트 삭제: 비밀번호 없이 바로 삭제
  const handleHostDelete = async (entryId: number) => {
    if (!confirm("이 방명록을 삭제하시겠습니까?")) return;
    try {
      await guestbookApi.deleteEntry(weddingId, entryId, undefined);
      fetchEntries(0);
    } catch {
      alert("삭제에 실패했습니다.");
    }
  };

  const formatDate = (dateStr: string) => {
    const d = new Date(dateStr);
    return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, "0")}.${String(d.getDate()).padStart(2, "0")}`;
  };

  const canSubmit = authorName.trim() !== "" && password.trim() !== "" && content.trim() !== "";

  return (
    <section className="px-6 py-12">
      <h2 className="text-center text-[11px] tracking-[0.4em] text-gray-400 uppercase mb-8">
        Guestbook
      </h2>

      {/* 작성 폼 */}
      <div className="bg-white rounded-2xl p-5 shadow-sm mb-6">
        <input
          type="text"
          placeholder="이름 *"
          value={authorName}
          onChange={(e) => setAuthorName(e.target.value)}
          maxLength={50}
          className="w-full text-sm border-b border-gray-100 pb-2 mb-3 outline-none placeholder:text-gray-300"
        />
        <textarea
          placeholder="축하 메시지를 남겨주세요"
          value={content}
          onChange={(e) => setContent(e.target.value)}
          maxLength={500}
          rows={3}
          className="w-full text-sm resize-none outline-none placeholder:text-gray-300 mb-3"
        />
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <input
              type="password"
              placeholder="비밀번호 *"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-28 text-xs border border-gray-100 rounded-lg px-2 py-1.5 outline-none placeholder:text-gray-300"
            />
            <label className="flex items-center gap-1 text-xs text-gray-400 cursor-pointer">
              <input
                type="checkbox"
                checked={isSecret}
                onChange={(e) => setIsSecret(e.target.checked)}
                className="w-3.5 h-3.5 accent-primary"
              />
              <IoLockClosed size={12} />
              비밀글
            </label>
          </div>
          <button
            onClick={handleSubmit}
            disabled={isSubmitting || !canSubmit}
            className="flex items-center gap-1 bg-primary text-white text-xs px-4 py-2 rounded-full disabled:opacity-40"
          >
            <IoSend size={12} />
            등록
          </button>
        </div>
      </div>

      {/* 방명록 목록 */}
      <div className="space-y-3">
        {entries.map((entry) => {
          const myEntry = isAuthor(entry);
          const secretMasked = entry.isSecret && entry.content === "비밀 메시지입니다";

          return (
            <div key={entry.id} className="bg-white rounded-xl p-4 shadow-sm">
              {/* 삭제 비밀번호 입력 (본인 삭제 시) */}
              {deletingId === entry.id ? (
                <div className="flex items-center gap-2">
                  <input
                    type="password"
                    placeholder="비밀번호 입력"
                    value={deletePassword}
                    onChange={(e) => setDeletePassword(e.target.value)}
                    className="flex-1 text-xs border border-gray-200 rounded-lg px-2 py-1.5 outline-none"
                  />
                  <button
                    onClick={() => handleDelete(entry.id)}
                    className="text-xs text-red-500 font-medium"
                  >
                    확인
                  </button>
                  <button
                    onClick={() => { setDeletingId(null); setDeletePassword(""); }}
                    className="text-xs text-gray-400"
                  >
                    취소
                  </button>
                </div>
              ) : editingId === entry.id ? (
                /* 수정 (본인만) */
                <div>
                  <textarea
                    value={editContent}
                    onChange={(e) => setEditContent(e.target.value)}
                    maxLength={500}
                    rows={2}
                    className="w-full text-sm resize-none outline-none border border-gray-100 rounded-lg p-2 mb-2"
                  />
                  <div className="flex justify-end gap-2">
                    <button
                      onClick={() => handleUpdate(entry.id)}
                      className="text-xs text-primary font-medium"
                    >
                      저장
                    </button>
                    <button
                      onClick={() => { setEditingId(null); setEditContent(""); }}
                      className="text-xs text-gray-400"
                    >
                      취소
                    </button>
                  </div>
                </div>
              ) : (
                <>
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-medium text-gray-700">
                        {entry.authorName}
                      </span>
                      {entry.isSecret && <IoLockClosed size={12} className="text-gray-300" />}
                    </div>
                    <span className="text-[10px] text-gray-300">{formatDate(entry.createdAt)}</span>
                  </div>
                  <p className={`text-sm leading-relaxed ${secretMasked ? "text-gray-300 italic" : "text-gray-600"}`}>
                    {entry.content}
                  </p>
                  {/* 액션 버튼 */}
                  <div className="flex justify-end gap-2 mt-2">
                    {/* 수정: 본인만 */}
                    {myEntry && (
                      <button
                        onClick={() => { setEditingId(entry.id); setEditContent(entry.content); }}
                        className="text-gray-300 hover:text-gray-500"
                      >
                        <IoPencil size={14} />
                      </button>
                    )}
                    {/* 삭제: 본인(비밀번호 필요) 또는 호스트(바로 삭제) */}
                    {(myEntry || isHost) && (
                      <button
                        onClick={() => {
                          if (isHost && !myEntry) {
                            void handleHostDelete(entry.id);
                          } else {
                            setDeletingId(entry.id);
                          }
                        }}
                        className="text-gray-300 hover:text-red-400"
                      >
                        <IoTrash size={14} />
                      </button>
                    )}
                  </div>
                </>
              )}
            </div>
          );
        })}
      </div>

      {/* 더보기 */}
      {hasMore && (
        <button
          onClick={() => fetchEntries(page + 1, true)}
          disabled={isLoading}
          className="w-full mt-4 py-3 text-xs text-gray-400 border border-gray-100 rounded-xl hover:bg-gray-50"
        >
          {isLoading ? "불러오는 중..." : "더보기"}
        </button>
      )}

      {entries.length === 0 && !isLoading && (
        <p className="text-center text-sm text-gray-300 mt-8">
          아직 남겨진 메시지가 없습니다
        </p>
      )}
    </section>
  );
};

export default GuestbookSection;
