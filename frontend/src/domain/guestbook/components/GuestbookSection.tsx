import { type FC, useState, useEffect, useCallback, useRef } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  staggerContainer,
  staggerItem,
  modalBackdrop,
  modalContent,
  buttonHover,
  buttonTap,
} from "../../../global/constants/animations";
import {
  IoSend,
  IoLockClosed,
  IoEllipsisVertical,
  IoPencil,
  IoTrash,
} from "react-icons/io5";
import { guestbookApi } from "../api/guestbookApi";
import { formatDotDate } from "../../../global/utils/date";
import type { GuestbookEntry, GuestbookRequest } from "../types";

interface Props {
  weddingId: number;
  currentUserId: number | null;
  hostUserIds: number[];
}

const GuestbookSection: FC<Props> = ({
  weddingId,
  currentUserId,
  hostUserIds,
}) => {
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
  const [editPassword, setEditPassword] = useState<string | null>(null);

  // ⋮ 드롭다운 메뉴
  const [menuOpenId, setMenuOpenId] = useState<number | null>(null);
  const menuRef = useRef<HTMLDivElement>(null);

  // 비밀번호 모달
  const [passwordModalAction, setPasswordModalAction] = useState<{
    entryId: number;
    action: "edit" | "delete";
  } | null>(null);
  const [modalPassword, setModalPassword] = useState("");
  const [modalError, setModalError] = useState("");
  const [modalVerifying, setModalVerifying] = useState(false);

  const isHost = currentUserId !== null && hostUserIds.includes(currentUserId);
  const isAuthor = (entry: GuestbookEntry) =>
    currentUserId !== null &&
    entry.userId !== null &&
    entry.userId === currentUserId;

  // 메뉴 바깥 클릭 시 닫기
  useEffect(() => {
    const handleClick = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpenId(null);
      }
    };
    if (menuOpenId !== null) {
      document.addEventListener("mousedown", handleClick);
    }
    return () => document.removeEventListener("mousedown", handleClick);
  }, [menuOpenId]);

  const fetchEntries = useCallback(
    async (pageNum: number, append = false) => {
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
    },
    [weddingId],
  );

  useEffect(() => {
    fetchEntries(0);
  }, [fetchEntries]);

  const isLoggedIn = currentUserId !== null;

  // 등록
  const handleSubmit = async () => {
    if (!authorName.trim() || !content.trim()) return;
    if (!isLoggedIn && !password.trim()) return;
    setIsSubmitting(true);
    try {
      const request: GuestbookRequest = {
        authorName: authorName.trim(),
        content: content.trim(),
        ...(password.trim() ? { password: password.trim() } : {}),
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

  // 수정 저장
  const handleUpdate = async (entryId: number) => {
    if (!editContent.trim()) return;
    try {
      const entry = entries.find((e) => e.id === entryId);
      if (!entry) return;
      await guestbookApi.updateEntry(weddingId, entryId, {
        authorName: entry.authorName,
        content: editContent.trim(),
        isSecret: entry.isSecret,
        ...(editPassword ? { password: editPassword } : {}),
      });
      setEditingId(null);
      setEditContent("");
      setEditPassword(null);
      fetchEntries(0);
    } catch {
      alert("수정에 실패했습니다. 비밀번호를 확인해주세요.");
    }
  };

  // 삭제
  const handleDelete = async (entryId: number, pw?: string) => {
    try {
      await guestbookApi.deleteEntry(weddingId, entryId, pw);
      fetchEntries(0);
    } catch {
      alert("삭제에 실패했습니다. 비밀번호를 확인해주세요.");
    }
  };

  // ⋮ 메뉴 액션 핸들러
  const handleMenuAction = (
    entry: GuestbookEntry,
    action: "edit" | "delete",
  ) => {
    setMenuOpenId(null);

    if (action === "edit") {
      if (isAuthor(entry)) {
        // 로그인 본인: 바로 편집 모드
        setEditingId(entry.id);
        setEditContent(entry.content);
        setEditPassword(null);
      } else {
        // 비로그인: 비밀번호 모달
        setPasswordModalAction({ entryId: entry.id, action: "edit" });
        setModalPassword("");
      }
    } else {
      // delete
      if (isHost || isAuthor(entry)) {
        // 호스트 또는 로그인 본인: confirm 후 바로 삭제
        if (confirm("이 방명록을 삭제하시겠습니까?")) {
          void handleDelete(entry.id);
        }
      } else {
        // 비로그인: 비밀번호 모달
        setPasswordModalAction({ entryId: entry.id, action: "delete" });
        setModalPassword("");
      }
    }
  };

  // 비밀번호 모달 확인
  const handleModalConfirm = async () => {
    if (!passwordModalAction || !modalPassword.trim()) return;
    const { entryId, action } = passwordModalAction;
    const pw = modalPassword.trim();

    setModalVerifying(true);
    setModalError("");

    try {
      await guestbookApi.verifyPassword(weddingId, entryId, pw);
    } catch {
      setModalError("비밀번호가 일치하지 않습니다");
      setModalVerifying(false);
      return;
    }

    setModalVerifying(false);
    setPasswordModalAction(null);
    setModalPassword("");
    setModalError("");

    if (action === "edit") {
      const entry = entries.find((e) => e.id === entryId);
      if (!entry) return;
      setEditingId(entryId);
      setEditContent(entry.content);
      setEditPassword(pw);
    } else {
      void handleDelete(entryId, pw);
    }
  };

  const canSubmit =
    authorName.trim() !== "" &&
    content.trim() !== "" &&
    (isLoggedIn || password.trim() !== "");

  return (
    <section>
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
              autoComplete="off"
              placeholder={isLoggedIn ? "비밀번호" : "비밀번호 *"}
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
            </label>
          </div>
          <motion.button
            onClick={handleSubmit}
            disabled={isSubmitting || !canSubmit}
            whileHover={!isSubmitting && canSubmit ? buttonHover : {}}
            whileTap={!isSubmitting && canSubmit ? buttonTap : {}}
            className="flex items-center gap-1 bg-primary text-white text-xs px-4 py-2 rounded-full disabled:opacity-40"
          >
            {isSubmitting ? (
              <motion.div
                animate={{ rotate: 360 }}
                transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
              >
                <IoSend size={12} />
              </motion.div>
            ) : (
              <IoSend size={12} />
            )}
            {isSubmitting ? "등록 중..." : "등록"}
          </motion.button>
        </div>
      </div>

      {/* 방명록 목록 */}
      <motion.div
        variants={staggerContainer}
        initial="hidden"
        animate="visible"
        className="space-y-3"
      >
        {entries.map((entry) => {
          const secretMasked =
            entry.isSecret && entry.content === "비밀 메시지입니다";

          return (
            <motion.div
              key={entry.id}
              variants={staggerItem}
              className="bg-white rounded-xl p-4 shadow-sm"
            >
              {editingId === entry.id ? (
                /* 수정 모드 */
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
                      onClick={() => {
                        setEditingId(null);
                        setEditContent("");
                        setEditPassword(null);
                      }}
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
                      {entry.isSecret && (
                        <IoLockClosed size={12} className="text-gray-300" />
                      )}
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-[10px] text-gray-300">
                        {formatDotDate(entry.createdAt)}
                      </span>
                      {/* 비로그인 작성 글(userId 없음): ⋮ 드롭다운 메뉴 */}
                      {entry.userId === null && (
                        <div
                          className="relative"
                          ref={menuOpenId === entry.id ? menuRef : undefined}
                        >
                          <button
                            onClick={() =>
                              setMenuOpenId(
                                menuOpenId === entry.id ? null : entry.id,
                              )
                            }
                            className="text-gray-300 hover:text-gray-500 p-0.5"
                          >
                            <IoEllipsisVertical size={14} />
                          </button>
                          {menuOpenId === entry.id && (
                            <div className="absolute right-0 top-6 bg-white border border-gray-100 rounded-lg shadow-lg py-1 z-10 min-w-[80px]">
                              <button
                                onClick={() => handleMenuAction(entry, "edit")}
                                className="w-full text-left text-xs text-gray-600 px-3 py-1.5 hover:bg-gray-50"
                              >
                                수정
                              </button>
                              <button
                                onClick={() =>
                                  handleMenuAction(entry, "delete")
                                }
                                className="w-full text-left text-xs text-red-500 px-3 py-1.5 hover:bg-gray-50"
                              >
                                삭제
                              </button>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  </div>
                  <p
                    className={`text-sm leading-relaxed ${secretMasked ? "text-gray-300 italic" : "text-gray-600"}`}
                  >
                    {entry.content}
                  </p>
                  {/* 로그인 작성 글: 본인만 수정/삭제 아이콘, 호스트는 삭제만 */}
                  {entry.userId !== null && (isAuthor(entry) || isHost) && (
                    <div className="flex justify-end gap-2 mt-2">
                      {isAuthor(entry) && (
                        <button
                          onClick={() => {
                            setEditingId(entry.id);
                            setEditContent(entry.content);
                            setEditPassword(null);
                          }}
                          className="text-gray-300 hover:text-gray-500"
                        >
                          <IoPencil size={14} />
                        </button>
                      )}
                      {(isAuthor(entry) || isHost) && (
                        <button
                          onClick={() => handleMenuAction(entry, "delete")}
                          className="text-gray-300 hover:text-red-400"
                        >
                          <IoTrash size={14} />
                        </button>
                      )}
                    </div>
                  )}
                </>
              )}
            </motion.div>
          );
        })}
      </motion.div>

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

      {/* 비밀번호 모달 */}
      <AnimatePresence>
        {passwordModalAction && (
          <motion.div
            variants={modalBackdrop}
            initial="hidden"
            animate="visible"
            exit="exit"
            className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center"
          >
            <motion.div
              variants={modalContent}
              initial="hidden"
              animate="visible"
              exit="exit"
              className="bg-white rounded-2xl p-6 mx-6 w-full max-w-sm shadow-xl"
            >
              <h3 className="text-sm font-medium text-gray-700 mb-4">
                {passwordModalAction.action === "edit" ? "수정" : "삭제"}하려면
                비밀번호를 입력해주세요
              </h3>
              <input
                type="password"
                autoComplete="off"
                placeholder="비밀번호"
                value={modalPassword}
                onChange={(e) => {
                  setModalPassword(e.target.value);
                  setModalError("");
                }}
                onKeyDown={(e) => e.key === "Enter" && handleModalConfirm()}
                autoFocus
                className={`w-full text-sm border rounded-lg px-3 py-2 outline-none ${modalError ? "border-error" : "border-gray-200"}`}
              />
              {modalError && (
                <p className="text-xs text-error mt-1.5">{modalError}</p>
              )}
              <div className="flex justify-end gap-2 mt-4">
                <button
                  onClick={() => {
                    setPasswordModalAction(null);
                    setModalPassword("");
                    setModalError("");
                  }}
                  className="text-xs text-gray-400 px-3 py-1.5"
                >
                  취소
                </button>
                <button
                  onClick={handleModalConfirm}
                  disabled={!modalPassword.trim() || modalVerifying}
                  className="text-xs text-white bg-primary px-4 py-1.5 rounded-full disabled:opacity-40"
                >
                  {modalVerifying ? "확인 중..." : "확인"}
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </section>
  );
};

export default GuestbookSection;
