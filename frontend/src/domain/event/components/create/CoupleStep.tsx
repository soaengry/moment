import { type FC, useState } from "react";
import type { HostRequest, HostRole, EventType } from "../../types";
import PersonForm from "./PersonForm";
import LandingPhotoGrid from "./LandingPhotoGrid";

export interface LandingPhoto {
  file?: File;
  preview: string;
  url?: string; // existing S3 URL (for edit mode)
}

interface CurrentUser {
  nickname: string;
  email: string;
  profileImageUrl: string | null;
}

interface Props {
  initialData: HostRequest[];
  initialPhotos?: LandingPhoto[];
  templateType: EventType;
  currentUser?: CurrentUser;
  onSubmit: (couples: HostRequest[], photos: LandingPhoto[]) => void;
  onBack: () => void;
}

const isWedding = (t: EventType) => t === "WEDDING";

const emptyCoupleForm = (role: HostRole): HostRequest => ({
  role,
  name: "",
  email: "",
  fatherName: "",
  motherName: "",
  isFatherAlive: true,
  isMotherAlive: true,
  contact: "",
  introduction: "",
});

const defaultRole = (templateType: EventType): HostRole =>
  templateType === "GATHERING" ? "HOST" : "GROOM";

const CoupleStep: FC<Props> = ({
  initialData,
  initialPhotos,
  templateType,
  currentUser,
  onSubmit,
  onBack,
}) => {
  const wedding = isWedding(templateType);
  const role = defaultRole(templateType);

  const buildInitialGroom = (): HostRequest => {
    const existing =
      initialData.find((c) => c.role === role) ??
      initialData.find((c) => c.role === "GROOM");
    if (existing) return existing;
    if (!wedding && currentUser) {
      return {
        ...emptyCoupleForm(role),
        name: currentUser.nickname,
        email: currentUser.email,
        profileImageUrl: currentUser.profileImageUrl ?? undefined,
      };
    }
    return emptyCoupleForm(role);
  };

  const [groom, setGroom] = useState<HostRequest>(buildInitialGroom);
  const [bride, setBride] = useState<HostRequest>(
    initialData.find((c) => c.role === "BRIDE") ?? emptyCoupleForm("BRIDE"),
  );
  const [photos, setPhotos] = useState<LandingPhoto[]>(initialPhotos ?? []);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const handleSubmit = () => {
    const newErrors: Record<string, string> = {};
    if (!groom.name.trim()) newErrors.groomName = "이름을 입력해주세요";
    if (!groom.email.trim()) newErrors.groomEmail = "이메일을 입력해주세요";
    if (wedding) {
      if (!bride.name.trim()) newErrors.brideName = "신부 이름을 입력해주세요";
      if (!bride.email.trim()) newErrors.brideEmail = "신부 이메일을 입력해주세요";
    }
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }
    onSubmit(wedding ? [groom, bride] : [groom], photos);
  };

  return (
    <div className="space-y-4">
      <PersonForm
        label={wedding ? "신랑 정보" : "주최자 정보"}
        role={role}
        data={groom}
        setData={setGroom}
        nameKey="groomName"
        errors={errors}
        showParents={wedding}
      />

      {wedding && (
        <PersonForm
          label="신부 정보"
          role="BRIDE"
          data={bride}
          setData={setBride}
          nameKey="brideName"
          errors={errors}
          showParents
        />
      )}

      <LandingPhotoGrid photos={photos} setPhotos={setPhotos} />

      <div className="flex gap-3">
        <button
          type="button"
          onClick={onBack}
          className="flex-1 py-3 rounded-xl border border-gray-200 text-gray-600 font-semibold hover:bg-gray-50 transition-colors"
        >
          이전
        </button>
        <button
          type="button"
          onClick={handleSubmit}
          className="flex-1 py-3 rounded-xl bg-primary text-white font-semibold hover:bg-primaryHover transition-colors"
        >
          다음
        </button>
      </div>
    </div>
  );
};

export default CoupleStep;
