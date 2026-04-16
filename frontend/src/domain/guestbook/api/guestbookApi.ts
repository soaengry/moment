import axiosInstance from "../../../global/api/axiosInstance";
import { GUESTBOOK_API } from "../guestbook.constants";
import type { GuestbookEntry, GuestbookRequest, PageResponse } from "../types";

export const guestbookApi = {
  getEntries: async (weddingId: number, page = 0, size = 20): Promise<PageResponse<GuestbookEntry>> => {
    const { data } = await axiosInstance.get<PageResponse<GuestbookEntry>>(
      `${GUESTBOOK_API.ENTRIES(weddingId)}?page=${page}&size=${size}`
    );
    return data;
  },

  createEntry: async (weddingId: number, request: GuestbookRequest): Promise<GuestbookEntry> => {
    const { data } = await axiosInstance.post<GuestbookEntry>(
      GUESTBOOK_API.ENTRIES(weddingId),
      request
    );
    return data;
  },

  updateEntry: async (weddingId: number, entryId: number, request: GuestbookRequest): Promise<GuestbookEntry> => {
    const { data } = await axiosInstance.put<GuestbookEntry>(
      GUESTBOOK_API.ENTRY(weddingId, entryId),
      request
    );
    return data;
  },

  deleteEntry: async (weddingId: number, entryId: number, password?: string): Promise<void> => {
    const params = password ? `?password=${encodeURIComponent(password)}` : "";
    await axiosInstance.delete(`${GUESTBOOK_API.ENTRY(weddingId, entryId)}${params}`);
  },

  verifyPassword: async (weddingId: number, entryId: number, password: string): Promise<void> => {
    await axiosInstance.post(GUESTBOOK_API.VERIFY_PASSWORD(weddingId, entryId), { password });
  },
};
