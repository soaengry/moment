import { type FC } from "react";
import { useNavigate } from "react-router-dom";
import { IoArrowBack, IoSettingsOutline } from "react-icons/io5";
import { useScrollVisibility } from "../../../global/hooks/useScrollVisibility";

interface Props {
  title: string;
  weddingId: number;
  invitationId?: string;
  showSettings?: boolean;
}

const WeddingHeader: FC<Props> = ({ title, invitationId, showSettings }) => {
  const navigate = useNavigate();
  const isVisible = useScrollVisibility();

  return (
    <header
      className={`sticky top-0 z-20 bg-white/80 backdrop-blur-md border-b border-gray-50 transition-transform duration-300 ${
        isVisible ? "translate-y-0" : "-translate-y-full"
      }`}
    >
      <div className="flex items-center justify-between px-4 py-3">
        <button
          onClick={() => navigate("/")}
          className="p-1 text-gray-600"
        >
          <IoArrowBack size={22} />
        </button>
        <h1 className="text-base font-semibold text-gray-800 truncate mx-4">
          {title}
        </h1>
        {showSettings ? (
          <button
            onClick={() => navigate(`/wedding/${invitationId}/edit`)}
            className="p-1 text-gray-500"
          >
            <IoSettingsOutline size={20} />
          </button>
        ) : (
          <div className="w-7" />
        )}
      </div>
    </header>
  );
};

export default WeddingHeader;
