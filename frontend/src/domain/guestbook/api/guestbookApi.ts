import axiosInstance from "../../../global/api/axiosInstance";
import { GUESTBOOK_API } from "../guestbook.constants";
import type { GuestbookEntry, GuestbookRequest, CursorPageResponse } from "../types";

export const guestbookApi = {
  getEntries: async (weddingId: number, cursor?: number, size = 20): Promise<CursorPageResponse<GuestbookEntry>> => {
    const params = new URLSearchParams({ size: String(size) });
    if (cursor != null) params.set("cursor", String(cursor));
    const { data } = await axiosInstance.get<CursorPageResponse<GuestbookEntry>>(
      `${GUESTBOOK_API.ENTRIES(weddingId)}?${params}`
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
