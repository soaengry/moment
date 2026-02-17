import { type FC, useState } from "react";
import type { AccountRequest, AccountSide } from "../../types";

export interface AccountGroupFormData {
  side: AccountSide;
  groupName: string;
  orderIndex: number;
  accounts: AccountRequest[];
}

interface Props {
  initialData: AccountGroupFormData[];
  onSubmit: (groups: AccountGroupFormData[]) => void;
  onBack: () => void;
}

const SIDE_OPTIONS: { value: AccountSide; label: string }[] = [
  { value: "GROOM", label: "신랑측" },
  { value: "BRIDE", label: "신부측" },
  { value: "BOTH", label: "공동" },
];

const emptyAccount = (orderIndex: number): AccountRequest => ({
  bankName: "",
  bankCode: "",
  accountNumber: "",
  accountHolder: "",
  orderIndex,
});

const emptyGroup = (orderIndex: number): AccountGroupFormData => ({
  side: "GROOM",
  groupName: "",
  orderIndex,
  accounts: [emptyAccount(0)],
});

const AccountStep: FC<Props> = ({ initialData, onSubmit, onBack }) => {
  const [groups, setGroups] = useState<AccountGroupFormData[]>(
    initialData.length > 0 ? initialData : [],
  );

  const addGroup = () => {
    if (groups.length >= 3) return;
    setGroups((prev) => [...prev, emptyGroup(prev.length)]);
  };

  const removeGroup = (index: number) => {
    setGroups((prev) =>
      prev
        .filter((_, i) => i !== index)
        .map((g, i) => ({ ...g, orderIndex: i })),
    );
  };

  const updateGroup = (
    index: number,
    field: keyof AccountGroupFormData,
    value: unknown,
  ) => {
    setGroups((prev) =>
      prev.map((g, i) => (i === index ? { ...g, [field]: value } : g)),
    );
  };

  const addAccount = (groupIndex: number) => {
    setGroups((prev) =>
      prev.map((g, i) => {
        if (i !== groupIndex || g.accounts.length >= 2) return g;
        return {
          ...g,
          accounts: [...g.accounts, emptyAccount(g.accounts.length)],
        };
      }),
    );
  };

  const removeAccount = (groupIndex: number, accountIndex: number) => {
    setGroups((prev) =>
      prev.map((g, i) => {
        if (i !== groupIndex) return g;
        return {
          ...g,
          accounts: g.accounts
            .filter((_, ai) => ai !== accountIndex)
            .map((a, ai) => ({ ...a, orderIndex: ai })),
        };
      }),
    );
  };

  const updateAccount = (
    groupIndex: number,
    accountIndex: number,
    field: keyof AccountRequest,
    value: string | number,
  ) => {
    setGroups((prev) =>
      prev.map((g, gi) => {
        if (gi !== groupIndex) return g;
        return {
          ...g,
          accounts: g.accounts.map((a, ai) =>
            ai === accountIndex ? { ...a, [field]: value } : a,
          ),
        };
      }),
    );
  };

  const handleSubmit = () => {
    // 유효한 그룹만 (최소 1개의 유효한 계좌가 있는)
    const valid = groups
      .filter((g) =>
        g.accounts.some(
          (a) => a.bankName.trim() && a.accountNumber.trim(),
        ),
      )
      .map((g, i) => ({
        ...g,
        orderIndex: i,
        groupName: g.groupName || SIDE_OPTIONS.find((s) => s.value === g.side)?.label || "",
        accounts: g.accounts
          .filter((a) => a.bankName.trim() && a.accountNumber.trim())
          .map((a, ai) => ({ ...a, orderIndex: ai })),
      }));
    onSubmit(valid);
  };

  const inputClass =
    "w-full px-4 py-2.5 rounded-lg border border-gray-200 text-sm focus:outline-none focus:border-primary";
  const labelClass = "block text-sm font-medium text-gray-700 mb-1";

  return (
    <div className="space-y-4">
      <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100 space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-semibold text-primary">
            계좌 정보
          </h3>
          <span className="text-xs text-gray-400">
            선택사항 (최대 3그룹)
          </span>
        </div>

        {groups.map((group, gi) => (
          <div
            key={gi}
            className="p-4 rounded-xl bg-bgPrimary space-y-3 relative"
          >
            <button
              type="button"
              onClick={() => removeGroup(gi)}
              className="absolute top-3 right-3 w-6 h-6 flex items-center justify-center rounded-full text-gray-400 hover:bg-gray-200 text-xs"
            >
              ✕
            </button>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className={labelClass}>측</label>
                <select
                  value={group.side}
                  onChange={(e) =>
                    updateGroup(gi, "side", e.target.value as AccountSide)
                  }
                  className={inputClass}
                >
                  {SIDE_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className={labelClass}>그룹 이름</label>
                <input
                  value={group.groupName}
                  onChange={(e) =>
                    updateGroup(gi, "groupName", e.target.value)
                  }
                  placeholder="예: 신랑측"
                  className={inputClass}
                />
              </div>
            </div>

            {/* 계좌 목록 */}
            {group.accounts.map((account, ai) => (
              <div
                key={ai}
                className="p-3 rounded-lg bg-white border border-gray-100 space-y-2 relative"
              >
                {group.accounts.length > 1 && (
                  <button
                    type="button"
                    onClick={() => removeAccount(gi, ai)}
                    className="absolute top-2 right-2 w-5 h-5 flex items-center justify-center rounded-full text-gray-300 hover:text-gray-500 text-xs"
                  >
                    ✕
                  </button>
                )}

                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className={labelClass}>은행명</label>
                    <input
                      value={account.bankName}
                      onChange={(e) =>
                        updateAccount(gi, ai, "bankName", e.target.value)
                      }
                      placeholder="○○은행"
                      className={inputClass}
                    />
                  </div>
                  <div>
                    <label className={labelClass}>은행코드</label>
                    <input
                      value={account.bankCode}
                      onChange={(e) =>
                        updateAccount(gi, ai, "bankCode", e.target.value)
                      }
                      placeholder="004"
                      className={inputClass}
                    />
                  </div>
                </div>
                <div>
                  <label className={labelClass}>예금주</label>
                  <input
                    value={account.accountHolder}
                    onChange={(e) =>
                      updateAccount(gi, ai, "accountHolder", e.target.value)
                    }
                    placeholder="홍길동"
                    className={inputClass}
                  />
                </div>
                <div>
                  <label className={labelClass}>계좌번호</label>
                  <input
                    value={account.accountNumber}
                    onChange={(e) =>
                      updateAccount(gi, ai, "accountNumber", e.target.value)
                    }
                    placeholder="123-456-789012"
                    className={inputClass}
                  />
                </div>
                <div>
                  <label className={labelClass}>카카오페이 송금 URL</label>
                  <input
                    value={account.kakaoPayUrl ?? ""}
                    onChange={(e) =>
                      updateAccount(gi, ai, "kakaoPayUrl", e.target.value)
                    }
                    placeholder="https://qr.kakaopay.com/..."
                    className={inputClass}
                  />
                </div>
              </div>
            ))}

            {group.accounts.length < 2 && (
              <button
                type="button"
                onClick={() => addAccount(gi)}
                className="w-full py-2 rounded-lg border border-dashed border-gray-300 text-gray-400 text-xs font-medium hover:bg-white transition-colors"
              >
                + 계좌 추가 (최대 2개)
              </button>
            )}
          </div>
        ))}

        {groups.length < 3 && (
          <button
            type="button"
            onClick={addGroup}
            className="w-full py-2.5 rounded-lg border-2 border-dashed border-green-200 text-primary text-sm font-medium hover:bg-bgPrimary transition-colors"
          >
            + 계좌 그룹 추가
          </button>
        )}
      </div>

      <div className="flex gap-3">
        <button
          type="button"
          onClick={onBack}
          className="flex-1 py-3 rounded-xl border border-gray-200 text-gray-600 font-semibold hover:bg-gray-50 transition-colors"
        >
          이전
        </button>
        <button
          type="button"
          onClick={handleSubmit}
          className="flex-1 py-3 rounded-xl bg-primary text-white font-semibold hover:bg-primaryHover transition-colors"
        >
          다음
        </button>
      </div>
    </div>
  );
};

export default AccountStep;
