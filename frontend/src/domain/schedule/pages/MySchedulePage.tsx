import { type FC } from "react";
import { ToastContainer } from "react-toastify";
import Calendar from "../components/Calendar";

const MySchedulePage: FC = () => {
  return (
    <div className="max-w-3xl mx-auto">
      <h2 className="text-2xl font-bold mb-6 text-primary">내 일정</h2>
      <div className="bg-white rounded-2xl shadow-lg p-4 border border-green-100">
        <Calendar />
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
