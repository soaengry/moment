import { type FC, useState } from "react";
import { toast } from "react-toastify";
import type { AccountGroupWithAccounts, AccountResponse, AccountSide } from "../types";

interface Props {
  accountGroups: AccountGroupWithAccounts[];
}

const SIDE_LABEL: Record<AccountSide, string> = {
  GROOM: "신랑측",
  GROOM_FAMILY: "신랑 혼주측",
  BRIDE: "신부측",
  BRIDE_FAMILY: "신부 혼주측",
};

const AccountCard: FC<{ account: AccountResponse }> = ({ account }) => {
  const isKakaoPay = account.bankCode === "KAKAOPAY";
  const isToss = account.bankCode === "TOSS";

  const handleCopy = async () => {
    try {
      const text = isToss ? account.accountNumber : account.accountNumber;
      await navigator.clipboard.writeText(text);
      toast.success(isToss ? "휴대폰 번호가 복사되었습니다" : "계좌번호가 복사되었습니다");
    } catch { toast.error("복사에 실패했습니다"); }
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

const AccountSection: FC<Props> = ({ accountGroups }) => {
  const [openSide, setOpenSide] = useState<AccountSide | null>(null);

  if (accountGroups.length === 0) return null;

  const sorted = [...accountGroups].sort((a, b) => a.group.orderIndex - b.group.orderIndex);

  return (
    <section className="py-10 px-6">
      <p className="text-[10px] tracking-[0.4em] text-primary/40 mb-2 uppercase font-medium text-center">
        Account
      </p>
      <p className="text-center text-xs text-gray-400 mb-6">
        축하의 마음을 전해주세요
      </p>

      <div className="space-y-3 max-w-sm mx-auto">
        {sorted.map(({ group, accounts }) => {
          const isOpen = openSide === group.side;
          const sortedAccounts = [...accounts].sort((a, b) => a.orderIndex - b.orderIndex);

          return (
            <div key={group.id}>
              <button
                onClick={() => setOpenSide((prev) => (prev === group.side ? null : group.side))}
                className="w-full flex items-center justify-between p-4 rounded-xl border border-gray-100 bg-white hover:bg-gray-50 transition-colors"
              >
                <span className="text-sm font-medium text-gray-700">
                  {SIDE_LABEL[group.side]}
                  {group.groupName && (
                    <span className="font-normal text-gray-400 ml-1.5">{group.groupName}</span>
                  )}
                </span>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                  className={`w-4 h-4 text-gray-400 transition-transform duration-200 ${isOpen ? "rotate-180" : ""}`}
                >
                  <path fillRule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z" clipRule="evenodd" />
                </svg>
              </button>
              {isOpen && (
                <div className="mt-2 space-y-2">
                  {sortedAccounts.map((account) => (
                    <AccountCard key={account.id} account={account} />
                  ))}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </section>
  );
};

export default AccountSection;
