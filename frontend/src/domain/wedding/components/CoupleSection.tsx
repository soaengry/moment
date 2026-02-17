import { type FC } from "react";
import type { CoupleResponse } from "../types";

interface Props {
  couples: CoupleResponse[];
}

const CoupleSection: FC<Props> = ({ couples }) => {
  const groom = couples.find((c) => c.role === "GROOM");
  const bride = couples.find((c) => c.role === "BRIDE");

  if (!groom && !bride) return null;

  const renderParent = (name: string | null, isAlive: boolean, label: string) => {
    if (!name) return null;
    return (
      <span className="text-sm text-gray-500">
        {!isAlive && <span className="text-gray-400">故 </span>}
        {name}
        <span className="text-gray-400 ml-1 text-xs">{label}</span>
      </span>
    );
  };

  const renderPerson = (person: CoupleResponse, label: string) => (
    <div className="flex flex-col items-center text-center flex-1">
      <img
        src={person.profileImageUrl ?? "/default-avatar.png"}
        alt={person.name}
        className="w-24 h-24 rounded-full object-cover bg-gray-100 mb-3"
      />
      <p className="text-xs text-primary font-medium mb-1">{label}</p>
      <p className="text-lg font-semibold text-gray-800 mb-2">{person.name}</p>

      <div className="flex flex-col gap-0.5 mb-2">
        {renderParent(person.fatherName, person.isFatherAlive, "아버지")}
        {renderParent(person.motherName, person.isMotherAlive, "어머니")}
      </div>

      {person.introduction && (
        <p className="text-xs text-gray-400 mt-1 leading-relaxed">
          {person.introduction}
        </p>
      )}

      {person.contact && (
        <a
          href={`tel:${person.contact}`}
          className="mt-2 inline-flex items-center justify-center w-8 h-8 rounded-full bg-primary/10 text-primary"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
            className="w-4 h-4"
          >
            <path
              fillRule="evenodd"
              d="M1.5 4.5a3 3 0 013-3h1.372c.86 0 1.61.586 1.819 1.42l1.105 4.423a1.875 1.875 0 01-.694 1.955l-1.293.97c-.135.101-.164.249-.126.352a11.285 11.285 0 006.697 6.697c.103.038.25.009.352-.126l.97-1.293a1.875 1.875 0 011.955-.694l4.423 1.105c.834.209 1.42.959 1.42 1.82V19.5a3 3 0 01-3 3h-2.25C8.552 22.5 1.5 15.448 1.5 6.75V4.5z"
              clipRule="evenodd"
            />
          </svg>
        </a>
      )}
    </div>
  );

  return (
    <section className="bg-white rounded-2xl shadow-lg p-6 border border-green-100">
      <h3 className="text-center text-sm text-gray-400 tracking-widest mb-6">
        INVITATION
      </h3>
      <div className="flex gap-6">
        {groom && renderPerson(groom, "신랑")}
        {bride && renderPerson(bride, "신부")}
      </div>
    </section>
  );
};

export default CoupleSection;
