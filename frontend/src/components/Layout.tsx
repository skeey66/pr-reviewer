import { Outlet, Link } from 'react-router-dom';
import { LogOut, Code2 } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';

export default function Layout() {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen" style={{ backgroundColor: 'var(--bg-primary)' }}>
      <header
        className="sticky top-0 z-40 border-b"
        style={{ backgroundColor: 'var(--bg-secondary)', borderColor: 'var(--border-primary)' }}
      >
        <div className="max-w-6xl mx-auto px-4 h-14 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 no-underline hover:no-underline">
            <Code2 className="w-6 h-6" style={{ color: 'var(--accent-blue)' }} />
            <span className="text-lg font-bold" style={{ color: 'var(--text-primary)' }}>
              CodeRev
            </span>
          </Link>

          {user && (
            <div className="flex items-center gap-3">
              <img
                src={user.avatarUrl}
                alt={user.login}
                className="w-8 h-8 rounded-full"
              />
              <span className="text-sm hidden sm:inline" style={{ color: 'var(--text-secondary)' }}>
                {user.login}
              </span>
              <button
                onClick={logout}
                className="p-2 rounded-lg hover:opacity-80 cursor-pointer"
                style={{ color: 'var(--text-secondary)', background: 'none', border: 'none' }}
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          )}
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-6">
        <Outlet />
      </main>
    </div>
  );
}
