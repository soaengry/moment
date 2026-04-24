import { type FC, useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { IoSearchOutline } from "react-icons/io5";
import { eventApi } from "../../domain/event/api/eventApi";
import { isAxiosError } from "axios";

const SearchPage: FC = () => {
  const navigate = useNavigate();
  const inputRef = useRef<HTMLInputElement>(null);
  const [slug, setSlug] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const handleSearch = async () => {
    const trimmed = slug.trim().toLowerCase();
    if (!trimmed) {
      inputRef.current?.focus();
      return;
    }

    setIsLoading(true);
    setErrorMsg(null);
    try {
      await eventApi.getEventInfo(trimmed);
      navigate(`/event/${trimmed}`);
    } catch (err) {
      if (isAxiosError(err) && err.response?.status === 404) {
        setErrorMsg("존재하지 않는 초대장 ID입니다.");
      } else if (isAxiosError(err) && err.response?.status === 401) {
        setErrorMsg("비공개 초대장이거나 접근 권한이 없습니다.");
      } else {
        setErrorMsg("검색에 실패했습니다. 다시 시도해주세요.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") handleSearch();
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSlug(e.target.value);
    if (errorMsg) setErrorMsg(null);
  };

  return (
    <div className="min-h-screen bg-[#faf9f6]">
      <div className="max-w-lg mx-auto px-4 pt-16 pb-32">
        {/* 헤더 */}
        <div className="mb-8 text-center">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-primary/10 mb-4">
            <IoSearchOutline className="text-3xl text-primary" />
          </div>
          <h1 className="text-xl font-bold text-gray-800">초대장 검색</h1>
          <p className="text-sm text-gray-400 mt-1">
            초대장 ID를 입력해 바로 이동하세요
          </p>
        </div>

        {/* 검색 입력 */}
        <div className="space-y-3">
          <div className="flex gap-2">
            <input
              ref={inputRef}
              type="text"
              value={slug}
              onChange={handleChange}
              onKeyDown={handleKeyDown}
              placeholder="초대장 ID (예: wedding-2025)"
              autoCapitalize="none"
              autoCorrect="off"
              spellCheck={false}
              className={`flex-1 px-4 py-3.5 rounded-xl border text-sm text-gray-900 placeholder-gray-400 bg-white focus:outline-none transition-colors ${
                errorMsg
                  ? "border-red-300 focus:border-red-400"
                  : "border-gray-200 focus:border-primary"
              }`}
            />
            <button
              type="button"
              onClick={handleSearch}
              disabled={isLoading}
              className="px-5 py-3.5 rounded-xl bg-primary text-white text-sm font-semibold hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed transition-colors whitespace-nowrap"
            >
              {isLoading ? (
                <span className="inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
              ) : (
                "검색"
              )}
            </button>
          </div>

          <AnimatePresence>
            {errorMsg && (
              <motion.p
                initial={{ opacity: 0, y: -4 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -4 }}
                className="text-xs text-red-500 px-1"
              >
                {errorMsg}
              </motion.p>
            )}
          </AnimatePresence>
        </div>

        {/* 안내 */}
        <div className="mt-10 rounded-2xl bg-white border border-gray-100 p-5 space-y-3">
          <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide">
            초대장 ID란?
          </p>
          <p className="text-sm text-gray-500 leading-relaxed">
            초대장을 만들 때 설정한 고유 ID입니다.
            <br />
            초대장 링크의 마지막 부분에서 확인할 수 있어요.
          </p>
          <div className="rounded-lg bg-gray-50 px-3 py-2">
            <p className="text-xs text-gray-400 font-mono">
              moment.app/event/
              <span className="text-primary font-semibold">wedding-2025</span>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SearchPage;
