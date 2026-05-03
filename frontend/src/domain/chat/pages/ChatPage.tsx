import { type FC, useState, useEffect, useRef, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { IoArrowBack, IoSend, IoAdd } from "react-icons/io5";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { chatApi } from "../api/chatApi";
import type { ChatMessage } from "../types";
import { useAuthStore } from "../../auth/store/useAuthStore";
import { tokenStorage } from "../../auth/auth.utils";
import { ENV } from "../../../global/config/env";
import { eventApi } from "../../event/api/eventApi";
import { attendanceApi } from "../../attendance/api/attendanceApi";
import ImageViewer from "../../feed/components/ImageViewer";

const ChatPage: FC = () => {
  const { slug } = useParams<{ slug: string }>();
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);

  const [eventId, setEventId] = useState<number | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState("");
  const [isConnected, setIsConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isUploading, setIsUploading] = useState(false);
  const [viewerImage, setViewerImage] = useState<string | null>(null);
  const [isParticipant, setIsParticipant] = useState(false);

  const clientRef = useRef<Client | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const pollingRef = useRef<ReturnType<typeof setInterval> | null>(null);

  // eventId 조회
  useEffect(() => {
    if (!slug) return;
    eventApi
      .getEventInfo(slug)
      .then((info) => {
        setEventId(info.event.id);
      })
      .catch(() => {
        /* silent */
      });
  }, [slug]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  const fetchMessages = useCallback(async () => {
    if (eventId === null) return;
    try {
      const res = await chatApi.getMessages(eventId);
      setMessages(res.content.reverse());
    } catch {
      /* silent */
    } finally {
      setIsLoading(false);
    }
  }, [eventId]);

  // 초기 메시지 로드
  useEffect(() => {
    if (eventId === null) return;
    setIsLoading(true);
    fetchMessages();
  }, [eventId, fetchMessages]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // 로그인 유저: WebSocket 연결
  useEffect(() => {
    if (eventId === null || !isAuthenticated) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(`${ENV.API_BASE_URL}/ws`),
      connectHeaders: {
        Authorization: `Bearer ${tokenStorage.getAccessToken() ?? ""}`,
      },
      onConnect: () => {
        setIsConnected(true);
        client.subscribe(`/topic/chat/event/${eventId}`, (message) => {
          const msg: ChatMessage = JSON.parse(message.body);
          setMessages((prev) => [...prev, msg]);
        });
      },
      onDisconnect: () => setIsConnected(false),
      onStompError: () => setIsConnected(false),
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [eventId, isAuthenticated]);

  // 참석자 여부 확인
  useEffect(() => {
    if (!isAuthenticated || eventId === null) { setIsParticipant(false); return; }
    attendanceApi.getMyAttendances()
      .then((list) => setIsParticipant(list.some((a) => a.eventId === eventId)))
      .catch(() => setIsParticipant(false));
  }, [isAuthenticated, eventId]);

  // 비로그인 유저: 30초 polling
  useEffect(() => {
    if (isAuthenticated || eventId === null) return;

    pollingRef.current = setInterval(() => {
      fetchMessages();
    }, 30000);

    return () => {
      if (pollingRef.current) clearInterval(pollingRef.current);
    };
  }, [isAuthenticated, eventId, fetchMessages]);

  const sendMessage = () => {
    if (!input.trim() || !clientRef.current?.connected || eventId === null)
      return;

    clientRef.current.publish({
      destination: "/app/chat.sendMessage",
      body: JSON.stringify({
        eventId,
        content: input.trim(),
        type: "CHAT",
      }),
    });

    setInput("");
  };

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file || eventId === null || !clientRef.current?.connected) return;

    setIsUploading(true);
    try {
      const imageUrl = await chatApi.uploadChatImage(eventId, file);
      clientRef.current.publish({
        destination: "/app/chat.sendMessage",
        body: JSON.stringify({
          eventId,
          content: "",
          imageUrl,
          type: "IMAGE",
        }),
      });
    } catch {
      /* silent */
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  };

  const formatTime = (dateStr: string) => {
    const d = new Date(dateStr);
    const hours = d.getHours();
    const mins = String(d.getMinutes()).padStart(2, "0");
    const ampm = hours < 12 ? "오전" : "오후";
    const h = hours % 12 || 12;
    return `${ampm} ${h}:${mins}`;
  };

  if (!slug || eventId === null) {
    return (
      <div className="min-h-screen bg-[#faf9f6] flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="h-screen flex flex-col bg-[#b2c7d9]">
      {/* Header */}
      <header className="bg-white/90 backdrop-blur-md px-4 py-3 flex items-center gap-3 shadow-sm">
        <button onClick={() => navigate(-1)} className="text-gray-600">
          <IoArrowBack size={22} />
        </button>
        <div>
          <h1 className="text-sm font-semibold text-gray-800">채팅</h1>
          {isAuthenticated && (
            <span
              className={`text-[10px] ${isConnected ? "text-green-500" : "text-gray-400"}`}
            >
              {isConnected ? "연결됨" : "연결 중..."}
            </span>
          )}
        </div>
      </header>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto px-4 py-3 space-y-3 pb-20">
        {isLoading ? (
          <div className="flex justify-center py-8">
            <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
          </div>
        ) : messages.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-sm text-white/60">아직 메시지가 없습니다</p>
          </div>
        ) : (
          messages.map((msg) => {
            const isMe = user?.id === msg.userId;

            return (
              <div
                key={msg.id}
                className={`flex ${isMe ? "justify-end" : "justify-start"} gap-2`}
              >
                {!isMe && (
                  <div className="w-8 h-8 rounded-full bg-white/80 flex-shrink-0 overflow-hidden flex items-center justify-center">
                    {msg.profileImageUrl ? (
                      <img
                        src={msg.profileImageUrl}
                        alt=""
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <span className="text-xs text-gray-500">
                        {msg.nickname.charAt(0)}
                      </span>
                    )}
                  </div>
                )}
                <div
                  className={`max-w-[70%] ${isMe ? "items-end" : "items-start"}`}
                >
                  {!isMe && (
                    <p className="text-[10px] text-white/80 mb-0.5 ml-1">
                      {msg.nickname}
                    </p>
                  )}
                  <div
                    className={`flex items-end gap-1.5 ${isMe ? "flex-row-reverse" : ""}`}
                  >
                    <div
                      className={`rounded-2xl overflow-hidden ${
                        isMe
                          ? "bg-[#fee500] text-gray-800 rounded-br-md"
                          : "bg-white text-gray-700 rounded-bl-md"
                      } ${msg.imageUrl ? "p-1" : "px-3.5 py-2"}`}
                    >
                      {msg.imageUrl ? (
                        <img
                          src={msg.imageUrl}
                          alt=""
                          className="max-w-full max-h-48 rounded-xl cursor-pointer"
                          onClick={() => setViewerImage(msg.imageUrl)}
                        />
                      ) : (
                        <span className="text-sm leading-relaxed">
                          {msg.content}
                        </span>
                      )}
                    </div>
                    <span className="text-[9px] text-white/60 flex-shrink-0">
                      {formatTime(msg.createdAt)}
                    </span>
                  </div>
                </div>
              </div>
            );
          })
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input */}
      {isAuthenticated && isParticipant ? (
        <div className="bg-white px-4 py-3 flex items-center gap-2">
          <input
            ref={fileInputRef}
            type="file"
            accept="image/jpeg,image/jpg,image/png,image/webp"
            className="hidden"
            onChange={handleImageUpload}
          />
          <button
            onClick={() => fileInputRef.current?.click()}
            disabled={!isConnected || isUploading}
            className="text-gray-400 disabled:opacity-30"
          >
            <IoAdd size={24} />
          </button>
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
      ) : (
        <div className="bg-white px-4 py-3 flex items-center justify-center border-t border-gray-100">
          <p className="text-sm text-gray-400">일정 참석자만 채팅 사용 가능합니다.</p>
        </div>
      )}

      {/* Image Viewer */}
      {viewerImage && (
        <ImageViewer
          imageUrls={[viewerImage]}
          initialIndex={0}
          onClose={() => setViewerImage(null)}
        />
      )}
    </div>
  );
};

export default ChatPage;
