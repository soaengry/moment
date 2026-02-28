import { type FC, useState, useRef } from "react";
import type { AccountRequest, AccountSide } from "../../types";
import axiosInstance from "../../../../global/api/axiosInstance";

export type PaymentMethod = "BANK" | "KAKAOPAY" | "TOSS";

export interface AccountGroupFormData {
  side: AccountSide;
  groupName: string;
  orderIndex: number;
  accounts: (AccountRequest & { type?: PaymentMethod })[];
}

interface Props {
  initialData: AccountGroupFormData[];
  onSubmit: (groups: AccountGroupFormData[]) => void;
  onBack: () => void;
}

const SIDE_OPTIONS: { value: AccountSide; label: string }[] = [
  { value: "GROOM", label: "신랑측" },
  { value: "GROOM_FAMILY", label: "신랑 혼주측" },
  { value: "BRIDE", label: "신부측" },
  { value: "BRIDE_FAMILY", label: "신부 혼주측" },
];

const PAYMENT_METHODS: { value: PaymentMethod; label: string; icon: string }[] =
  [
    { value: "BANK", label: "은행 계좌", icon: "🏦" },
    { value: "KAKAOPAY", label: "카카오페이", icon: "💛" },
    { value: "TOSS", label: "토스", icon: "💙" },
  ];

const createAccount = (
  type: PaymentMethod,
  orderIndex: number,
): AccountRequest & { type?: PaymentMethod } => {
  if (type === "KAKAOPAY") {
    return {
      bankName: "카카오페이",
      bankCode: "KAKAOPAY",
      accountNumber: "",
      accountHolder: "",
      kakaoPayUrl: "",
      orderIndex,
      type,
    };
  }
  if (type === "TOSS") {
    return {
      bankName: "토스",
      bankCode: "TOSS",
      accountNumber: "",
      accountHolder: "",
      orderIndex,
      type,
    };
  }
  return {
    bankName: "",
    bankCode: "",
    accountNumber: "",
    accountHolder: "",
    orderIndex,
    type: "BANK",
  };
};

const getAccountType = (
  account: AccountRequest & { type?: PaymentMethod },
): PaymentMethod => {
  if (account.type) return account.type;
  if (account.bankCode === "KAKAOPAY") return "KAKAOPAY";
  if (account.bankCode === "TOSS") return "TOSS";
  return "BANK";
};

