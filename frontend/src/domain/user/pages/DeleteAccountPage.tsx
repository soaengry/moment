import { type FC, useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi } from "../../auth/api/authApi";
import { useAuthStore } from "../../auth/store/useAuthStore";

const DeleteAccountPage: FC = () => {
  const navigate = useNavigate();
  const logout = useAuthStore((state) => state.logout);
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
    <div className="max-w-lg mx-auto">
      <h2 className="text-2xl text-danger font-bold mb-6">회원 탈퇴</h2>

      <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100">
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
          <div className="mb-4 p-3 rounded-lg text-sm bg-bgDanger text-danger">
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
            className="flex-1 py-3 rounded-lg text-white font-medium transition-opacity disabled:opacity-50 bg-danger hover:bg-dangerHover"
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
  );
};

export default DeleteAccountPage;
