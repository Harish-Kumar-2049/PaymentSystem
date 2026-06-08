import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import type { ReactNode } from 'react';

// Layouts
import UserLayout from './components/UserLayout';

// User Views
import UserLogin from './views/user/Login';
import Register from './views/user/Register';
import UserDashboard from './views/user/Dashboard';

// Shared
import NotFound from './views/shared/NotFound';

// ── Route Guards ──

function UserRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

function PublicRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth();
  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }
  return <>{children}</>;
}

function AppRoutes() {
  return (
    <Routes>
      {/* ── Public Routes ── */}
      <Route
        path="/login"
        element={
          <PublicRoute>
            <UserLogin />
          </PublicRoute>
        }
      />
      <Route
        path="/register"
        element={
          <PublicRoute>
            <Register />
          </PublicRoute>
        }
      />

      {/* ── User Portal ── */}
      <Route
        element={
          <UserRoute>
            <UserLayout />
          </UserRoute>
        }
      >
        <Route path="/dashboard" element={<UserDashboard />} />
      </Route>

      {/* ── Fallbacks ── */}
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}
