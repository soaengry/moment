import { type FC, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { IoChatbubbles } from "react-icons/io5";
import { chatApi } from "../api/chatApi";
import type { ChatRoom } from "../types";

interface Props {
  weddingId: number;
  invitationId: string;
}

const ChatRoomList: FC<Props> = ({ weddingId, invitationId }) => {
  const navigate = useNavigate();
  const [rooms, setRooms] = useState<ChatRoom[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchRooms = async () => {
      try {
        const data = await chatApi.getRooms(weddingId);
        setRooms(data);
      } catch { /* silent */ }
      finally { setIsLoading(false); }
    };
    fetchRooms();
  }, [weddingId]);

  if (isLoading) {
    return (
      <div className="flex justify-center py-8">
        <div className="w-5 h-5 border-2 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (rooms.length === 0) {
    return (
      <div className="text-center py-12">
        <IoChatbubbles size={40} className="text-gray-200 mx-auto mb-3" />
        <p className="text-sm text-gray-300">아직 채팅방이 없습니다</p>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      {rooms.map((room) => (
        <button
          key={room.id}
          onClick={() => navigate(`/wedding/${invitationId}/chat/${room.id}`)}
          className="w-full flex items-center gap-3 p-4 bg-white rounded-xl shadow-sm hover:bg-gray-50 transition-colors"
        >
          <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0">
            <IoChatbubbles size={18} className="text-primary" />
          </div>
          <div className="flex-1 text-left">
            <p className="text-sm font-medium text-gray-700">{room.name}</p>
            <p className="text-[10px] text-gray-400">
              {new Date(room.createdAt).toLocaleDateString("ko-KR")}
            </p>
          </div>
        </button>
      ))}
    </div>
  );
};

export default ChatRoomList;
