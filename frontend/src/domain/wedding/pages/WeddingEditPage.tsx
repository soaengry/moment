import { type FC, useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { toast, ToastContainer } from "react-toastify";
import { weddingApi } from "../api/weddingApi";
import type {
  WeddingInfoResponse,
  WeddingRequest,
  CoupleRequest,
  ScheduleRequest,
  TransportationRequest,
  AnnouncementRequest,
} from "../types";
import BasicInfoStep from "../components/create/BasicInfoStep";
import CoupleStep from "../components/create/CoupleStep";
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
  schedules: ScheduleRequest[];
  accountGroups: AccountGroupFormData[];
  transportation: TransportationRequest[];
  announcements: AnnouncementRequest[];
  dressCode: string;
  notice: string;
  parkingInfo: string;
  mealInfo: string;
}

const WeddingEditPage: FC = () => {
  const navigate = useNavigate();
  const { weddingId } = useParams<{ weddingId: string }>();
  const [step, setStep] = useState(0);
  const [formState, setFormState] = useState<WeddingFormState | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [originalData, setOriginalData] = useState<WeddingInfoResponse | null>(null);

  useEffect(() => {
    const load = async () => {
      if (!weddingId) return;
      try {
        const info = await weddingApi.getWeddingInfo(Number(weddingId));
        setOriginalData(info);

        const { wedding, couples, schedules, accountGroups, transportation, announcements } = info;

        // 기존 데이터 → 폼 상태로 변환
        const basic: WeddingRequest = {
          title: wedding.title,
          weddingDate: wedding.weddingDate,
          venueName: wedding.venueName,
          venueAddress: wedding.venueAddress,
          venueDetail: wedding.venueDetail ?? undefined,
          venuePhone: wedding.venuePhone ?? undefined,
          mapImageUrl: wedding.mapImageUrl ?? undefined,
        };

        const coupleRequests: CoupleRequest[] = couples.map((c) => ({
          role: c.role,
          name: c.name,
          fatherName: c.fatherName ?? undefined,
          motherName: c.motherName ?? undefined,
          isFatherAlive: c.isFatherAlive,
          isMotherAlive: c.isMotherAlive,
          contact: c.contact ?? undefined,
          profileImageUrl: c.profileImageUrl ?? undefined,
          introduction: c.introduction ?? undefined,
        }));

        const scheduleRequests: ScheduleRequest[] = schedules
          .sort((a, b) => a.orderIndex - b.orderIndex)
          .map((s) => ({
            time: s.time,
            title: s.title,
            description: s.description ?? undefined,
            orderIndex: s.orderIndex,
          }));

        const accountGroupData: AccountGroupFormData[] = accountGroups
          .sort((a, b) => a.group.orderIndex - b.group.orderIndex)
          .map((ag) => ({
            side: ag.group.side,
            groupName: ag.group.groupName,
            orderIndex: ag.group.orderIndex,
            accounts: ag.accounts
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

        const transportRequests: TransportationRequest[] = transportation
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

        setFormState({
          basic,
          couples: coupleRequests,
          schedules: scheduleRequests,
          accountGroups: accountGroupData,
          transportation: transportRequests,
          announcements: announcementRequests,
          dressCode: wedding.dressCode ?? "",
          notice: wedding.notice ?? "",
          parkingInfo: wedding.parkingInfo ?? "",
          mealInfo: wedding.mealInfo ?? "",
        });
      } catch {
        toast.error("초대장 정보를 불러올 수 없습니다");
      } finally {
        setIsLoading(false);
      }
    };
    load();
  }, [weddingId]);

  if (isLoading || !formState) {
    return (
      <div className="min-h-screen bg-bgPrimary flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  const id = Number(weddingId);

  const handleNext = () => setStep((s) => Math.min(s + 1, STEPS.length - 1));
  const handlePrev = () => setStep((s) => Math.max(s - 1, 0));

  const handleBasicSubmit = (data: WeddingRequest) => {
    setFormState((prev) => prev && { ...prev, basic: data });
    handleNext();
  };

  const handleCoupleSubmit = (couples: CoupleRequest[]) => {
    setFormState((prev) => prev && { ...prev, couples });
    handleNext();
  };

  const handleScheduleSubmit = (schedules: ScheduleRequest[]) => {
    setFormState((prev) => prev && { ...prev, schedules });
    handleNext();
  };

  const handleAccountSubmit = (accountGroups: AccountGroupFormData[]) => {
    setFormState((prev) => prev && { ...prev, accountGroups });
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
    if (!state.basic || !originalData) return;
    setIsSubmitting(true);

    try {
      // 1. Wedding 기본정보 업데이트
      const weddingRequest: WeddingRequest = {
        ...state.basic,
        dressCode: state.dressCode || undefined,
        notice: state.notice || undefined,
        parkingInfo: state.parkingInfo || undefined,
        mealInfo: state.mealInfo || undefined,
      };
      await weddingApi.updateWedding(id, weddingRequest);

      // 2. 기존 sub-resource 삭제 후 재생성
      // Couples: 기존 삭제 → 새로 생성
      for (const c of originalData.couples) {
        await weddingApi.deleteCouple(c.id);
      }
      for (const couple of state.couples) {
        await weddingApi.createCouple(id, couple);
      }

      // Schedules
      for (const s of originalData.schedules) {
        await weddingApi.deleteSchedule(s.id);
      }
      for (const schedule of state.schedules) {
        await weddingApi.createSchedule(id, schedule);
      }

      // Account Groups (삭제하면 하위 accounts도 cascade)
      for (const ag of originalData.accountGroups) {
        await weddingApi.deleteAccountGroup(ag.group.id);
      }
      for (const groupData of state.accountGroups) {
        const group = await weddingApi.createAccountGroup(id, {
          side: groupData.side,
          groupName: groupData.groupName,
          orderIndex: groupData.orderIndex,
        });
        for (const account of groupData.accounts) {
          await weddingApi.createAccount(group.id, account);
        }
      }

      // Transportation
      for (const t of originalData.transportation) {
        await weddingApi.deleteTransportation(t.id);
      }
      for (const transport of state.transportation) {
        await weddingApi.createTransportation(id, transport);
      }

      // Announcements
      for (const a of originalData.announcements) {
        await weddingApi.deleteAnnouncement(a.id);
      }
      for (const announcement of state.announcements) {
        await weddingApi.createAnnouncement(id, announcement);
      }

      toast.success("초대장이 수정되었습니다!");
      setTimeout(() => navigate(`/wedding/${id}`), 1500);
    } catch {
      toast.error("초대장 수정에 실패했습니다. 다시 시도해주세요.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-bgPrimary">
      <div className="max-w-lg mx-auto px-4 py-6">
        <h1 className="text-xl font-bold text-gray-800 mb-6 text-center">
          초대장 수정하기
        </h1>

        {/* 스텝 인디케이터 */}
        <div className="flex items-center justify-center gap-1 mb-8">
          {STEPS.map((label, i) => (
            <div key={label} className="flex items-center gap-1">
              <button
                onClick={() => { if (i < step) setStep(i); }}
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
                <div className={`w-6 h-0.5 ${i < step ? "bg-primary/30" : "bg-gray-200"}`} />
              )}
            </div>
          ))}
        </div>
        <p className="text-center text-sm text-gray-500 mb-6">{STEPS[step]}</p>

        {/* 스텝 폼 */}
        {step === 0 && (
          <BasicInfoStep initialData={formState.basic} onSubmit={handleBasicSubmit} />
        )}
        {step === 1 && (
          <CoupleStep initialData={formState.couples} onSubmit={handleCoupleSubmit} onBack={handlePrev} />
        )}
        {step === 2 && (
          <ScheduleStep initialData={formState.schedules} onSubmit={handleScheduleSubmit} onBack={handlePrev} />
        )}
        {step === 3 && (
          <AccountStep initialData={formState.accountGroups} onSubmit={handleAccountSubmit} onBack={handlePrev} />
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

export default WeddingEditPage;