const AccountStep: FC<Props> = ({ initialData, onSubmit, onBack }) => {
  const [groups, setGroups] = useState<AccountGroupFormData[]>(
    initialData.length > 0 ? initialData : [],
  );
  const [detectingMap, setDetectingMap] = useState<Record<string, boolean>>({});
  const debounceTimers = useRef<Record<string, ReturnType<typeof setTimeout>>>(
    {},
  );

  const addGroup = () => {
    if (groups.length >= 4) return;
    const newIndex = groups.length;
    setGroups((prev) => [
      ...prev,
      {
        side: "GROOM",
        groupName: "",
        orderIndex: newIndex,
        accounts: [createAccount("BANK", 0)],
      },
    ]);
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

  const updateAccount = (
    groupIndex: number,
    accountIndex: number,
    field: keyof (AccountRequest & { type?: PaymentMethod }),
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

  const detectBank = (
    groupIndex: number,
    accountIndex: number,
    accountNumber: string,
  ) => {
    const key = `${groupIndex}-${accountIndex}`;
    const cleaned = accountNumber.replace(/[^0-9]/g, "");

    if (debounceTimers.current[key]) {
      clearTimeout(debounceTimers.current[key]);
    }

    if (cleaned.length < 3) {
      updateAccount(groupIndex, accountIndex, "bankName", "");
      updateAccount(groupIndex, accountIndex, "bankCode", "");
      return;
    }

    setDetectingMap((prev) => ({ ...prev, [key]: true }));

    debounceTimers.current[key] = setTimeout(async () => {
      try {
        const { data } = await axiosInstance.get<{
          bankCode: string;
          bankName: string;
        }>(
          `/api/banks/detect?accountNumber=${encodeURIComponent(accountNumber)}`,
        );
        setGroups((prev) =>
          prev.map((g, gi) => {
            if (gi !== groupIndex) return g;
            return {
              ...g,
              accounts: g.accounts.map((a, ai) =>
                ai === accountIndex
                  ? { ...a, bankName: data.bankName, bankCode: data.bankCode }
                  : a,
              ),
            };
          }),
        );
      } catch {
        // silent
      } finally {
        setDetectingMap((prev) => ({ ...prev, [key]: false }));
      }
    }, 400);
  };

  const handleAccountNumberChange = (
    groupIndex: number,
    accountIndex: number,
    value: string,
  ) => {
    updateAccount(groupIndex, accountIndex, "accountNumber", value);
    detectBank(groupIndex, accountIndex, value);
  };

  const getUsedTypes = (group: AccountGroupFormData): PaymentMethod[] =>
    group.accounts.map(getAccountType);

  const addAccountToGroup = (gi: number, type: PaymentMethod) => {
    setGroups((prev) =>
      prev.map((g, i) => {
        if (i !== gi || g.accounts.length >= 3) return g;
        return {
          ...g,
          accounts: [...g.accounts, createAccount(type, g.accounts.length)],
        };
      }),
    );
  };

  const removeAccount = (gi: number, ai: number) => {
    setGroups((prev) =>
      prev.map((g, i) => {
        if (i !== gi) return g;
        return {
          ...g,
          accounts: g.accounts
            .filter((_, idx) => idx !== ai)
            .map((a, idx) => ({ ...a, orderIndex: idx })),
        };
      }),
    );
  };

  const handleSubmit = () => {
    const valid = groups
      .filter((g) =>
        g.accounts.some((a) => {
          const type = getAccountType(a);
          if (type === "KAKAOPAY") return a.kakaoPayUrl?.trim();
          if (type === "TOSS") return a.accountNumber.trim();
          return a.bankCode.trim() && a.accountNumber.trim();
        }),
      )
      .map((g, i) => ({
        ...g,
        orderIndex: i,
        groupName:
          g.groupName ||
          SIDE_OPTIONS.find((s) => s.value === g.side)?.label ||
          "",
        accounts: g.accounts.map((a, ai) => {
          const { type, ...rest } = a as AccountRequest & {
            type?: PaymentMethod;
          };
          return { ...rest, orderIndex: ai, type };
        }),
      }));
    onSubmit(valid);
  };

  const inputClass =
    "w-full px-4 py-2.5 rounded-lg border border-gray-200 text-sm focus:outline-none focus:border-primary";
  const labelClass = "block text-sm font-medium text-gray-700 mb-1";

  const renderBankForm = (
    account: AccountRequest & { type?: PaymentMethod },
    gi: number,
    ai: number,
  ) => {
    const detectKey = `${gi}-${ai}`;
    const isDetecting = detectingMap[detectKey] ?? false;

    return (
      <div className="space-y-2">
        <div>
          <label className={labelClass}>계좌번호</label>
          <input
            value={account.accountNumber}
            onChange={(e) => handleAccountNumberChange(gi, ai, e.target.value)}
            placeholder="계좌번호를 입력하면 은행이 자동 감지됩니다"
            className={inputClass}
          />
        </div>

        <div className="flex items-center gap-2 min-h-[32px]">
          {isDetecting ? (
            <span className="text-xs text-gray-400">은행 감지 중...</span>
          ) : account.bankName ? (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-primary/10 text-primary text-xs font-semibold">
              {account.bankName}
            </span>
          ) : account.accountNumber.replace(/[^0-9]/g, "").length >= 3 ? (
            <span className="text-xs text-red-400">
              은행을 감지할 수 없습니다. 직접 입력해주세요.
            </span>
          ) : null}
        </div>

        {!account.bankName &&
          account.accountNumber.replace(/[^0-9]/g, "").length >= 3 &&
          !isDetecting && (
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
          )}

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
      </div>
    );
  };

  const renderKakaoPayForm = (
    account: AccountRequest & { type?: PaymentMethod },
    gi: number,
    ai: number,
  ) => (
    <div className="space-y-3">
      <div className="flex items-center gap-2 px-2 py-1.5 bg-yellow-50 rounded-lg">
        <span className="text-lg">💛</span>
        <span className="text-xs text-yellow-700 font-medium">
          카카오페이 송금 링크를 입력해주세요
        </span>
      </div>
      <div>
        <label className={labelClass}>카카오페이 송금 URL *</label>
        <input
          value={account.kakaoPayUrl ?? ""}
          onChange={(e) => updateAccount(gi, ai, "kakaoPayUrl", e.target.value)}
          placeholder="https://qr.kakaopay.com/..."
          className={inputClass}
        />
      </div>
      <div>
        <label className={labelClass}>받는 분</label>
        <input
          value={account.accountHolder}
          onChange={(e) =>
            updateAccount(gi, ai, "accountHolder", e.target.value)
          }
          placeholder="홍길동"
          className={inputClass}
        />
      </div>
    </div>
  );

  const renderTossForm = (
    account: AccountRequest & { type?: PaymentMethod },
    gi: number,
    ai: number,
  ) => (
    <div className="space-y-3">
      <div className="flex items-center gap-2 px-2 py-1.5 bg-blue-50 rounded-lg">
        <span className="text-lg">💙</span>
        <span className="text-xs text-blue-700 font-medium">
          토스 송금 정보를 입력해주세요
        </span>
      </div>
      <div>
        <label className={labelClass}>토스 ID (전화번호) *</label>
        <input
          value={account.accountNumber}
          onChange={(e) =>
            updateAccount(gi, ai, "accountNumber", e.target.value)
          }
          placeholder="010-1234-5678"
          className={inputClass}
        />
      </div>
      <div>
        <label className={labelClass}>받는 분</label>
        <input
          value={account.accountHolder}
          onChange={(e) =>
            updateAccount(gi, ai, "accountHolder", e.target.value)
          }
          placeholder="홍길동"
          className={inputClass}
        />
      </div>
    </div>
  );

  return (
    <div className="space-y-4">
      <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100 space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-semibold text-primary">
            계좌 / 송금 정보
          </h3>
          <span className="text-xs text-gray-400">선택사항 (최대 4그룹)</span>
        </div>

        {groups.map((group, gi) => {
          const usedTypes = getUsedTypes(group);
          const availableTypes = PAYMENT_METHODS.filter(
            (m) => !usedTypes.includes(m.value),
          );

          return (
            <div
              key={gi}
              className="p-4 rounded-xl bg-bgPrimary space-y-4 relative"
            >
              <button
                type="button"
                onClick={() => removeGroup(gi)}
                className="absolute top-3 right-3 w-6 h-6 flex items-center justify-center rounded-full text-gray-400 hover:bg-gray-200 text-xs"
              >
                ✕
              </button>

              {/* Side selector */}
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

              {/* Account list */}
              <div className="space-y-3">
                {group.accounts.map((account, ai) => {
                  const accountType = getAccountType(account);

                  return (
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

                      <div className="flex items-center gap-1.5 mb-1">
                        <span className="text-sm">
                          {accountType === "KAKAOPAY"
                            ? "💛"
                            : accountType === "TOSS"
                              ? "💙"
                              : "🏦"}
                        </span>
                        <span className="text-xs font-medium text-gray-500">
                          {
                            PAYMENT_METHODS.find((m) => m.value === accountType)
                              ?.label
                          }
                        </span>
                      </div>

                      {accountType === "BANK" &&
                        renderBankForm(account, gi, ai)}
                      {accountType === "KAKAOPAY" &&
                        renderKakaoPayForm(account, gi, ai)}
                      {accountType === "TOSS" &&
                        renderTossForm(account, gi, ai)}
                    </div>
                  );
                })}
              </div>

              {/* Add account button */}
              {group.accounts.length < 3 && availableTypes.length > 0 && (
                <div className="space-y-2">
                  <p className="text-[11px] text-gray-400 font-medium">
                    송금 방법 추가
                  </p>
                  <div className="flex gap-2">
                    {availableTypes.map((method) => (
                      <button
                        key={method.value}
                        type="button"
                        onClick={() => addAccountToGroup(gi, method.value)}
                        className="flex items-center gap-1.5 px-3 py-2 rounded-lg border border-dashed border-gray-300 text-gray-400 text-xs font-medium hover:bg-white transition-colors"
                      >
                        <span>{method.icon}</span>
                        <span>{method.label}</span>
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
          );
        })}

        {groups.length < 4 && (
          <button
            type="button"
            onClick={addGroup}
            className="w-full py-2.5 rounded-lg border-2 border-dashed border-green-200 text-primary text-sm font-medium hover:bg-bgPrimary transition-colors"
          >
            + 계좌 / 송금 추가
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
