import { type FC } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { IoArrowBack } from "react-icons/io5";
import ChatRoomList from "../components/ChatRoomList";
import WeddingBottomNav from "../../wedding/components/WeddingBottomNav";
import { useScrollVisibility } from "../../../global/hooks/useScrollVisibility";

const ChatPage: FC = () => {
  const { weddingId } = useParams<{ weddingId: string }>();
  const navigate = useNavigate();
  const headerVisible = useScrollVisibility();

  if (!weddingId) return null;

  const numericWeddingId = Number(weddingId);

  return (
    <div className="min-h-screen bg-[#faf9f6]">
      <div className="max-w-lg mx-auto">
        <header className={`sticky top-0 z-20 bg-white/80 backdrop-blur-md border-b border-gray-50 transition-transform duration-300 ${headerVisible ? "translate-y-0" : "-translate-y-full"}`}>
          <div className="flex items-center justify-between px-4 py-3">
            <button onClick={() => navigate(-1)} className="p-1 text-gray-600">
              <IoArrowBack size={22} />
            </button>
            <h1 className="text-base font-semibold text-gray-800">채팅</h1>
            <div className="w-7" />
          </div>
        </header>
        <div className="px-4 py-4">
          <ChatRoomList weddingId={numericWeddingId} />
        </div>
        <div className="h-20" />
      </div>

      <WeddingBottomNav
        weddingId={numericWeddingId}
        activeTab="chat"
      />
    </div>
  );
};

export default ChatPage;
