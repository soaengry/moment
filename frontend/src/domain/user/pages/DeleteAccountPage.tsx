import { type FC, useState } from "react";
import { useNavigate } from "react-router-dom";
import { IoArrowBack } from "react-icons/io5";
import { authApi } from "../../auth/api/authApi";
import { useAuthStore } from "../../auth/store/useAuthStore";
import { useScrollVisibility } from "../../../global/hooks/useScrollVisibility";

const DeleteAccountPage: FC = () => {
  const navigate = useNavigate();
  const logout = useAuthStore((state) => state.logout);
  const headerVisible = useScrollVisibility();
  const [agreed, setAgreed] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleDelete = async () => {
    if (!agreed) return;
    if (!window.confirm("정말로 탈퇴하시겠습니까?")) return;

    setServerError(null);
    setIsSubmitting(true);

    try {
      await authApi.deleteAccount();
      logout();
      navigate("/login", {
        state: {
          message:
            "회원 탈퇴가 완료되었습니다. 30일 내 계정을 복구할 수 있습니다.",
        },
      });
    } catch {
      setServerError("회원 탈퇴 중 오류가 발생했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#faf9f6]">
      <div className="max-w-lg mx-auto">
        <header className={`sticky top-0 z-20 bg-white/80 backdrop-blur-md border-b border-gray-50 transition-transform duration-300 ${headerVisible ? "translate-y-0" : "-translate-y-full"}`}>
          <div className="flex items-center gap-3 px-4 py-3">
            <button onClick={() => navigate(-1)} className="text-gray-600">
              <IoArrowBack size={22} />
            </button>
            <h1 className="text-base font-semibold text-gray-800">회원 탈퇴</h1>
          </div>
        </header>

      <div className="px-4 py-4">
      <div className="bg-white rounded-2xl shadow-sm p-6 border border-gray-100">
        <div className="mb-6 p-4 rounded-lg bg-red-50 text-sm text-red-700 space-y-2">
          <p className="font-semibold">
            ⚠️ 회원 탈퇴 시 다음 사항을 확인해주세요:
          </p>
          <ul className="list-disc list-inside space-y-1 ml-1">
            <li>모든 개인정보가 삭제됩니다</li>
            <li>작성한 게시글과 댓글은 유지됩니다</li>
            <li>30일 이내 재가입 시 계정을 복구할 수 있습니다</li>
            <li>30일 이후에는 영구적으로 삭제됩니다</li>
          </ul>
        </div>

        {serverError && (
          <div className="mb-4 p-3 rounded-lg text-sm bg-bgError text-error">
            {serverError}
          </div>
        )}

        <label className="flex items-start gap-3 mb-6 cursor-pointer">
          <input
            type="checkbox"
            checked={agreed}
            onChange={(e) => setAgreed(e.target.checked)}
            className="mt-0.5 w-4 h-4"
          />
          <span className="text-sm text-gray-700">
            위 내용을 확인했으며, 회원 탈퇴에 동의합니다
          </span>
        </label>

        <div className="flex gap-3">
          <button
            onClick={handleDelete}
            disabled={!agreed || isSubmitting}
            className="flex-1 py-3 rounded-lg text-white font-medium transition-opacity disabled:opacity-50 bg-red-500"
          >
            {isSubmitting ? "처리 중..." : "탈퇴하기"}
          </button>
          <button
            onClick={() => navigate(-1)}
            className="flex-1 py-3 rounded-lg border border-gray-200 text-gray-600 font-medium hover:bg-gray-50 transition-colors"
          >
            취소
          </button>
        </div>
      </div>
      </div>

        <div className="h-20" />
      </div>
    </div>
  );
};

export default DeleteAccountPage;
