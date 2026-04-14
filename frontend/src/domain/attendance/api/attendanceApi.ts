import axiosInstance from "../../../global/api/axiosInstance";
import { ATTENDANCE_API } from "../attendance.constants";
import type { AttendanceResponse, AddAttendanceRequest } from "../types";

export const attendanceApi = {
  getMyAttendances: async (): Promise<AttendanceResponse[]> => {
    const { data } = await axiosInstance.get<AttendanceResponse[]>(
      ATTENDANCE_API.BASE,
    );
    return data;
  },

  addAttendance: async (
    request: AddAttendanceRequest,
  ): Promise<AttendanceResponse> => {
    const { data } = await axiosInstance.post<AttendanceResponse>(
      ATTENDANCE_API.BASE,
      request,
    );
    return data;
  },

  deleteAttendance: async (id: number): Promise<void> => {
    await axiosInstance.delete(ATTENDANCE_API.DETAIL(id));
  },
};
