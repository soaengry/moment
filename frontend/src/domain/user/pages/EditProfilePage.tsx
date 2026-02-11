import { type FC, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../../auth/store/useAuthStore";
import { authApi } from "../../auth/api/authApi";
import { AUTH_VALIDATION } from "../../auth/auth.constants";
import { isAxiosError } from "axios";

const EditProfilePage: FC = () => {
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const setUser = useAuthStore((state) => state.setUser);

  const [nickname, setNickname] = useState("");
  const [nicknameDupError, setNicknameDupError] = useState<string | null>(null);
  const [serverError, setServerError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (user) {
      setNickname(user.nickname);
    }
  }, [user]);

  const handleNicknameBlur = async () => {
    if (!nickname || nickname.length < AUTH_VALIDATION.NICKNAME_MIN_LENGTH)
      return;
    if (nickname === user?.nickname) {
      setNicknameDupError(null);
      return;
    }

    try {
      const { exists } = await authApi.checkNickname(nickname);
      setNicknameDupError(exists ? "이미 사용 중인 닉네임입니다." : null);
    } catch {
      // 무시
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (nicknameDupError) return;

    setServerError(null);
    setIsSubmitting(true);

    try {
      const updated = await authApi.updateProfile({ nickname });
      setUser(updated);
      navigate("/my-page");
    } catch (error: unknown) {
      if (isAxiosError(error) && error.response?.status === 409) {
        setServerError("이미 사용 중인 닉네임입니다.");
      } else {
        setServerError("프로필 수정 중 오류가 발생했습니다.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!user) return null;

  return (
    <div className="max-w-lg mx-auto">
      <h2 className="text-2xl font-bold mb-6" style={{ color: "#88AF64" }}>
        프로필 수정
      </h2>

      <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100">
        {serverError && (
          <div
            className="mb-4 p-3 rounded-lg text-sm"
            style={{ backgroundColor: "#FEF2F2", color: "#DC2626" }}
          >
            {serverError}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label
              htmlFor="nickname"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              닉네임
            </label>
            <input
              id="nickname"
              type="text"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              onBlur={handleNicknameBlur}
              placeholder="닉네임 (2-50자)"
              className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 transition-colors"
            />
            {nicknameDupError && (
              <p className="mt-1 text-sm" style={{ color: "#DC2626" }}>
                {nicknameDupError}
              </p>
            )}
          </div>

          <div className="flex gap-3">
            <button
              type="submit"
              disabled={isSubmitting || !!nicknameDupError}
              className="flex-1 py-3 rounded-lg text-white font-medium transition-opacity disabled:opacity-50"
              style={{ backgroundColor: "#88AF64" }}
            >
              {isSubmitting ? "수정 중..." : "수정 완료"}
            </button>
            <button
              type="button"
              onClick={() => navigate(-1)}
              className="flex-1 py-3 rounded-lg border border-gray-200 text-gray-600 font-medium hover:bg-gray-50 transition-colors"
            >
              취소
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditProfilePage;
