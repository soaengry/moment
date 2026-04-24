import axios from "axios";
import axiosInstance from "../../../global/api/axiosInstance";
import { ENV } from "../../../global/config/env";
import { RSVP_API, RSVP_VALIDATION } from "../rsvp.constants";
import type {
  RsvpCreateRequest,
  RsvpUpdateRequest,
  RsvpResponse,
  RsvpStatsResponse,
  RsvpListResponse,
} from "../types";

export const rsvpApi = {
  create(data: RsvpCreateRequest) {
    return axiosInstance.post<RsvpResponse>(RSVP_API.BASE, data);
  },

  getMyRsvp(weddingId: number) {
    return axiosInstance.get<RsvpResponse | null>(RSVP_API.ME, { params: { weddingId } });
  },

  update(id: number, data: RsvpUpdateRequest) {
    return axiosInstance.put<RsvpResponse>(`${RSVP_API.BASE}/${id}`, data);
  },

  remove(id: number) {
    return axiosInstance.delete(`${RSVP_API.BASE}/${id}`);
  },

  getStats(weddingId: number) {
    return axiosInstance.get<RsvpStatsResponse>(RSVP_API.STATS, { params: { weddingId } });
  },

  getList(weddingId: number, page: number) {
    return axiosInstance.get<RsvpListResponse>(RSVP_API.LIST, {
      params: { weddingId, page, size: RSVP_VALIDATION.PAGE_SIZE },
    });
  },

  exportCsv(weddingId: number) {
    return axios.get(`${ENV.API_BASE_URL}${RSVP_API.EXPORT}`, {
      params: { weddingId },
      responseType: "blob",
      withCredentials: true,
    });
  },
};
