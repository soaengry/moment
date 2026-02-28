import axiosInstance from "../../../global/api/axiosInstance";
import { SCHEDULE_API } from "../schedule.constants";
import type { AttendanceResponse, AddAttendanceRequest } from "../types";

export const scheduleApi = {
  getMyAttendances: async (): Promise<AttendanceResponse[]> => {
    const { data } = await axiosInstance.get<AttendanceResponse[]>(
      SCHEDULE_API.BASE,
    );
    return data;
  },

  addAttendance: async (
    request: AddAttendanceRequest,
  ): Promise<AttendanceResponse> => {
    const { data } = await axiosInstance.post<AttendanceResponse>(
      SCHEDULE_API.BASE,
      request,
    );
    return data;
  },

  deleteAttendance: async (id: number): Promise<void> => {
    await axiosInstance.delete(SCHEDULE_API.DETAIL(id));
  },
};
