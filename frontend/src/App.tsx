import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from '@/hooks/useAuth';
import AuthGuard from '@/components/AuthGuard';
import Layout from '@/components/Layout';
import LoginPage from '@/pages/LoginPage';
import DashboardPage from '@/pages/DashboardPage';
import PullRequestListPage from '@/pages/PullRequestListPage';
import ReviewDetailPage from '@/pages/ReviewDetailPage';
import ReportsPage from '@/pages/ReportsPage';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route element={<AuthGuard><Layout /></AuthGuard>}>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/subscriptions/:id/pulls" element={<PullRequestListPage />} />
            <Route path="/reviews/:id" element={<ReviewDetailPage />} />
            <Route path="/subscriptions/:id/reports" element={<ReportsPage />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
