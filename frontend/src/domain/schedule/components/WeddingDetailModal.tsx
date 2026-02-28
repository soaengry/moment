import { type FC } from "react";
import { useNavigate } from "react-router-dom";
import { IoClose, IoLocation, IoCalendar, IoOpen, IoTrash } from "react-icons/io5";
import type { AttendanceResponse } from "../types";

interface WeddingDetailModalProps {
  attendance: AttendanceResponse;
  onClose: () => void;
  onDelete: (id: number) => void;
}

const WeddingDetailModal: FC<WeddingDetailModalProps> = ({
  attendance,
  onClose,
  onDelete,
}) => {
  const navigate = useNavigate();

  const weddingDate = new Date(attendance.weddingDate);
  const dateStr = weddingDate.toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    weekday: "short",
  });
  const timeStr = weddingDate.toLocaleTimeString("ko-KR", {
    hour: "2-digit",
    minute: "2-digit",
  });

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-sm mx-4 overflow-hidden">
        <div className="flex items-center justify-between p-4 border-b border-gray-100">
          <h3 className="text-lg font-semibold text-gray-800">결혼식 정보</h3>
          <button
            onClick={onClose}
            className="p-1 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <IoClose className="w-5 h-5 text-gray-500" />
          </button>
        </div>

        <div className="p-4 space-y-4">
          <h4 className="text-base font-semibold text-gray-800">
            {attendance.title}
          </h4>

          {(attendance.groomName || attendance.brideName) && (
            <div className="flex items-center gap-3">
              {attendance.groomProfileImageUrl && (
                <img
                  src={attendance.groomProfileImageUrl}
                  alt="신랑"
                  className="w-10 h-10 rounded-full object-cover"
                />
              )}
              <span className="text-sm text-gray-700">
                {attendance.groomName ?? ""}
                {attendance.groomName && attendance.brideName && " & "}
                {attendance.brideName ?? ""}
              </span>
              {attendance.brideProfileImageUrl && (
                <img
                  src={attendance.brideProfileImageUrl}
                  alt="신부"
                  className="w-10 h-10 rounded-full object-cover"
                />
              )}
            </div>
          )}

          <div className="flex items-start gap-2 text-sm text-gray-600">
            <IoCalendar className="w-4 h-4 mt-0.5 text-gray-400 shrink-0" />
            <span>
              {dateStr} {timeStr}
            </span>
          </div>

          <div className="flex items-start gap-2 text-sm text-gray-600">
            <IoLocation className="w-4 h-4 mt-0.5 text-gray-400 shrink-0" />
            <div>
              <p>{attendance.venueName}</p>
              <p className="text-gray-400 text-xs">{attendance.venueAddress}</p>
            </div>
          </div>
        </div>

        <div className="flex gap-2 p-4 border-t border-gray-100">
          <button
            onClick={() => navigate(`/wedding/${attendance.invitationId}`)}
            className="flex-1 flex items-center justify-center gap-1 py-2.5 rounded-lg text-sm font-medium text-white bg-primary hover:bg-primaryHover transition-colors"
          >
            <IoOpen className="w-4 h-4" />
            결혼식 보기
          </button>
          <button
            onClick={() => onDelete(attendance.id)}
            className="px-4 py-2.5 rounded-lg text-sm font-medium text-red-500 border border-red-200 hover:bg-red-50 transition-colors"
          >
            <IoTrash className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
};

export default WeddingDetailModal;
