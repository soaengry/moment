import { type FC, type ReactNode } from "react";
import Header from "./Header";

interface LayoutProps {
  children: ReactNode;
}

const Layout: FC<LayoutProps> = ({ children }) => {
  return (
    <div className="min-h-screen flex flex-col bg-bgPrimary">
      <Header />
      <main className="flex-1 flex items-center justify-center pt-16">
        {children}
      </main>
    </div>
  );
};

export default Layout;
