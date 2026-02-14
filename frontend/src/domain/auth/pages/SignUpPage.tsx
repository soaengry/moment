import { type FC } from "react";
import { SignUpForm } from "../components";

const SignUpPage: FC = () => {
  return (
    <div className="min-h-screen flex items-center justify-center px-4 py-8 bg-bgPrimary">
      <SignUpForm />
    </div>
  );
};

export default SignUpPage;
