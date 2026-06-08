import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/admin/login');
  };

  const initials = user?.email ? user.email.charAt(0).toUpperCase() : '?';

  return (
    <div className="dashboard-layout" data-theme="admin">
      <aside className="sidebar">
        <div className="sidebar__brand">
          <div className="sidebar__brand-icon">🛡️</div>
          <span className="sidebar__brand-name">PayHa Admin</span>
        </div>

        <nav className="sidebar__nav">
          <NavLink
            to="/admin/dashboard"
            className={({ isActive }) =>
              `sidebar__link ${isActive ? 'sidebar__link--active' : ''}`
            }
          >
            <span className="sidebar__link-icon">🏠</span>
            Control Panel
          </NavLink>
        </nav>

        <div className="sidebar__footer">
          <div className="sidebar__user">
            <div className="sidebar__avatar">{initials}</div>
            <div className="sidebar__user-info">
              <div className="sidebar__user-name">{user?.email}</div>
              <div className="sidebar__user-role">ADMINISTRATOR</div>
            </div>
          </div>
          <button
            id="admin-logout-btn"
            className="btn btn--ghost btn--full btn--sm"
            style={{ marginTop: '0.75rem' }}
            onClick={handleLogout}
          >
            Sign Out
          </button>
        </div>
      </aside>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
