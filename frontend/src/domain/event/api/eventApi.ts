import axiosInstance from "../../../global/api/axiosInstance";
import { EVENT_API } from "../event.constants";
import type {
  EventInfoResponse,
  EventResponse,
  EventRequest,
  WeddingRequest,
  WeddingResponse,
  HostRequest,
  HostResponse,
  ScheduleRequest,
  ScheduleResponse,
  AccountGroupRequest,
  AccountGroupResponse,
  AccountRequest,
  AccountResponse,
  HeroImageRequest,
  HeroImageResponse,
  TransportationRequest,
  TransportationResponse,
  AnnouncementRequest,
  AnnouncementResponse,
  CheckSlugResponse,
} from "../types";

export const eventApi = {
  // ─── Event ───

  createEvent: async (request: EventRequest): Promise<EventResponse> => {
    const { data } = await axiosInstance.post<EventResponse>(
      EVENT_API.BASE,
      request,
    );
    return data;
  },

  getEvent: async (eventId: number): Promise<EventResponse> => {
    const { data } = await axiosInstance.get<EventResponse>(
      EVENT_API.DETAIL(eventId),
    );
    return data;
  },

  updateEvent: async (
    eventId: number,
    request: EventRequest,
  ): Promise<EventResponse> => {
    const { data } = await axiosInstance.put<EventResponse>(
      EVENT_API.DETAIL(eventId),
      request,
    );
    return data;
  },

  deleteEvent: async (eventId: number): Promise<void> => {
    await axiosInstance.delete(EVENT_API.DETAIL(eventId));
  },

  checkSlug: async (slug: string): Promise<CheckSlugResponse> => {
    const { data } = await axiosInstance.post<CheckSlugResponse>(
      EVENT_API.CHECK_SLUG,
      { slug },
    );
    return data;
  },

  getEventInfo: async (slug: string): Promise<EventInfoResponse> => {
    const { data } = await axiosInstance.get<EventInfoResponse>(
      EVENT_API.INFO(slug),
    );
    return data;
  },

  // ─── HeroImage ───

  addHeroImage: async (
    eventId: number,
    request: HeroImageRequest,
  ): Promise<HeroImageResponse> => {
    const { data } = await axiosInstance.post<HeroImageResponse>(
      EVENT_API.HERO_IMAGES(eventId),
      request,
    );
    return data;
  },

  getHeroImages: async (eventId: number): Promise<HeroImageResponse[]> => {
    const { data } = await axiosInstance.get<HeroImageResponse[]>(
      EVENT_API.HERO_IMAGES(eventId),
    );
    return data;
  },

  deleteHeroImage: async (imageId: number): Promise<void> => {
    await axiosInstance.delete(EVENT_API.HERO_IMAGE(imageId));
  },

  // ─── Transportation ───

  addTransportation: async (
    eventId: number,
    request: TransportationRequest,
  ): Promise<TransportationResponse> => {
    const { data } = await axiosInstance.post<TransportationResponse>(
      EVENT_API.TRANSPORTATION(eventId),
      request,
    );
    return data;
  },

  updateTransportation: async (
    transportId: number,
    request: TransportationRequest,
  ): Promise<TransportationResponse> => {
    const { data } = await axiosInstance.put<TransportationResponse>(
      EVENT_API.TRANSPORTATION_DETAIL(transportId),
      request,
    );
    return data;
  },

  deleteTransportation: async (transportId: number): Promise<void> => {
    await axiosInstance.delete(EVENT_API.TRANSPORTATION_DETAIL(transportId));
  },

  // ─── Announcement ───

  addAnnouncement: async (
    eventId: number,
    request: AnnouncementRequest,
  ): Promise<AnnouncementResponse> => {
    const { data } = await axiosInstance.post<AnnouncementResponse>(
      EVENT_API.ANNOUNCEMENTS(eventId),
      request,
    );
    return data;
  },

  updateAnnouncement: async (
    announcementId: number,
    request: AnnouncementRequest,
  ): Promise<AnnouncementResponse> => {
    const { data } = await axiosInstance.put<AnnouncementResponse>(
      EVENT_API.ANNOUNCEMENT_DETAIL(announcementId),
      request,
    );
    return data;
  },

  deleteAnnouncement: async (announcementId: number): Promise<void> => {
    await axiosInstance.delete(EVENT_API.ANNOUNCEMENT_DETAIL(announcementId));
  },

  // ─── Wedding ───

  createWedding: async (request: WeddingRequest): Promise<WeddingResponse> => {
    const { data } = await axiosInstance.post<WeddingResponse>(
      EVENT_API.WEDDING_BASE,
      request,
    );
    return data;
  },

  getWedding: async (weddingId: number): Promise<WeddingResponse> => {
    const { data } = await axiosInstance.get<WeddingResponse>(
      EVENT_API.WEDDING_DETAIL(weddingId),
    );
    return data;
  },

  updateWedding: async (
    weddingId: number,
    request: WeddingRequest,
  ): Promise<WeddingResponse> => {
    const { data } = await axiosInstance.put<WeddingResponse>(
      EVENT_API.WEDDING_DETAIL(weddingId),
      request,
    );
    return data;
  },

  // ─── Host ───

  createHost: async (
    eventId: number,
    request: HostRequest,
  ): Promise<HostResponse> => {
    const { data } = await axiosInstance.post<HostResponse>(
      EVENT_API.HOSTS(eventId),
      request,
    );
    return data;
  },

  updateHost: async (
    hostId: number,
    request: HostRequest,
  ): Promise<HostResponse> => {
    const { data } = await axiosInstance.put<HostResponse>(
      EVENT_API.HOST_DETAIL(hostId),
      request,
    );
    return data;
  },

  deleteHost: async (hostId: number): Promise<void> => {
    await axiosInstance.delete(EVENT_API.HOST_DETAIL(hostId));
  },

  // ─── Schedule ───

  createSchedule: async (
    weddingId: number,
    request: ScheduleRequest,
  ): Promise<ScheduleResponse> => {
    const { data } = await axiosInstance.post<ScheduleResponse>(
      EVENT_API.SCHEDULES(weddingId),
      request,
    );
    return data;
  },

  updateSchedule: async (
    scheduleId: number,
    request: ScheduleRequest,
  ): Promise<ScheduleResponse> => {
    const { data } = await axiosInstance.put<ScheduleResponse>(
      EVENT_API.SCHEDULE_DETAIL(scheduleId),
      request,
    );
    return data;
  },

  deleteSchedule: async (scheduleId: number): Promise<void> => {
    await axiosInstance.delete(EVENT_API.SCHEDULE_DETAIL(scheduleId));
  },

  // ─── AccountGroup ───

  createAccountGroup: async (
    weddingId: number,
    request: AccountGroupRequest,
  ): Promise<AccountGroupResponse> => {
    const { data } = await axiosInstance.post<AccountGroupResponse>(
      EVENT_API.ACCOUNT_GROUPS(weddingId),
      request,
    );
    return data;
  },

  updateAccountGroup: async (
    groupId: number,
    request: AccountGroupRequest,
  ): Promise<AccountGroupResponse> => {
    const { data } = await axiosInstance.put<AccountGroupResponse>(
      EVENT_API.ACCOUNT_GROUP_DETAIL(groupId),
      request,
    );
    return data;
  },

  deleteAccountGroup: async (groupId: number): Promise<void> => {
    await axiosInstance.delete(EVENT_API.ACCOUNT_GROUP_DETAIL(groupId));
  },

  // ─── Account ───

  createAccount: async (
    groupId: number,
    request: AccountRequest,
  ): Promise<AccountResponse> => {
    const { data } = await axiosInstance.post<AccountResponse>(
      EVENT_API.ACCOUNTS(groupId),
      request,
    );
    return data;
  },

  deleteAccount: async (accountId: number): Promise<void> => {
    await axiosInstance.delete(EVENT_API.ACCOUNT_DETAIL(accountId));
  },

  // ─── File Upload ───

  uploadFile: async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append("file", file);
    const { data } = await axiosInstance.post<{ url: string }>(
      "/api/files/upload",
      formData,
      { headers: { "Content-Type": "multipart/form-data" } },
    );
    return data.url;
  },
};
