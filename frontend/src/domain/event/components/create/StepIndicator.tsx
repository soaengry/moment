import { type FC } from "react";

interface Props {
  steps: string[];
  currentStep: number;
  onStepClick: (index: number) => void;
}

const StepIndicator: FC<Props> = ({ steps, currentStep, onStepClick }) => (
  <div className="mb-8">
    <div className="flex items-center justify-center gap-1">
      {steps.map((label, i) => (
        <div key={label} className="flex items-center gap-1">
          <button
            onClick={() => { if (i < currentStep) onStepClick(i); }}
            disabled={i > currentStep}
            className={`w-8 h-8 rounded-full text-xs font-semibold flex items-center justify-center transition-colors ${
              i === currentStep
                ? "bg-primary text-white"
                : i < currentStep
                  ? "bg-primary/20 text-primary cursor-pointer"
                  : "bg-gray-200 text-gray-400"
            }`}
          >
            {i + 1}
          </button>
          {i < steps.length - 1 && (
            <div className={`w-6 h-0.5 ${i < currentStep ? "bg-primary/30" : "bg-gray-200"}`} />
          )}
        </div>
      ))}
    </div>
    <p className="text-center text-sm text-gray-500 mt-3">{steps[currentStep]}</p>
  </div>
);

export default StepIndicator;
