import { type FC } from "react";
import { useNavigate } from "react-router-dom";
import { IoArrowBack } from "react-icons/io5";
import { ToastContainer } from "react-toastify";
import { useScrollVisibility } from "../../../global/hooks/useScrollVisibility";
import Calendar from "../components/Calendar";

const MySchedulePage: FC = () => {
  const navigate = useNavigate();
  const headerVisible = useScrollVisibility();

  return (
    <div className="min-h-screen bg-[#faf9f6]">
      <div className="max-w-lg mx-auto">
        <header className={`sticky top-0 z-20 bg-white/80 backdrop-blur-md border-b border-gray-50 transition-transform duration-300 ${headerVisible ? "translate-y-0" : "-translate-y-full"}`}>
          <div className="flex items-center gap-3 px-4 py-3">
            <button onClick={() => navigate(-1)} className="text-gray-600">
              <IoArrowBack size={22} />
            </button>
            <h1 className="text-base font-semibold text-gray-800">내 일정</h1>
          </div>
        </header>

        <div className="px-4 py-4">
          <div className="bg-white rounded-2xl shadow-sm p-4 border border-gray-100">
            <Calendar />
          </div>
        </div>

        <div className="h-20" />
      </div>

      <ToastContainer
        position="bottom-center"
        autoClose={3000}
        hideProgressBar
        toastClassName="text-sm"
      />
    </div>
  );
};

export default MySchedulePage;
