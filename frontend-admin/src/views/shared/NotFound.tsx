import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="auth-page">
      <div style={{ textAlign: 'center' }}>
        <div style={{ fontSize: '4rem', marginBottom: '0.5rem' }}>🔍</div>
        <h1 style={{ fontSize: '2rem', fontWeight: 800, marginBottom: '0.5rem', color: 'var(--text-primary)' }}>
          404 – Page Not Found
        </h1>
        <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
          The page you're looking for doesn't exist.
        </p>
        <Link to="/login" className="btn btn--primary">Go to Login</Link>
      </div>
    </div>
  );
}
