import axiosInstance from "../../../global/api/axiosInstance";
import type { ChatMessage, PageResponse } from "../types";
import { CHAT_API } from "../chat.constants";

export const chatApi = {
  getMessages: async (
    eventId: number,
    page = 0,
    size = 50,
  ): Promise<PageResponse<ChatMessage>> => {
    const { data } = await axiosInstance.get<PageResponse<ChatMessage>>(
      `${CHAT_API.MESSAGES(eventId)}?page=${page}&size=${size}`,
    );
    return data;
  },

  uploadChatImage: async (eventId: number, file: File): Promise<string> => {
    const formData = new FormData();
    formData.append("file", file);
    const { data } = await axiosInstance.post<{ imageUrl: string }>(
      CHAT_API.UPLOAD_IMAGE(eventId),
      formData,
      { headers: { "Content-Type": "multipart/form-data" } },
    );
    return data.imageUrl;
  },
};
