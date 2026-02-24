import { Github } from 'lucide-react';

export default function LoginPage() {
  const handleLogin = () => {
    window.location.href = '/oauth2/authorization/github';
  };

  return (
    <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: 'var(--bg-primary)' }}>
      <div
        className="w-full max-w-sm mx-4 rounded-xl p-8 text-center"
        style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border-primary)' }}
      >
        <h1 className="text-2xl font-bold mb-2">CodeRev</h1>
        <p className="text-sm mb-8" style={{ color: 'var(--text-secondary)' }}>
          AI 기반 PR 리뷰 자동화
        </p>
        <button
          onClick={handleLogin}
          className="w-full flex items-center justify-center gap-2 px-4 py-3 rounded-lg font-medium cursor-pointer"
          style={{
            backgroundColor: 'var(--text-primary)',
            color: 'var(--bg-primary)',
            border: 'none',
          }}
        >
          <Github className="w-5 h-5" />
          GitHub으로 로그인
        </button>
      </div>
    </div>
  );
}
