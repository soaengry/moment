import { type FC, useState, useRef, useMemo } from "react";
import { motion, useInView, AnimatePresence } from "framer-motion";
import { slideUp, staggerContainer, staggerItem } from "../../../global/constants/animations";
import { toast } from "react-toastify";
import type {
  AccountGroupWithAccounts,
  AccountResponse,
  EventType,
} from "../types";
import { TEMPLATE_LABELS } from "../utils/templateLabels";

interface Props {
  accountGroups: AccountGroupWithAccounts[];
  eventType: EventType;
}

const AccountCard: FC<{ account: AccountResponse }> = ({ account }) => {
  const isKakaoPay = account.bankCode === "KAKAOPAY";
  const isToss = account.bankCode === "TOSS";

  const handleCopy = async () => {
    try {
      const text = isToss ? account.accountNumber : account.accountNumber;
      await navigator.clipboard.writeText(text);
      toast.success(
        isToss ? "휴대폰 번호가 복사되었습니다" : "계좌번호가 복사되었습니다",
      );
    } catch {
      toast.error("복사에 실패했습니다");
    }
  };

  if (isKakaoPay) {
    return (
      <div className="p-4 rounded-xl bg-white border border-gray-100">
        <div className="flex items-center justify-between mb-3">
          <span className="text-sm font-medium text-gray-700">카카오페이</span>
          <span className="text-xs text-gray-400">{account.accountHolder}</span>
        </div>
        <button
          onClick={() => window.open(account.kakaoPayUrl!, "_blank")}
          className="w-full py-2 rounded-lg bg-[#FEE500] text-[#191919] text-[11px] font-medium hover:opacity-90 transition-colors"
        >
          카카오페이
        </button>
      </div>
    );
  }

  return (
    <div className="p-4 rounded-xl bg-white border border-gray-100">
      <div className="flex items-center justify-between mb-2">
        <span className="text-sm font-medium text-gray-700">
          {isToss ? "토스" : account.bankName}
        </span>
        <span className="text-xs text-gray-400">{account.accountHolder}</span>
      </div>
      <p className="text-sm text-gray-500 font-mono tracking-wider mb-3">
        {account.accountNumber}
      </p>
      <button
        onClick={handleCopy}
        className="w-full py-2 rounded-lg border border-gray-200 text-gray-600 text-[11px] font-medium hover:bg-gray-50 transition-colors"
      >
        {isToss ? "휴대폰 번호 복사" : "계좌번호 복사"}
      </button>
    </div>
  );
};

const AccountSection: FC<Props> = ({ accountGroups, eventType }) => {
  const tl = TEMPLATE_LABELS[eventType];
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true, amount: 0.3 });
  const [openGroupId, setOpenGroupId] = useState<number | null>(null);

  const sorted = useMemo(
    () => [...accountGroups].sort((a, b) => a.group.orderIndex - b.group.orderIndex),
    [accountGroups],
  );

  if (sorted.length === 0) return null;

  return (
    <motion.section
      ref={ref}
      variants={slideUp}
      initial="hidden"
      animate={isInView ? "visible" : "hidden"}
      className="py-10 px-6"
    >
      <p className="text-[10px] tracking-[0.4em] text-primary/40 mb-2 uppercase font-medium text-center">
        Account
      </p>
      <p className="text-center text-xs text-gray-400 mb-6">
        {tl.accountDesc}
      </p>

      <div className="space-y-3 max-w-sm mx-auto">
        {sorted.map(({ group, accounts }) => {
          const isOpen = openGroupId === group.id;
          const sortedAccounts = [...accounts].sort(
            (a, b) => a.orderIndex - b.orderIndex,
          );

          return (
            <div key={group.id}>
              <button
                onClick={() =>
                  setOpenGroupId((prev) =>
                    prev === group.id ? null : group.id,
                  )
                }
                className="w-full flex items-center justify-between p-4 rounded-xl border border-gray-100 bg-white hover:bg-gray-50 transition-colors"
              >
                <span className="text-sm font-medium text-gray-700">
                  {group.groupName}
                </span>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                  className={`w-4 h-4 text-gray-400 transition-transform duration-200 ${isOpen ? "rotate-180" : ""}`}
                >
                  <path
                    fillRule="evenodd"
                    d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z"
                    clipRule="evenodd"
                  />
                </svg>
              </button>
              <AnimatePresence>
                {isOpen && (
                  <motion.div
                    variants={staggerContainer}
                    initial="hidden"
                    animate="visible"
                    exit="hidden"
                    className="mt-3 space-y-2"
                  >
                    {sortedAccounts.map((account) => (
                      <motion.div key={account.id} variants={staggerItem}>
                        <AccountCard account={account} />
                      </motion.div>
                    ))}
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          );
        })}
      </div>
    </motion.section>
  );
};

export default AccountSection;
