import axiosInstance from "../../../global/api/axiosInstance";
import { WEDDING_API } from "../wedding.constants";
import type {
  WeddingInfoResponse,
  WeddingResponse,
  WeddingRequest,
  CoupleRequest,
  CoupleResponse,
  ScheduleRequest,
  ScheduleResponse,
  AccountGroupRequest,
  AccountGroupResponse,
  AccountRequest,
  AccountResponse,
  TransportationRequest,
  TransportationResponse,
  AnnouncementRequest,
  AnnouncementResponse,
} from "../types";

export const weddingApi = {
  // Wedding
  getWeddingInfo: async (weddingId: number): Promise<WeddingInfoResponse> => {
    const { data } = await axiosInstance.get<WeddingInfoResponse>(
      WEDDING_API.INFO(weddingId),
    );
    return data;
  },

  createWedding: async (request: WeddingRequest): Promise<WeddingResponse> => {
    const { data } = await axiosInstance.post<WeddingResponse>(
      WEDDING_API.BASE,
      request,
    );
    return data;
  },

  updateWedding: async (weddingId: number, request: WeddingRequest): Promise<WeddingResponse> => {
    const { data } = await axiosInstance.put<WeddingResponse>(
      WEDDING_API.DETAIL(weddingId),
      request,
    );
    return data;
  },

  deleteWedding: async (weddingId: number): Promise<void> => {
    await axiosInstance.delete(WEDDING_API.DETAIL(weddingId));
  },

  // Couple
  createCouple: async (
    weddingId: number,
    request: CoupleRequest,
  ): Promise<CoupleResponse> => {
    const { data } = await axiosInstance.post<CoupleResponse>(
      WEDDING_API.COUPLES(weddingId),
      request,
    );
    return data;
  },

  // Schedule
  createSchedule: async (
    weddingId: number,
    request: ScheduleRequest,
  ): Promise<ScheduleResponse> => {
    const { data } = await axiosInstance.post<ScheduleResponse>(
      WEDDING_API.SCHEDULES(weddingId),
      request,
    );
    return data;
  },

  // Account Group + Account
  createAccountGroup: async (
    weddingId: number,
    request: AccountGroupRequest,
  ): Promise<AccountGroupResponse> => {
    const { data } = await axiosInstance.post<AccountGroupResponse>(
      WEDDING_API.ACCOUNT_GROUPS(weddingId),
      request,
    );
    return data;
  },

  createAccount: async (
    groupId: number,
    request: AccountRequest,
  ): Promise<AccountResponse> => {
    const { data } = await axiosInstance.post<AccountResponse>(
      WEDDING_API.ACCOUNTS(groupId),
      request,
    );
    return data;
  },

  // Transportation
  createTransportation: async (
    weddingId: number,
    request: TransportationRequest,
  ): Promise<TransportationResponse> => {
    const { data } = await axiosInstance.post<TransportationResponse>(
      WEDDING_API.TRANSPORTATION(weddingId),
      request,
    );
    return data;
  },

  // Update/Delete for sub-resources
  updateCouple: async (coupleId: number, request: CoupleRequest): Promise<CoupleResponse> => {
    const { data } = await axiosInstance.put<CoupleResponse>(`/api/weddings/couples/${coupleId}`, request);
    return data;
  },

  deleteCouple: async (coupleId: number): Promise<void> => {
    await axiosInstance.delete(`/api/weddings/couples/${coupleId}`);
  },

  updateSchedule: async (scheduleId: number, request: ScheduleRequest): Promise<ScheduleResponse> => {
    const { data } = await axiosInstance.put<ScheduleResponse>(`/api/weddings/schedules/${scheduleId}`, request);
    return data;
  },

  deleteSchedule: async (scheduleId: number): Promise<void> => {
    await axiosInstance.delete(`/api/weddings/schedules/${scheduleId}`);
  },

  deleteAccountGroup: async (groupId: number): Promise<void> => {
    await axiosInstance.delete(`/api/weddings/account-groups/${groupId}`);
  },

  deleteAccount: async (accountId: number): Promise<void> => {
    await axiosInstance.delete(`/api/weddings/accounts/${accountId}`);
  },

  deleteTransportation: async (transportationId: number): Promise<void> => {
    await axiosInstance.delete(`/api/weddings/transportation/${transportationId}`);
  },

  deleteAnnouncement: async (announcementId: number): Promise<void> => {
    await axiosInstance.delete(`/api/weddings/announcements/${announcementId}`);
  },

  // Announcement
  createAnnouncement: async (
    weddingId: number,
    request: AnnouncementRequest,
  ): Promise<AnnouncementResponse> => {
    const { data } = await axiosInstance.post<AnnouncementResponse>(
      WEDDING_API.ANNOUNCEMENTS(weddingId),
      request,
    );
    return data;
  },
};
