import axiosInstance from "../../../global/api/axiosInstance";
import { WEDDING_API } from "../wedding.constants";
import type { WeddingInfoResponse } from "../types";

export const weddingApi = {
  getWeddingInfo: async (weddingId: number): Promise<WeddingInfoResponse> => {
    const { data } = await axiosInstance.get<WeddingInfoResponse>(
      WEDDING_API.INFO(weddingId),
    );
    return data;
  },
};
