import { type FC, useState } from "react";
import type { AnnouncementResponse } from "../types";

interface Props {
  announcements: AnnouncementResponse[];
}

const formatDate = (dateStr: string) =>
  new Date(dateStr).toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });

const AnnouncementSection: FC<Props> = ({ announcements }) => {
  const [isOpen, setIsOpen] = useState(false);

  if (announcements.length === 0) return null;

  const sorted = [...announcements].sort((a, b) => {
    if (a.isPinned !== b.isPinned) return a.isPinned ? -1 : 1;
    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
  });

  return (
    <>
      {/* FAB 버튼 */}
      <button
        onClick={() => setIsOpen(true)}
        className="fixed bottom-6 right-6 z-40 w-12 h-12 rounded-full bg-primary text-white shadow-lg hover:bg-primaryHover transition-colors flex items-center justify-center"
      >
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className="w-5 h-5">
          <path d="M5.85 3.5a.75.75 0 00-1.117-1 9.719 9.719 0 00-2.348 4.876.75.75 0 001.479.248A8.219 8.219 0 015.85 3.5zM19.267 2.5a.75.75 0 10-1.118 1 8.22 8.22 0 011.987 4.124.75.75 0 001.48-.248A9.72 9.72 0 0019.266 2.5z" />
          <path fillRule="evenodd" d="M12 2.25A6.75 6.75 0 005.25 9v.75a8.217 8.217 0 01-2.119 5.52.75.75 0 00.298 1.206c1.544.57 3.16.99 4.831 1.243a3.75 3.75 0 107.48 0 24.583 24.583 0 004.83-1.244.75.75 0 00.298-1.205 8.217 8.217 0 01-2.118-5.52V9A6.75 6.75 0 0012 2.25zM9.75 18c0-.034 0-.067.002-.1a25.05 25.05 0 004.496 0l.002.1a2.25 2.25 0 01-4.5 0z" clipRule="evenodd" />
        </svg>
      </button>

      {/* 모달 */}
      {isOpen && (
        <div
          className="fixed inset-0 z-50 flex items-end justify-center bg-black/40"
          onClick={() => setIsOpen(false)}
        >
          <div
            className="w-full max-w-lg bg-white rounded-t-2xl max-h-[70vh] flex flex-col animate-slide-up"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-center justify-between p-5 border-b border-gray-100">
              <h3 className="text-base font-semibold text-gray-800">공지사항</h3>
              <button
                onClick={() => setIsOpen(false)}
                className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100 text-gray-400"
              >
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-5 h-5">
                  <path d="M6.28 5.22a.75.75 0 00-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 101.06 1.06L10 11.06l3.72 3.72a.75.75 0 101.06-1.06L11.06 10l3.72-3.72a.75.75 0 00-1.06-1.06L10 8.94 6.28 5.22z" />
                </svg>
              </button>
            </div>
            <div className="overflow-y-auto p-5 space-y-3">
              {sorted.map((a) => (
                <div
                  key={a.id}
                  className={`p-4 rounded-xl ${
                    a.isPinned ? "bg-bgPrimary border border-primary/20" : "bg-gray-50"
                  }`}
                >
                  <div className="flex items-center gap-2 mb-2">
                    {a.isPinned && (
                      <span className="text-[10px] text-primary font-semibold tracking-wider">PIN</span>
                    )}
                    <h4 className="text-sm font-semibold text-gray-800">{a.title}</h4>
                  </div>
                  <p className="text-sm text-gray-500 whitespace-pre-line mb-2">{a.content}</p>
                  <p className="text-[10px] text-gray-300">{formatDate(a.createdAt)}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default AnnouncementSection;
