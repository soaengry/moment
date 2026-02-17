import { type FC, useState } from "react";
import { toast } from "react-toastify";
import type { AccountGroupWithAccounts, AccountResponse, AccountSide } from "../types";

interface Props {
  accountGroups: AccountGroupWithAccounts[];
}

const SIDE_LABEL: Record<AccountSide, string> = {
  GROOM: "신랑측",
  BRIDE: "신부측",
  BOTH: "공동",
};

const AccountCard: FC<{ account: AccountResponse }> = ({ account }) => {
  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(account.accountNumber);
      toast.success("계좌번호가 복사되었습니다");
    } catch {
      toast.error("복사에 실패했습니다");
    }
  };

  const handleKakaoPay = () => {
    if (account.kakaoPayUrl) {
      window.open(account.kakaoPayUrl, "_blank");
    }
  };

  return (
    <div className="p-4 rounded-xl bg-bgPrimary">
      <div className="flex items-center justify-between mb-2">
        <span className="text-sm font-semibold text-gray-700">
          {account.bankName}
        </span>
        <span className="text-xs text-gray-400">{account.accountHolder}</span>
      </div>

      <p className="text-sm text-gray-600 font-mono mb-3">
        {account.accountNumber}
      </p>

      <div className="flex gap-2">
        <button
          onClick={handleCopy}
          className="flex-1 py-2 rounded-lg border border-gray-200 text-gray-600 text-xs font-medium hover:bg-gray-50 transition-colors"
        >
          계좌번호 복사
        </button>
        {account.kakaoPayUrl && (
          <button
            onClick={handleKakaoPay}
            className="flex-1 py-2 rounded-lg bg-[#FEE500] text-[#191919] text-xs font-medium hover:opacity-90 transition-colors"
          >
            카카오페이 송금
          </button>
        )}
      </div>
    </div>
  );
};

const AccountSection: FC<Props> = ({ accountGroups }) => {
  const [openSide, setOpenSide] = useState<AccountSide | null>(null);

  if (accountGroups.length === 0) return null;

  const sorted = [...accountGroups].sort(
    (a, b) => a.group.orderIndex - b.group.orderIndex,
  );

  const toggleSide = (side: AccountSide) => {
    setOpenSide((prev) => (prev === side ? null : side));
  };

  return (
    <section className="bg-white rounded-2xl shadow-lg p-6 border border-green-100">
      <h3 className="text-center text-sm text-gray-400 tracking-widest mb-2">
        ACCOUNT
      </h3>
      <p className="text-center text-xs text-gray-400 mb-6">
        축하의 마음을 전해주세요
      </p>

      <div className="space-y-3">
        {sorted.map(({ group, accounts }) => {
          const isOpen = openSide === group.side;
          const sortedAccounts = [...accounts].sort(
            (a, b) => a.orderIndex - b.orderIndex,
          );

          return (
            <div key={group.id}>
              <button
                onClick={() => toggleSide(group.side)}
                className="w-full flex items-center justify-between p-4 rounded-xl border border-green-100 hover:bg-bgPrimary transition-colors"
              >
                <span className="text-sm font-semibold text-gray-700">
                  {SIDE_LABEL[group.side]}{" "}
                  <span className="font-normal text-gray-400">
                    {group.groupName}
                  </span>
                </span>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                  className={`w-5 h-5 text-gray-400 transition-transform ${isOpen ? "rotate-180" : ""}`}
                >
                  <path
                    fillRule="evenodd"
                    d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z"
                    clipRule="evenodd"
                  />
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
