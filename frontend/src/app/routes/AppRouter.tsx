import { type FC } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { LoginPage, SignUpPage } from "../../domain/auth/pages";
import ProtectedRoute from "./ProtectedRoute";
import Layout from "../../global/components/Layout";

const HomePage: FC = () => (
  <div className="text-center py-16">
    <h1 className="text-3xl font-bold" style={{ color: "#88AF64" }}>
      Moment Sample
    </h1>
    <p className="mt-4 text-gray-600">소중한 순간을 함께</p>
  </div>
);

const AppRouter: FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignUpPage />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Layout>
                <HomePage />
              </Layout>
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
};

export default AppRouter;
