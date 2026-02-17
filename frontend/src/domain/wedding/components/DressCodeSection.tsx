import { type FC } from "react";
import type { WeddingResponse } from "../types";

interface Props {
  wedding: WeddingResponse;
}

const InfoCard: FC<{ title: string; content: string }> = ({
  title,
  content,
}) => (
  <div className="p-4 rounded-xl bg-bgPrimary">
    <p className="text-sm font-semibold text-gray-700 mb-1">{title}</p>
    <p className="text-sm text-gray-500 whitespace-pre-line">{content}</p>
  </div>
);

const DressCodeSection: FC<Props> = ({ wedding }) => {
  const { dressCode, notice, parkingInfo, mealInfo } = wedding;
  const hasContent = dressCode || notice || parkingInfo || mealInfo;

  if (!hasContent) return null;

  return (
    <section className="bg-white rounded-2xl shadow-lg p-6 border border-green-100">
      <h3 className="text-center text-sm text-gray-400 tracking-widest mb-6">
        INFORMATION
      </h3>

      <div className="space-y-3">
        {dressCode && <InfoCard title="드레스 코드" content={dressCode} />}
        {notice && <InfoCard title="유의사항" content={notice} />}
        {parkingInfo && <InfoCard title="주차 안내" content={parkingInfo} />}
        {mealInfo && <InfoCard title="식사 안내" content={mealInfo} />}
      </div>
    </section>
  );
};

export default DressCodeSection;
