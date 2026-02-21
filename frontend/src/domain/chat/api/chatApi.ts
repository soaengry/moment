import axiosInstance from "../../../global/api/axiosInstance";
import type { ChatRoom, ChatMessage, PageResponse } from "../types";

export const chatApi = {
  getRooms: async (weddingId: number): Promise<ChatRoom[]> => {
    const { data } = await axiosInstance.get<ChatRoom[]>(
      `/api/weddings/${weddingId}/chat/rooms`
    );
    return data;
  },

  createRoom: async (weddingId: number, name: string): Promise<ChatRoom> => {
    const { data } = await axiosInstance.post<ChatRoom>(
      `/api/weddings/${weddingId}/chat/rooms`,
      { name }
    );
    return data;
  },

  getMessages: async (
    weddingId: number,
    roomId: number,
    page = 0,
    size = 50
  ): Promise<PageResponse<ChatMessage>> => {
    const { data } = await axiosInstance.get<PageResponse<ChatMessage>>(
      `/api/weddings/${weddingId}/chat/rooms/${roomId}/messages?page=${page}&size=${size}`
    );
    return data;
  },
};
