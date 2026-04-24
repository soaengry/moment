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
  DeleteAccountPage,
  MyPostsPage,
  MyBookmarksPage,
  MyLikesPage,
  MyCommentsPage,
  PastAttendancesPage,
} from "../../domain/user/pages";
import ProtectedRoute from "./ProtectedRoute";
import Layout from "../../global/components/Layout";
import BottomNav from "../../global/components/BottomNav";
import { HomePage, SearchPage } from "../../global/pages";
import {
  EventInfoPage,
  EventCreatePage,
  EventEditPage,
} from "../../domain/event/pages";
import { FeedPage } from "../../domain/feed/pages";
import { ChatPage } from "../../domain/chat/pages";
import MyAttendancePage from "../../domain/attendance/pages/MyAttendancePage";

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
        <Route path="/search" element={<SearchPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignUpPage />} />
        <Route path="/verify-email" element={<VerifyEmailPage />} />
        <Route path="/restore-account" element={<RestoreAccountPage />} />
        <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />
        <Route path="/event/:slug" element={<EventInfoPage />} />
        <Route path="/event/:slug/feed" element={<EventInfoPage />} />
        <Route path="/event/:slug/guestbook" element={<EventInfoPage />} />
        <Route path="/event/:slug/rsvp" element={<EventInfoPage />} />

        {/* 로그인 필요 페이지 */}

        <Route
          path="/event/create"
          element={
            <ProtectedRoute>
              <EventCreatePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/event/:slug/edit"
          element={
            <ProtectedRoute>
              <EventEditPage />
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
        <Route path="/event/:slug/chat" element={<ChatPage />} />
        <Route
          path="/my-schedule"
          element={
            <ProtectedRoute>
              <MyAttendancePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-page"
          element={
            <ProtectedRoute>
              <MyPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-page/posts"
          element={
            <ProtectedRoute>
              <MyPostsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-page/bookmarks"
          element={
            <ProtectedRoute>
              <MyBookmarksPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-page/likes"
          element={
            <ProtectedRoute>
              <MyLikesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-page/past-schedules"
          element={
            <ProtectedRoute>
              <PastAttendancesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-page/comments"
          element={
            <ProtectedRoute>
              <MyCommentsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/edit-profile"
          element={
            <ProtectedRoute>
              <EditProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/delete-account"
          element={
            <ProtectedRoute>
              <DeleteAccountPage />
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
