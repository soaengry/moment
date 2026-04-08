import { type FC, useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast, ToastContainer } from "react-toastify";
import { eventApi } from "../api/eventApi";
import { scheduleApi } from "../../schedule/api/scheduleApi";
import { useAuthStore } from "../../auth/store/useAuthStore";
import type { EventRequest, EventType } from "../types";
import TemplateSelectStep from "../components/create/TemplateSelectStep";
import BasicInfoStep from "../components/create/BasicInfoStep";
import CoupleStep from "../components/create/CoupleStep";
import ScheduleStep from "../components/create/ScheduleStep";
import AccountStep from "../components/create/AccountStep";
import ExtraInfoStep, { type ExtraInfoFormData } from "../components/create/ExtraInfoStep";
import {
  useEventSteps,
  initialEventFormState,
  type EventFormState,
} from "../hooks/useEventSteps";
import StepIndicator from "../components/create/StepIndicator";

const STEP_LABELS: Record<EventType, string[]> = {
  WEDDING:   ["템플릿", "기본 정보", "신랑신부", "식순", "계좌 정보", "추가 정보"],
  GATHERING: ["템플릿", "기본 정보", "주최자",   "일정",   "계좌 정보", "추가 정보"],
};

const EventCreatePage: FC = () => {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const [formState, setFormState] = useState<EventFormState>(initialEventFormState);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const steps = STEP_LABELS[formState.eventType];

  const {
    step,
    setStep,
    handlePrev,
    handleBasicSubmit,
    handleCoupleSubmit,
    handleScheduleSubmit,
    handleAccountSubmit,
    buildExtraState,
  } = useEventSteps(formState, setFormState, { totalSteps: steps.length });

  const handleTemplateSubmit = (eventType: EventType) => {
    setFormState((prev) => ({ ...prev, eventType }));
    // manually advance since the hook's handleNext uses the stale steps.length
    setStep((s) => s + 1);
  };

  const handleExtraSubmit = async (extra: ExtraInfoFormData) => {
    const state = buildExtraState(extra);
    setFormState(state);
    await submitAll(state);
  };

  const submitAll = async (state: EventFormState) => {
    if (!state.basic) return;
    setIsSubmitting(true);
    try {
      const event = await eventApi.createEvent({ ...state.basic, type: state.eventType });
      const { id: eventId, slug } = event;

      const wedding = await eventApi.createWedding({
        eventId,
        notice: state.notice || undefined,
        parkingInfo: state.parkingInfo || undefined,
        mealInfo: state.mealInfo || undefined,
      });

      for (const couple of state.couples) {
        await eventApi.createHost(eventId, couple);
      }
      for (const schedule of state.schedules) {
        await eventApi.createSchedule(wedding.id, schedule);
      }
      for (const groupData of state.accountGroups) {
        const group = await eventApi.createAccountGroup(wedding.id, {
          groupName: groupData.groupName,
          orderIndex: groupData.orderIndex,
        });
        for (const account of groupData.accounts) {
          await eventApi.createAccount(group.id, account);
        }
      }
      for (let i = 0; i < state.landingPhotos.length; i++) {
        const photo = state.landingPhotos[i];
        const imageUrl = await eventApi.uploadFile(photo.file!);
        await eventApi.addHeroImage(eventId, { imageUrl, orderIndex: i });
      }
      for (const transport of state.transportation) {
        await eventApi.addTransportation(eventId, transport);
      }
      for (const announcement of state.announcements) {
        await eventApi.addAnnouncement(eventId, announcement);
      }

      try {
        await scheduleApi.addAttendance({ slug });
      } catch {
        // 자동 등록 실패 시 무시
      }

      toast.success("초대장이 생성되었습니다!");
      setTimeout(() => navigate(`/event/${slug}`), 1500);
    } catch {
      toast.error("초대장 생성에 실패했습니다. 다시 시도해주세요.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const stepComponents = [
    <TemplateSelectStep
      initialType={formState.eventType}
      onSubmit={handleTemplateSubmit}
    />,
    <BasicInfoStep
      initialData={formState.basic}
      templateType={formState.eventType}
      onSubmit={handleBasicSubmit}
    />,
    <CoupleStep
      initialData={formState.couples}
      initialPhotos={formState.landingPhotos}
      templateType={formState.eventType}
      currentUser={user ?? undefined}
      onSubmit={handleCoupleSubmit}
      onBack={handlePrev}
    />,
    <ScheduleStep
      initialData={formState.schedules}
      templateType={formState.eventType}
      onSubmit={handleScheduleSubmit}
      onBack={handlePrev}
    />,
    <AccountStep
      initialData={formState.accountGroups}
      templateType={formState.eventType}
      onSubmit={handleAccountSubmit}
      onBack={handlePrev}
    />,
    <ExtraInfoStep
      initialData={{
        transportation: formState.transportation,
        announcements: formState.announcements,
        notice: formState.notice,
        parkingInfo: formState.parkingInfo,
        mealInfo: formState.mealInfo,
      }}
      templateType={formState.eventType}
      onSubmit={handleExtraSubmit}
      onBack={handlePrev}
      isSubmitting={isSubmitting}
    />,
  ];

  return (
    <div className="min-h-screen bg-bgPrimary">
      <div className="max-w-lg mx-auto px-4 py-6">
        <h1 className="text-xl font-bold text-gray-800 mb-6 text-center">
          초대장 만들기
        </h1>

        <StepIndicator steps={steps} currentStep={step} onStepClick={setStep} />

        {stepComponents[step]}
      </div>

      <ToastContainer
        position="bottom-center"
        autoClose={2000}
        hideProgressBar
        closeOnClick
        pauseOnHover={false}
        toastClassName="text-sm"
      />
    </div>
  );
};

export default EventCreatePage;
