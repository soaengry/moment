import { type FC, useState, useEffect } from "react";
import { IoCheckmarkCircle, IoCloseCircleOutline, IoPencilOutline } from "react-icons/io5";
import { toast } from "react-toastify";
import { isAxiosError } from "axios";
import { rsvpApi } from "../api/rsvpApi";
import { RsvpForm } from "./RsvpForm";
import type { RsvpResponse } from "../types";

interface Props {
  weddingId: number;
}

const SIDE_LABEL: Record<string, string> = { GROOM: "신랑측", BRIDE: "신부측" };

const RsvpSection: FC<Props> = ({ weddingId }) => {
  const [existingRsvp, setExistingRsvp] = useState<RsvpResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    const fetchRsvp = async () => {
      try {
        const res = await rsvpApi.getMyRsvp(weddingId);
        setExistingRsvp(res.data ?? null);
      } catch {
        setExistingRsvp(null);
      } finally {
        setIsLoading(false);
      }
    };
    fetchRsvp();
  }, [weddingId]);

  const handleDelete = async () => {
    if (!existingRsvp) return;
    setIsDeleting(true);
    try {
      await rsvpApi.remove(existingRsvp.id);
      setExistingRsvp(null);
      toast.success("참석 정보가 삭제되었습니다");
    } catch (err) {
      if (isAxiosError(err)) {
        const msg = err.response?.data?.status?.message;
        toast.error(msg ?? "삭제에 실패했습니다");
      } else {
        toast.error("삭제에 실패했습니다");
      }
    } finally {
      setIsDeleting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center py-16">
        <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (existingRsvp && !isEditing) {
    const isAttending = existingRsvp.attendance === "YES";
    return (
      <div className="space-y-4">
        <div className="bg-white rounded-2xl border border-gray-100 p-5 space-y-4">
          <div className="flex items-center gap-2">
            <IoCheckmarkCircle
              className={`text-xl ${isAttending ? "text-primary" : "text-gray-400"}`}
            />
            <span
              className={`text-sm font-semibold ${isAttending ? "text-primary" : "text-gray-500"}`}
            >
              {isAttending ? "참석" : "불참"}
            </span>
          </div>

          <div className="space-y-2 text-sm text-gray-700">
            <div className="flex justify-between">
              <span className="text-gray-400">성함</span>
              <span className="font-medium">{existingRsvp.name}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-400">측</span>
              <span>{SIDE_LABEL[existingRsvp.side]}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-400">연락처</span>
              <span>{existingRsvp.phone}</span>
            </div>
            {isAttending && (
              <>
                <div className="flex justify-between">
                  <span className="text-gray-400">참석 인원</span>
                  <span>{existingRsvp.attendeeCount}명</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">식사 여부</span>
                  <span>
                    {existingRsvp.meal.willEat
                      ? `예 (${existingRsvp.meal.mealCount}명)`
                      : "아니오"}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">셔틀버스</span>
                  <span>
                    {existingRsvp.shuttle.willRide
                      ? `예 (${existingRsvp.shuttle.rideCount}명)`
                      : "아니오"}
                  </span>
                </div>
              </>
            )}
            {existingRsvp.note && (
              <div className="flex justify-between">
                <span className="text-gray-400">전달사항</span>
                <span className="text-right max-w-[60%]">{existingRsvp.note}</span>
              </div>
            )}
          </div>
        </div>

        <div className="flex gap-3">
          <button
            onClick={() => setIsEditing(true)}
            className="flex-1 flex items-center justify-center gap-1.5 py-3 rounded-xl border border-gray-200 text-sm text-gray-600 hover:bg-gray-50 transition-colors cursor-pointer"
          >
            <IoPencilOutline size={15} />
            수정하기
          </button>
          <button
            onClick={handleDelete}
            disabled={isDeleting}
            className="flex-1 flex items-center justify-center gap-1.5 py-3 rounded-xl border border-red-100 text-sm text-red-400 hover:bg-red-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors cursor-pointer"
          >
            <IoCloseCircleOutline size={15} />
            {isDeleting ? "삭제 중..." : "삭제하기"}
          </button>
        </div>
      </div>
    );
  }

  return (
    <RsvpForm
      weddingId={weddingId}
      existingRsvp={existingRsvp ?? undefined}
      onSuccess={(rsvp) => {
        setExistingRsvp(rsvp);
        setIsEditing(false);
      }}
      onCancel={existingRsvp ? () => setIsEditing(false) : undefined}
    />
  );
};

export default RsvpSection;
