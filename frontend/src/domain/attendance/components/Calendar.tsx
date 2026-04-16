import { type FC, useState, useEffect, useCallback } from "react";
import { toast } from "react-toastify";
import { isAxiosError } from "axios";
import { attendanceApi } from "../api/attendanceApi";
import type { AttendanceResponse } from "../types";
import CalendarHeader from "./CalendarHeader";
import CalendarGrid from "./CalendarGrid";
import AddScheduleModal from "./AddAttendanceModal";
import WeddingDetailModal from "./EventDetailModal";

const Calendar: FC = () => {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth());
  const [attendances, setAttendances] = useState<AttendanceResponse[]>([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [selectedAttendance, setSelectedAttendance] =
    useState<AttendanceResponse | null>(null);
  const [isAdding, setIsAdding] = useState(false);

  const fetchAttendances = useCallback(async () => {
    try {
      const data = await attendanceApi.getMyAttendances();
      setAttendances(data);
    } catch {
      toast.error("일정을 불러오는데 실패했습니다");
    }
  }, []);

  useEffect(() => {
    fetchAttendances();
  }, [fetchAttendances]);

  const handlePrevMonth = () => {
    if (month === 0) {
      setYear((y) => y - 1);
      setMonth(11);
    } else {
      setMonth((m) => m - 1);
    }
  };

  const handleNextMonth = () => {
    if (month === 11) {
      setYear((y) => y + 1);
      setMonth(0);
    } else {
      setMonth((m) => m + 1);
    }
  };

  const handleAdd = async (slug: string) => {
    setIsAdding(true);
    try {
      const newAttendance = await attendanceApi.addAttendance({ slug });
      setAttendances((prev) => [newAttendance, ...prev]);
      setShowAddModal(false);
      toast.success("일정이 등록되었습니다");
    } catch (err) {
      if (isAxiosError(err) && err.response) {
        const status = err.response.status;
        if (status === 404) {
          toast.error("존재하지 않는 초대장입니다");
        } else if (status === 409) {
          toast.error("이미 등록된 일정입니다");
        } else {
          toast.error("일정 등록에 실패했습니다");
        }
      } else {
        toast.error("일정 등록에 실패했습니다");
      }
    } finally {
      setIsAdding(false);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await attendanceApi.deleteAttendance(id);
      setAttendances((prev) => prev.filter((a) => a.id !== id));
      setSelectedAttendance(null);
      toast.success("일정이 삭제되었습니다");
    } catch {
      toast.error("일정 삭제에 실패했습니다");
    }
  };

  return (
    <div>
      <CalendarHeader
        year={year}
        month={month}
        onPrevMonth={handlePrevMonth}
        onNextMonth={handleNextMonth}
        onAddClick={() => setShowAddModal(true)}
      />
      <CalendarGrid
        year={year}
        month={month}
        attendances={attendances}
        onBlockClick={setSelectedAttendance}
      />

      {showAddModal && (
        <AddScheduleModal
          onClose={() => setShowAddModal(false)}
          onSubmit={handleAdd}
          isLoading={isAdding}
        />
      )}

      {selectedAttendance && (
        <WeddingDetailModal
          attendance={selectedAttendance}
          onClose={() => setSelectedAttendance(null)}
          onDelete={handleDelete}
        />
      )}
    </div>
  );
};

export default Calendar;
