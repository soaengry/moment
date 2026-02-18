import { type FC, useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast, ToastContainer } from "react-toastify";
import { weddingApi } from "../api/weddingApi";
import type {
  WeddingRequest,
  CoupleRequest,
  ScheduleRequest,
  AccountGroupRequest,
  AccountRequest,
  TransportationRequest,
  AnnouncementRequest,
} from "../types";
import BasicInfoStep from "../components/create/BasicInfoStep";
import CoupleStep, { type LandingPhoto } from "../components/create/CoupleStep";
import ScheduleStep from "../components/create/ScheduleStep";
import AccountStep, {
  type AccountGroupFormData,
} from "../components/create/AccountStep";
import ExtraInfoStep, {
  type ExtraInfoFormData,
} from "../components/create/ExtraInfoStep";

const STEPS = ["기본 정보", "신랑신부", "식순", "계좌 정보", "추가 정보"];

export interface WeddingFormState {
  basic: WeddingRequest | null;
  couples: CoupleRequest[];
  landingPhotos: LandingPhoto[];
  schedules: ScheduleRequest[];
  accountGroups: AccountGroupFormData[];
  transportation: TransportationRequest[];
  announcements: AnnouncementRequest[];
  dressCode: string;
  notice: string;
  parkingInfo: string;
  mealInfo: string;
}

const initialState: WeddingFormState = {
  basic: null,
  couples: [],
  landingPhotos: [],
  schedules: [],
  accountGroups: [],
  transportation: [],
  announcements: [],
  dressCode: "",
  notice: "",
  parkingInfo: "",
  mealInfo: "",
};

const WeddingCreatePage: FC = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const [formState, setFormState] = useState<WeddingFormState>(initialState);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleNext = () => setStep((s) => Math.min(s + 1, STEPS.length - 1));
  const handlePrev = () => setStep((s) => Math.max(s - 1, 0));

  const handleBasicSubmit = (data: WeddingRequest) => {
    setFormState((prev) => ({ ...prev, basic: data }));
    handleNext();
  };

  const handleCoupleSubmit = (couples: CoupleRequest[], photos: LandingPhoto[]) => {
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

  const handleExtraSubmit = async (extra: ExtraInfoFormData) => {
    const updatedState: WeddingFormState = {
      ...formState,
      transportation: extra.transportation,
      announcements: extra.announcements,
      dressCode: extra.dressCode,
      notice: extra.notice,
      parkingInfo: extra.parkingInfo,
      mealInfo: extra.mealInfo,
    };
    setFormState(updatedState);
    await submitAll(updatedState);
  };

  const submitAll = async (state: WeddingFormState) => {
    if (!state.basic) return;
    setIsSubmitting(true);

    try {
      // 1. 웨딩 생성 (드레스코드/유의사항 포함)
      const weddingRequest: WeddingRequest = {
        ...state.basic,
        dressCode: state.dressCode || undefined,
        notice: state.notice || undefined,
        parkingInfo: state.parkingInfo || undefined,
        mealInfo: state.mealInfo || undefined,
      };
      const wedding = await weddingApi.createWedding(weddingRequest);
      const weddingId = wedding.id;

      // 2. 신랑/신부
      for (const couple of state.couples) {
        await weddingApi.createCouple(weddingId, couple);
      }

      // 3. 식순
      for (const schedule of state.schedules) {
        await weddingApi.createSchedule(weddingId, schedule);
      }

      // 4. 계좌 그룹 + 계좌
      for (const groupData of state.accountGroups) {
        const group = await weddingApi.createAccountGroup(weddingId, {
          side: groupData.side,
          groupName: groupData.groupName,
          orderIndex: groupData.orderIndex,
        });
        for (const account of groupData.accounts) {
          await weddingApi.createAccount(group.id, account);
        }
      }

      // 5. 랜딩 사진 → S3 업로드 → 갤러리 생성
      for (let i = 0; i < state.landingPhotos.length; i++) {
        const photo = state.landingPhotos[i];
        const imageUrl = await weddingApi.uploadFile(photo.file);
        await weddingApi.createGallery(weddingId, {
          imageUrl,
          orderIndex: i,
        });
      }

      // 6. 교통
      for (const transport of state.transportation) {
        await weddingApi.createTransportation(weddingId, transport);
      }

      // 7. 공지사항
      for (const announcement of state.announcements) {
        await weddingApi.createAnnouncement(weddingId, announcement);
      }

      toast.success("초대장이 생성되었습니다!");
      setTimeout(() => navigate(`/wedding/${weddingId}`), 1500);
    } catch {
      toast.error("초대장 생성에 실패했습니다. 다시 시도해주세요.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-bgPrimary">
      <div className="max-w-lg mx-auto px-4 py-6">
        {/* 헤더 */}
        <h1 className="text-xl font-bold text-gray-800 mb-6 text-center">
          초대장 만들기
        </h1>

        {/* 스텝 인디케이터 */}
        <div className="flex items-center justify-center gap-1 mb-8">
          {STEPS.map((label, i) => (
            <div key={label} className="flex items-center gap-1">
              <button
                onClick={() => {
                  if (i < step) setStep(i);
                }}
                disabled={i > step}
                className={`w-8 h-8 rounded-full text-xs font-semibold flex items-center justify-center transition-colors ${
                  i === step
                    ? "bg-primary text-white"
                    : i < step
                      ? "bg-primary/20 text-primary cursor-pointer"
                      : "bg-gray-200 text-gray-400"
                }`}
              >
                {i + 1}
              </button>
              {i < STEPS.length - 1 && (
                <div
                  className={`w-6 h-0.5 ${i < step ? "bg-primary/30" : "bg-gray-200"}`}
                />
              )}
            </div>
          ))}
        </div>
        <p className="text-center text-sm text-gray-500 mb-6">
          {STEPS[step]}
        </p>

        {/* 스텝 폼 */}
        {step === 0 && (
          <BasicInfoStep
            initialData={formState.basic}
            onSubmit={handleBasicSubmit}
          />
        )}
        {step === 1 && (
          <CoupleStep
            initialData={formState.couples}
            initialPhotos={formState.landingPhotos}
            onSubmit={handleCoupleSubmit}
            onBack={handlePrev}
          />
        )}
        {step === 2 && (
          <ScheduleStep
            initialData={formState.schedules}
            onSubmit={handleScheduleSubmit}
            onBack={handlePrev}
          />
        )}
        {step === 3 && (
          <AccountStep
            initialData={formState.accountGroups}
            onSubmit={handleAccountSubmit}
            onBack={handlePrev}
          />
        )}
        {step === 4 && (
          <ExtraInfoStep
            initialData={{
              transportation: formState.transportation,
              announcements: formState.announcements,
              dressCode: formState.dressCode,
              notice: formState.notice,
              parkingInfo: formState.parkingInfo,
              mealInfo: formState.mealInfo,
            }}
            onSubmit={handleExtraSubmit}
            onBack={handlePrev}
            isSubmitting={isSubmitting}
          />
        )}
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

export default WeddingCreatePage;
