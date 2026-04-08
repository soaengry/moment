import { useState } from "react";
import type {
  EventRequest,
  HostRequest,
  ScheduleRequest,
  TransportationRequest,
  AnnouncementRequest,
  EventType,
} from "../types";
import type { LandingPhoto } from "../components/create/CoupleStep";
import type { AccountGroupFormData } from "../components/create/AccountStep";
import type { ExtraInfoFormData } from "../components/create/ExtraInfoStep";

export interface EventFormState {
  eventType: EventType;
  basic: EventRequest | null;
  couples: HostRequest[];
  landingPhotos: LandingPhoto[];
  schedules: ScheduleRequest[];
  accountGroups: AccountGroupFormData[];
  transportation: TransportationRequest[];
  announcements: AnnouncementRequest[];
  notice: string;
  parkingInfo: string;
  mealInfo: string;
}

export const initialEventFormState: EventFormState = {
  eventType: "GATHERING",
  basic: null,
  couples: [],
  landingPhotos: [],
  schedules: [],
  accountGroups: [],
  transportation: [],
  announcements: [],
  notice: "",
  parkingInfo: "",
  mealInfo: "",
};

interface UseEventStepsOptions {
  totalSteps: number;
}

export function useEventSteps(
  formState: EventFormState,
  setFormState: React.Dispatch<React.SetStateAction<EventFormState>>,
  { totalSteps }: UseEventStepsOptions,
) {
  const [step, setStep] = useState(0);

  const handleNext = () => setStep((s) => Math.min(s + 1, totalSteps - 1));
  const handlePrev = () => setStep((s) => Math.max(s - 1, 0));

  const handleBasicSubmit = (data: EventRequest) => {
    setFormState((prev) => ({ ...prev, basic: data }));
    handleNext();
  };

  const handleCoupleSubmit = (couples: HostRequest[], photos: LandingPhoto[]) => {
    setFormState((prev) => ({ ...prev, couples, landingPhotos: photos }));
    handleNext();
  };

  const handleScheduleSubmit = (schedules: ScheduleRequest[]) => {
    setFormState((prev) => ({ ...prev, schedules }));
    handleNext();
  };

  const handleAccountSubmit = (accountGroups: AccountGroupFormData[]) => {
    setFormState((prev) => ({ ...prev, accountGroups }));
    handleNext();
  };

  const buildExtraState = (extra: ExtraInfoFormData): EventFormState => ({
    ...formState,
    transportation: extra.transportation,
    announcements: extra.announcements,
    notice: extra.notice,
    parkingInfo: extra.parkingInfo,
    mealInfo: extra.mealInfo,
  });

  return {
    step,
    setStep,
    handleNext,
    handlePrev,
    handleBasicSubmit,
    handleCoupleSubmit,
    handleScheduleSubmit,
    handleAccountSubmit,
    buildExtraState,
  };
}
