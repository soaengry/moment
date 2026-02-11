import { type FC } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import {
  LoginPage,
  SignUpPage,
  VerifyEmailPage,
  RestoreAccountPage,
} from "../../domain/auth/pages";
import {
  MyPage,
  EditProfilePage,
  ChangePasswordPage,
  DeleteAccountPage,
} from "../../domain/user/pages";
import ProtectedRoute from "./ProtectedRoute";
import Layout from "../../global/components/Layout";

const AppRouter: FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        {/* 비로그인 페이지 */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignUpPage />} />
        <Route path="/verify-email" element={<VerifyEmailPage />} />
        <Route path="/restore-account" element={<RestoreAccountPage />} />

        {/* 로그인 필요 페이지 */}
        <Route
          path="/my-page"
          element={
            <ProtectedRoute>
              <Layout>
                <MyPage />
              </Layout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/edit-profile"
          element={
            <ProtectedRoute>
              <Layout>
                <EditProfilePage />
              </Layout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/change-password"
          element={
            <ProtectedRoute>
              <Layout>
                <ChangePasswordPage />
              </Layout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/delete-account"
          element={
            <ProtectedRoute>
              <Layout>
                <DeleteAccountPage />
              </Layout>
            </ProtectedRoute>
          }
        />

        {/* 기본 리다이렉트 */}
        <Route path="/" element={<Navigate to="/my-page" replace />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
};

export default AppRouter;
