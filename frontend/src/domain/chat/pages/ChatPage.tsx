import { type FC } from "react";
import { useParams } from "react-router-dom";
import ChatRoomList from "../components/ChatRoomList";

const ChatPage: FC = () => {
  const { weddingId } = useParams<{ weddingId: string }>();

  if (!weddingId) return null;

  return (
    <div className="min-h-screen bg-[#faf9f6]">
      <div className="max-w-lg mx-auto">
        <header className="sticky top-0 z-20 bg-white/80 backdrop-blur-md border-b border-gray-50 px-4 py-3">
          <h1 className="text-lg font-bold text-gray-800">채팅</h1>
        </header>
        <div className="px-4 py-4">
          <ChatRoomList weddingId={Number(weddingId)} />
        </div>
        <div className="h-20" />
      </div>
    </div>
  );
};

export default ChatPage;
