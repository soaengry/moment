import { type FC, useState } from "react";
import type { EventType } from "../../types";

interface Props {
  initialType?: EventType;
  onSubmit: (eventType: EventType) => void;
}

interface TemplateOption {
  type: EventType;
  icon: string;
  displayName: string;
  description: string;
}

const TEMPLATE_OPTIONS: TemplateOption[] = [
  {
    type: "WEDDING",
    icon: "💍",
    displayName: "웨딩",
    description: "결혼식 초대장을 만들어보세요",
  },
  {
    type: "GATHERING",
    icon: "🥂",
    displayName: "모임",
    description: "각종 모임 초대장을 만들어보세요",
  },
];

const TemplateSelectStep: FC<Props> = ({ initialType, onSubmit }) => {
  const [selected, setSelected] = useState<EventType | null>(
    initialType ?? null,
  );

  return (
    <div className="space-y-6">
      <div className="text-center">
        <p className="text-sm text-gray-500">어떤 초대장을 만들까요?</p>
      </div>

      <div className="space-y-3">
        {TEMPLATE_OPTIONS.map((t) => {
          const isActive = selected === t.type;
          return (
            <button
              key={t.type}
              type="button"
              onClick={() => setSelected(t.type)}
              className={`w-full text-left p-5 rounded-2xl border-2 transition-all ${
                isActive
                  ? "border-primary bg-primary/5 shadow-md"
                  : "border-gray-200 bg-white hover:border-primary/40"
              }`}
            >
              <div className="flex items-center gap-4">
                <div className="w-14 h-14 rounded-xl bg-primary/10 flex items-center justify-center text-2xl flex-shrink-0">
                  {t.icon}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <p className="text-base font-semibold text-gray-800">
                      {t.displayName}
                    </p>
                    {isActive && (
                      <span className="text-[10px] bg-primary text-white px-2 py-0.5 rounded-full font-medium">
                        선택됨
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-gray-400 mt-1 leading-relaxed">
                    {t.description}
                  </p>
                </div>
                <div
                  className={`w-5 h-5 rounded-full border-2 flex-shrink-0 transition-all ${
                    isActive
                      ? "border-primary bg-primary"
                      : "border-gray-300"
                  }`}
                >
                  {isActive && (
                    <svg
                      viewBox="0 0 20 20"
                      fill="white"
                      className="w-full h-full"
                    >
                      <path
                        fillRule="evenodd"
                        d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                        clipRule="evenodd"
                      />
                    </svg>
                  )}
                </div>
              </div>
            </button>
          );
        })}
      </div>

      <button
        type="button"
        disabled={!selected}
        onClick={() => selected && onSubmit(selected)}
        className="w-full py-3 rounded-xl bg-primary text-white font-semibold hover:bg-primaryHover transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
      >
        다음
      </button>
    </div>
  );
};

export default TemplateSelectStep;
