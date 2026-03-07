import { type FC } from "react";
import { motion } from "framer-motion";
import { buttonTap } from "../../../global/constants/animations";
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
    <motion.header
      initial={{ y: 0 }}
      animate={{ y: isVisible ? 0 : "-100%" }}
      transition={{ type: "spring", stiffness: 300, damping: 30 }}
      className="sticky top-0 z-20 bg-white/80 backdrop-blur-md border-b border-gray-50"
    >
      <div className="flex items-center justify-between px-4 py-3">
        <motion.button
          onClick={() => navigate("/")}
          whileTap={buttonTap}
          className="p-1 text-gray-600"
        >
          <IoArrowBack size={22} />
        </motion.button>
        <h1 className="text-base font-semibold text-gray-800 truncate mx-4">
          {title}
        </h1>
        {showSettings ? (
          <motion.button
            onClick={() => navigate(`/wedding/${invitationId}/edit`)}
            whileTap={buttonTap}
            className="p-1 text-gray-500"
          >
            <IoSettingsOutline size={20} />
          </motion.button>
        ) : (
          <div className="w-7" />
        )}
      </div>
    </motion.header>
  );
};

export default WeddingHeader;
