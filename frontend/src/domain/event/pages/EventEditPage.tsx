import { type FC, useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { toast, ToastContainer } from "react-toastify";
import { IoArrowBack } from "react-icons/io5";
import { useScrollVisibility } from "../../../global/hooks/useScrollVisibility";
import { eventApi } from "../api/eventApi";
import type {
  EventRequest,
  HostRequest,
  ScheduleRequest,
  TransportationRequest,
  AnnouncementRequest,
  EventType,
  WeddingDetailResponse,
  GatheringDetailResponse,
} from "../types";
import { isWeddingDetail } from "../types";
import { useEventDetail } from "../hooks/useEventDetail";
import {
  useEventSteps,
  type EventFormState,
} from "../hooks/useEventSteps";
import BasicInfoStep from "../components/create/BasicInfoStep";
import CoupleStep from "../components/create/CoupleStep";
import ScheduleStep from "../components/create/ScheduleStep";
import AccountStep, { type AccountGroupFormData } from "../components/create/AccountStep";
import ExtraInfoStep, { type ExtraInfoFormData } from "../components/create/ExtraInfoStep";
import StepIndicator from "../components/create/StepIndicator";
import type { LandingPhoto } from "../components/create/CoupleStep";

const STEP_LABELS: Record<EventType, string[]> = {
  WEDDING:   ["기본 정보", "신랑신부", "식순", "계좌 정보", "추가 정보"],
  GATHERING: ["기본 정보", "주최자",   "일정",   "계좌 정보", "추가 정보"],
};

const EventEditPage: FC = () => {
  const navigate = useNavigate();
  const { slug } = useParams<{ slug: string }>();
  const [formState, setFormState] = useState<EventFormState | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { data: eventDetail, isLoading, error } = useEventDetail(slug);
  const headerVisible = useScrollVisibility();

  useEffect(() => {
    if (!eventDetail) return;
    const { event, heroImages, transportation, announcements, detail, schedules, accountGroups } = eventDetail;

    const weddingDetail = isWeddingDetail(detail) ? detail as WeddingDetailResponse : null;
    const gatheringDetail = (!isWeddingDetail(detail) && detail) ? detail as GatheringDetailResponse : null;
    const rawHosts = weddingDetail ? weddingDetail.hosts : (gatheringDetail ? gatheringDetail.hosts : []);

    const coupleRequests: HostRequest[] = rawHosts.map((c) => ({
      role: c.role,
      name: c.name,
      email: c.email,
      contact: c.contact ?? undefined,
      profileImageUrl: c.profileImageUrl ?? undefined,
      introduction: c.introduction ?? undefined,
      weddingHostData: weddingDetail && "fatherName" in c
        ? {
            fatherName: (c as WeddingDetailResponse["hosts"][0]).fatherName ?? undefined,
            motherName: (c as WeddingDetailResponse["hosts"][0]).motherName ?? undefined,
            isFatherAlive: (c as WeddingDetailResponse["hosts"][0]).isFatherAlive ?? undefined,
            isMotherAlive: (c as WeddingDetailResponse["hosts"][0]).isMotherAlive ?? undefined,
          }
        : undefined,
    }));

    const scheduleRequests: ScheduleRequest[] = [...schedules]
      .sort((a, b) => a.orderIndex - b.orderIndex)
      .map((s) => ({
        title: s.title,
        description: s.description ?? undefined,
        orderIndex: s.orderIndex,
      }));

    const accountGroupData: AccountGroupFormData[] = [...accountGroups]
      .sort((a, b) => a.group.orderIndex - b.group.orderIndex)
      .map((ag) => ({
        groupName: ag.group.groupName,
        orderIndex: ag.group.orderIndex,
        accounts: [...ag.accounts]
          .sort((a, b) => a.orderIndex - b.orderIndex)
          .map((a) => ({
            bankName: a.bankName,
            bankCode: a.bankCode ?? "",
            accountNumber: a.accountNumber,
            accountHolder: a.accountHolder,
            kakaoPayUrl: a.kakaoPayUrl ?? undefined,
            orderIndex: a.orderIndex,
          })),
      }));

    const transportRequests: TransportationRequest[] = [...transportation]
      .sort((a, b) => a.orderIndex - b.orderIndex)
      .map((t) => ({
        type: t.type,
        title: t.title,
        description: t.description ?? undefined,
        orderIndex: t.orderIndex,
      }));

    const announcementRequests: AnnouncementRequest[] = announcements.map((a) => ({
      title: a.title,
      content: a.content,
      isPinned: a.isPinned,
    }));

    const existingPhotos: LandingPhoto[] = [...heroImages]
      .sort((a, b) => a.orderIndex - b.orderIndex)
      .map((g) => ({ preview: g.imageUrl, url: g.imageUrl }));

    const basic: EventRequest = {
      title: event.title,
      slug: event.slug,
      date: event.date,
      locationName: event.locationName,
      locationAddress: event.locationAddress,
      locationDetail: event.locationDetail ?? undefined,
    };

    setFormState({
      eventType: event.type,
      basic,
      couples: coupleRequests,
      landingPhotos: existingPhotos,
      schedules: scheduleRequests,
      accountGroups: accountGroupData,
      transportation: transportRequests,
      announcements: announcementRequests,
      notice: weddingDetail?.notice ?? "",
      parkingInfo: weddingDetail?.parkingInfo ?? "",
      mealInfo: weddingDetail?.mealInfo ?? "",
    });
  }, [eventDetail]);

  // 훅은 formState가 null일 때도 호출되어야 하므로 임시 빈 상태로 초기화
  const safeFormState: EventFormState = formState ?? {
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
  const steps = STEP_LABELS[safeFormState.eventType];

  const {
    step,
    setStep,
    handlePrev,
    handleBasicSubmit,
    handleCoupleSubmit,
    handleScheduleSubmit,
    handleAccountSubmit,
    buildExtraState,
  } = useEventSteps(safeFormState, setFormState as React.Dispatch<React.SetStateAction<EventFormState>>, {
    totalSteps: steps.length,
  });

  if (isLoading || (!formState && !error)) {
    return (
      <div className="min-h-screen bg-bgPrimary flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (error || !formState || !eventDetail) {
    return (
      <div className="min-h-screen bg-bgPrimary flex items-center justify-center">
        <p className="text-gray-500">초대장 정보를 불러올 수 없습니다</p>
      </div>
    );
  }

  const handleExtraSubmit = async (extra: ExtraInfoFormData) => {
    const state = buildExtraState(extra);
    setFormState(state);
    await submitAll(state);
  };

  const submitAll = async (state: EventFormState) => {
    if (!state.basic) return;
    setIsSubmitting(true);
    const eventId = eventDetail.event.id;

    try {
      // 히어로 이미지 업로드 처리
      const heroImages = [];
      for (let i = 0; i < state.landingPhotos.length; i++) {
        const photo = state.landingPhotos[i];
        let imageUrl = photo.url;
        if (photo.file) imageUrl = await eventApi.uploadFile(photo.file);
        if (imageUrl) heroImages.push({ imageUrl, orderIndex: i });
      }

      const isWedding = state.eventType === "WEDDING";

      await eventApi.updateEventWithDetails(eventId, {
        event: {
          ...state.basic,
          type: state.eventType,
          notice: state.notice || undefined,
          parkingInfo: state.parkingInfo || undefined,
          mealInfo: state.mealInfo || undefined,
        },
        heroImages,
        schedules: state.schedules,
        transportation: state.transportation,
        announcements: state.announcements,
        hosts: state.couples.map((c) =>
          isWedding ? c : { ...c, weddingHostData: undefined },
        ),
        accountGroups: state.accountGroups.map((g) => ({
          groupName: g.groupName,
          orderIndex: g.orderIndex,
          accounts: g.accounts.map(({ type: _type, ...rest }) => rest),
        })),
      });

      toast.success("초대장이 수정되었습니다!");
      setTimeout(() => navigate(`/event/${slug}`), 1500);
    } catch {
      toast.error("초대장 수정에 실패했습니다. 다시 시도해주세요.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const stepComponents = [
    <BasicInfoStep
      initialData={formState.basic}
      templateType={formState.eventType}
      onSubmit={handleBasicSubmit}
    />,
    <CoupleStep
      initialData={formState.couples}
      initialPhotos={formState.landingPhotos}
      templateType={formState.eventType}
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
      onSubmit={handleExtraSubmit}
      onBack={handlePrev}
      isSubmitting={isSubmitting}
      submitLabel="초대장 수정"
    />,
  ];

  return (
    <div className="min-h-screen bg-bgPrimary">
      <header
        className={`sticky top-0 z-20 bg-white/80 backdrop-blur-md border-b border-gray-50 transition-transform duration-300 ${
          headerVisible ? "translate-y-0" : "-translate-y-full"
        }`}
      >
        <div className="max-w-lg mx-auto flex items-center gap-3 px-4 py-3">
          <button
            onClick={() => {
              if (window.confirm("수정을 취소하시겠습니까? 변경사항이 저장되지 않습니다.")) {
                navigate(-1);
              }
            }}
            className="text-gray-600"
          >
            <IoArrowBack size={22} />
          </button>
          <h1 className="text-base font-semibold text-gray-800">초대장 수정하기</h1>
        </div>
      </header>

      <div className="max-w-lg mx-auto px-4 py-6">
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

export default EventEditPage;
