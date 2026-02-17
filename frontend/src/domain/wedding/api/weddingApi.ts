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
