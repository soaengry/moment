import { type FC } from "react";
import { LoginForm } from "../components";

const LoginPage: FC = () => {
  return (
    <div className="min-h-screen flex items-center justify-center px-4 bg-bgPrimary ">
      <LoginForm />
    </div>
  );
};

export default LoginPage;
