import { type FC, useState, useEffect, useRef, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { IoArrowBack, IoSend } from "react-icons/io5";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { chatApi } from "../api/chatApi";
import type { ChatMessage } from "../types";
import { useAuthStore } from "../../auth/store/useAuthStore";
import { tokenStorage } from "../../auth/auth.utils";
import { ENV } from "../../../global/config/env";
import { weddingApi } from "../../wedding/api/weddingApi";

const ChatRoomPage: FC = () => {
  const { invitationId, roomId } = useParams<{ invitationId: string; roomId: string }>();
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);

  const [weddingId, setWeddingId] = useState<number | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState("");
  const [isConnected, setIsConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [roomName, setRoomName] = useState("");

  useEffect(() => {
    if (!invitationId) return;
    weddingApi.getWeddingInfo(invitationId).then((info) => {
      setWeddingId(Number(info.wedding.id));
    }).catch(() => { /* silent */ });
  }, [invitationId]);

  const clientRef = useRef<Client | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  const fetchMessages = useCallback(async () => {
    if (weddingId === null || !roomId) return;
    setIsLoading(true);
    try {
      const res = await chatApi.getMessages(weddingId, Number(roomId));
      setMessages(res.content.reverse());
    } catch { /* silent */ }
    finally { setIsLoading(false); }
  }, [weddingId, roomId]);

  const fetchRoomInfo = useCallback(async () => {
    if (weddingId === null) return;
    try {
      const rooms = await chatApi.getRooms(weddingId);
      const room = rooms.find((r) => r.id === Number(roomId));
      if (room) setRoomName(room.name);
    } catch { /* silent */ }
  }, [weddingId, roomId]);

  useEffect(() => {
    fetchMessages();
    fetchRoomInfo();
  }, [fetchMessages, fetchRoomInfo]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // WebSocket connection
  useEffect(() => {
    if (!roomId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(`${ENV.API_BASE_URL}/ws`),
      connectHeaders: {
        Authorization: `Bearer ${tokenStorage.getAccessToken() ?? ""}`,
      },
      onConnect: () => {
        setIsConnected(true);
        client.subscribe(`/topic/chat/${roomId}`, (message) => {
          const msg: ChatMessage = JSON.parse(message.body);
          setMessages((prev) => [...prev, msg]);
        });
      },
      onDisconnect: () => {
        setIsConnected(false);
      },
      onStompError: () => {
        setIsConnected(false);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [roomId]);

  const sendMessage = () => {
    if (!input.trim() || !clientRef.current?.connected || !roomId) return;

    clientRef.current.publish({
      destination: "/app/chat.sendMessage",
      body: JSON.stringify({
        roomId: Number(roomId),
        content: input.trim(),
        type: "CHAT",
      }),
    });

    setInput("");
  };

  const formatTime = (dateStr: string) => {
    const d = new Date(dateStr);
    const hours = d.getHours();
    const mins = String(d.getMinutes()).padStart(2, "0");
    const ampm = hours < 12 ? "오전" : "오후";
    const h = hours % 12 || 12;
    return `${ampm} ${h}:${mins}`;
  };

  return (
    <div className="h-screen flex flex-col bg-[#b2c7d9]">
      {/* Header */}
      <header className="bg-white/90 backdrop-blur-md px-4 py-3 flex items-center gap-3 shadow-sm">
        <button onClick={() => navigate(-1)} className="text-gray-600">
          <IoArrowBack size={22} />
        </button>
        <div>
          <h1 className="text-sm font-semibold text-gray-800">{roomName || "채팅방"}</h1>
          <span className={`text-[10px] ${isConnected ? "text-green-500" : "text-gray-400"}`}>
            {isConnected ? "연결됨" : "연결 중..."}
          </span>
        </div>
      </header>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto px-4 py-3 space-y-3">
        {isLoading ? (
          <div className="flex justify-center py-8">
            <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
          </div>
        ) : (
          messages.map((msg) => {
            if (msg.type === "JOIN" || msg.type === "LEAVE") {
              return (
                <div key={msg.id} className="text-center">
                  <span className="text-[10px] text-white/70 bg-black/10 px-3 py-1 rounded-full">
                    {msg.nickname}님이 {msg.type === "JOIN" ? "입장" : "퇴장"}했습니다
                  </span>
                </div>
              );
            }

            const isMe = user?.id === msg.userId;

            return (
              <div key={msg.id} className={`flex ${isMe ? "justify-end" : "justify-start"} gap-2`}>
                {!isMe && (
                  <div className="w-8 h-8 rounded-full bg-white/80 flex-shrink-0 overflow-hidden flex items-center justify-center">
                    {msg.profileImageUrl ? (
                      <img src={msg.profileImageUrl} alt="" className="w-full h-full object-cover" />
                    ) : (
                      <span className="text-xs text-gray-500">{msg.nickname.charAt(0)}</span>
                    )}
                  </div>
                )}
                <div className={`max-w-[70%] ${isMe ? "items-end" : "items-start"}`}>
                  {!isMe && (
                    <p className="text-[10px] text-white/80 mb-0.5 ml-1">{msg.nickname}</p>
                  )}
                  <div className={`flex items-end gap-1.5 ${isMe ? "flex-row-reverse" : ""}`}>
                    <div
                      className={`px-3.5 py-2 rounded-2xl text-sm leading-relaxed ${
                        isMe
                          ? "bg-[#fee500] text-gray-800 rounded-br-md"
                          : "bg-white text-gray-700 rounded-bl-md"
                      }`}
                    >
                      {msg.content}
                    </div>
                    <span className="text-[9px] text-white/60 flex-shrink-0">{formatTime(msg.createdAt)}</span>
                  </div>
                </div>
              </div>
            );
          })
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input */}
      <div className="bg-white px-4 py-3 flex items-center gap-2">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && sendMessage()}
          placeholder="메시지 입력..."
          maxLength={500}
          className="flex-1 text-sm outline-none placeholder:text-gray-300"
        />
        <button
          onClick={sendMessage}
          disabled={!input.trim() || !isConnected}
          className="text-primary disabled:opacity-30"
        >
          <IoSend size={20} />
        </button>
      </div>
    </div>
  );
};

export default ChatRoomPage;
