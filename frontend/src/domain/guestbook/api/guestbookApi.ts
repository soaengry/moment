import axiosInstance from "../../../global/api/axiosInstance";
import type { GuestbookEntry, GuestbookRequest, PageResponse } from "../types";

export const guestbookApi = {
  getEntries: async (weddingId: number, page = 0, size = 20): Promise<PageResponse<GuestbookEntry>> => {
    const { data } = await axiosInstance.get<PageResponse<GuestbookEntry>>(
      `/api/weddings/${weddingId}/guestbook?page=${page}&size=${size}`
    );
    return data;
  },

  createEntry: async (weddingId: number, request: GuestbookRequest): Promise<GuestbookEntry> => {
    const { data } = await axiosInstance.post<GuestbookEntry>(
      `/api/weddings/${weddingId}/guestbook`,
      request
    );
    return data;
  },

  updateEntry: async (weddingId: number, entryId: number, request: GuestbookRequest): Promise<GuestbookEntry> => {
    const { data } = await axiosInstance.put<GuestbookEntry>(
      `/api/weddings/${weddingId}/guestbook/${entryId}`,
      request
    );
    return data;
  },

  deleteEntry: async (weddingId: number, entryId: number, password?: string): Promise<void> => {
    const params = password ? `?password=${encodeURIComponent(password)}` : "";
    await axiosInstance.delete(`/api/weddings/${weddingId}/guestbook/${entryId}${params}`);
  },
};
