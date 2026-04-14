import axiosInstance from "../../../global/api/axiosInstance";
import { EVENT_API } from "../event.constants";
import type {
  EventInfoResponse,
  EventFullCreateRequest,
  CheckSlugResponse,
} from "../types";

export const eventApi = {
  createEventWithDetails: async (
    request: EventFullCreateRequest,
  ): Promise<EventInfoResponse> => {
    const { data } = await axiosInstance.post<EventInfoResponse>(
      EVENT_API.BASE,
      request,
    );
    return data;
  },

  updateEventWithDetails: async (
    eventId: number,
    request: EventFullCreateRequest,
  ): Promise<EventInfoResponse> => {
    const { data } = await axiosInstance.put<EventInfoResponse>(
      EVENT_API.UPDATE_DETAILS(eventId),
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
