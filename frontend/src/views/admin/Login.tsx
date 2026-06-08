import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { authAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';

export default function AdminLogin() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const res = await authAPI.login({ email, password });
      const { token, email: userEmail, role } = res.data;

      if (role !== 'ADMIN') {
        setError('This portal is restricted to administrators only.');
        setLoading(false);
        return;
      }

      login(token, userEmail, role);
      navigate('/admin/dashboard');
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosErr = err as { response?: { data?: { message?: string }; status?: number } };
        if (axiosErr.response?.status === 401 || axiosErr.response?.status === 403) {
          setError('Invalid credentials.');
        } else {
          setError(axiosErr.response?.data?.message || 'Login failed.');
        }
      } else {
        setError('Unable to connect to server. Is the backend running?');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page" data-theme="admin">
      <div className="auth-card">
        <div className="auth-card__header">
          <div className="auth-card__logo">🛡️ PayHa Admin</div>
          <p className="auth-card__subtitle">Administrative Control Portal</p>
        </div>

        {error && <div className="alert alert--error">⚠ {error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="admin-email">Admin Email</label>
            <input
              id="admin-email"
              className="form-input"
              type="email"
              placeholder="admin@payha.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="admin-password">Password</label>
            <input
              id="admin-password"
              className="form-input"
              type="password"
              placeholder="Enter admin password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button
            id="admin-login-btn"
            className="btn btn--primary btn--full btn--lg"
            type="submit"
            disabled={loading}
          >
            {loading ? <span className="spinner" /> : 'Access Control Panel'}
          </button>
        </form>

        <div className="auth-card__footer">
          Not an admin? <a href="/login">Go to User Portal</a>
        </div>
      </div>
    </div>
  );
}
