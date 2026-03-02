import axiosInstance from "../../../global/api/axiosInstance";
import type { ChatMessage, PageResponse } from "../types";

export const chatApi = {
  getMessages: async (
    weddingId: number,
    page = 0,
    size = 50
  ): Promise<PageResponse<ChatMessage>> => {
    const { data } = await axiosInstance.get<PageResponse<ChatMessage>>(
      `/api/weddings/${weddingId}/chat/messages?page=${page}&size=${size}`
    );
    return data;
  },

  uploadChatImage: async (
    weddingId: number,
    file: File
  ): Promise<string> => {
    const formData = new FormData();
    formData.append("file", file);
    const { data } = await axiosInstance.post<{ imageUrl: string }>(
      `/api/weddings/${weddingId}/chat/images`,
      formData,
      { headers: { "Content-Type": "multipart/form-data" } }
    );
    return data.imageUrl;
  },
};
