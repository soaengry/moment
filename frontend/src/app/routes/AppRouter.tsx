import { type FC } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import {
  LoginPage,
  SignUpPage,
  VerifyEmailPage,
  RestoreAccountPage,
  OAuth2CallbackPage,
} from "../../domain/auth/pages";
import {
  MyPage,
  EditProfilePage,
  ChangePasswordPage,
  DeleteAccountPage,
} from "../../domain/user/pages";
import ProtectedRoute from "./ProtectedRoute";
import Layout from "../../global/components/Layout";
import BottomNav from "../../global/components/BottomNav";
import { HomePage } from "../../global/pages";
import {
  WeddingInfoPage,
  WeddingCreatePage,
  WeddingEditPage,
} from "../../domain/wedding/pages";
import { FeedPage } from "../../domain/feed/pages";
import { ChatPage, ChatRoomPage } from "../../domain/chat/pages";
import MySchedulePage from "../../domain/schedule/pages/MySchedulePage";

const AppRouter: FC = () => {
  return (
    <BrowserRouter>
      <BottomNav />
      <Routes>
        {/* 비로그인 페이지 */}
        <Route
          path="/"
          element={
            <Layout>
              <HomePage />
            </Layout>
          }
        />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignUpPage />} />
        <Route path="/verify-email" element={<VerifyEmailPage />} />
        <Route path="/restore-account" element={<RestoreAccountPage />} />
        <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />
        <Route path="/wedding/:invitationId" element={<WeddingInfoPage />} />

        {/* 로그인 필요 페이지 */}

        <Route
          path="/wedding/create"
          element={
            <ProtectedRoute>
              <WeddingCreatePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/wedding/:weddingId/edit"
          element={
            <ProtectedRoute>
              <WeddingEditPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/feed"
          element={
            <ProtectedRoute>
              <FeedPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/wedding/:weddingId/chat"
          element={
            <ProtectedRoute>
              <ChatPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/wedding/:weddingId/chat/:roomId"
          element={
            <ProtectedRoute>
              <ChatRoomPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-schedule"
          element={
            <ProtectedRoute>
              <Layout>
                <MySchedulePage />
              </Layout>
            </ProtectedRoute>
          }
        />
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
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
};

export default AppRouter;
