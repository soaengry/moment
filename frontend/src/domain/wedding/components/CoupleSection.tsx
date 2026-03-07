import { type FC, useRef } from "react";
import { motion, useInView } from "framer-motion";
import { slideUp } from "../../../global/constants/animations";
import type { CoupleResponse } from "../types";

interface Props {
  couples: CoupleResponse[];
}

const CoupleSection: FC<Props> = ({ couples }) => {
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true, amount: 0.3 });

  const groom = couples.find((c) => c.role === "GROOM");
  const bride = couples.find((c) => c.role === "BRIDE");

  if (!groom && !bride) return null;

  const renderParents = (person: CoupleResponse) => {
    const parts: string[] = [];
    if (person.fatherName)
      parts.push(`${!person.isFatherAlive ? "故 " : ""}${person.fatherName}`);
    if (person.motherName)
      parts.push(`${!person.isMotherAlive ? "故 " : ""}${person.motherName}`);
    if (parts.length === 0) return null;
    return <p className="text-xs text-gray-400 mt-1">{parts.join(" · ")}</p>;
  };

  const renderPerson = (person: CoupleResponse, label: string) => (
    <div className="flex flex-col items-center text-center flex-1">
      {person.profileImageUrl && (
        <img
          src={person.profileImageUrl}
          alt={person.name}
          className="w-24 h-24 rounded-full object-cover border-[3px] border-white shadow-lg mb-3"
        />
      )}
      <span className="text-[10px] tracking-[0.3em] text-primary/60 font-medium mb-1 uppercase">
        {label}
      </span>
      <p className="text-lg font-semibold text-gray-800">{person.name}</p>
      {renderParents(person)}

      {person.introduction && (
        <p className="text-xs text-gray-400 mt-2 leading-relaxed max-w-[140px] min-h-10">
          {person.introduction}
        </p>
      )}

      {person.contact && (
        <a
          href={`tel:${person.contact}`}
          className="mt-3 inline-flex items-center gap-1 px-3 py-1 rounded-full border border-primary/20 text-primary text-[10px] font-medium hover:bg-primary/5 transition-colors"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
            className="w-3 h-3"
          >
            <path
              fillRule="evenodd"
              d="M1.5 4.5a3 3 0 013-3h1.372c.86 0 1.61.586 1.819 1.42l1.105 4.423a1.875 1.875 0 01-.694 1.955l-1.293.97c-.135.101-.164.249-.126.352a11.285 11.285 0 006.697 6.697c.103.038.25.009.352-.126l.97-1.293a1.875 1.875 0 011.955-.694l4.423 1.105c.834.209 1.42.959 1.42 1.82V19.5a3 3 0 01-3 3h-2.25C8.552 22.5 1.5 15.448 1.5 6.75V4.5z"
              clipRule="evenodd"
            />
          </svg>
          연락하기
        </a>
      )}
    </div>
  );

  return (
    <motion.section
      ref={ref}
      variants={slideUp}
      initial="hidden"
      animate={isInView ? "visible" : "hidden"}
      className="py-10 px-6 text-center"
    >
      <p className="text-[10px] tracking-[0.4em] text-primary/40 mb-8 uppercase font-medium">
        Invitation
      </p>

      <div className="flex gap-6 justify-center items-start">
        {groom && renderPerson(groom, "Groom")}
        <div className="pt-8">
          <span className="text-xl text-primary/20 font-serif">&</span>
        </div>
        {bride && renderPerson(bride, "Bride")}
      </div>
    </motion.section>
  );
};

export default CoupleSection;
