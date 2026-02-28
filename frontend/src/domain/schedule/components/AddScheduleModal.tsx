import { type FC } from "react";
import { useForm } from "react-hook-form";
import { IoClose } from "react-icons/io5";

interface AddScheduleForm {
  invitationId: string;
}

interface AddScheduleModalProps {
  onClose: () => void;
  onSubmit: (invitationId: string) => Promise<void>;
  isLoading: boolean;
}

const AddScheduleModal: FC<AddScheduleModalProps> = ({
  onClose,
  onSubmit,
  isLoading,
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<AddScheduleForm>();

  const handleFormSubmit = async (data: AddScheduleForm) => {
    await onSubmit(data.invitationId.trim());
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-sm mx-4">
        <div className="flex items-center justify-between p-4 border-b border-gray-100">
          <h3 className="text-lg font-semibold text-gray-800">일정 등록</h3>
          <button
            onClick={onClose}
            className="p-1 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <IoClose className="w-5 h-5 text-gray-500" />
          </button>
        </div>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="p-4 space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              초대장 ID
            </label>
            <input
              {...register("invitationId", {
                required: "초대장 ID를 입력해주세요",
              })}
              placeholder="초대장 링크의 ID를 입력하세요"
              className="w-full px-3 py-2.5 rounded-lg border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
              autoFocus
            />
            {errors.invitationId && (
              <p className="mt-1 text-xs text-red-500">
                {errors.invitationId.message}
              </p>
            )}
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full py-2.5 rounded-lg text-sm font-medium text-white bg-primary hover:bg-primaryHover disabled:opacity-50 transition-colors"
          >
            {isLoading ? "등록 중..." : "등록하기"}
          </button>
        </form>
      </div>
    </div>
  );
};

export default AddScheduleModal;
