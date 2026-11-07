import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useAuth } from '../auth.jsx';

export default function Layout() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const link = ({ isActive }) => (isActive ? 'active' : undefined);
  const close = () => setOpen(false);

  return (
    <>
      <header className="topbar">
        <div className="brand">🎓 Placement Tracker</div>
        <button className="menu-btn" aria-label="Toggle menu" onClick={() => setOpen(!open)}>☰</button>
        <nav className={open ? 'nav open' : 'nav'} onClick={close}>
          <NavLink to="/" end className={link}>Dashboard</NavLink>
          <NavLink to="/opportunities" className={link}>Opportunities</NavLink>
          <NavLink to="/applications" className={link}>{isAdmin ? 'Applications' : 'My Applications'}</NavLink>
          {isAdmin && <NavLink to="/companies" className={link}>Companies</NavLink>}
          {isAdmin && <NavLink to="/students" className={link}>Students</NavLink>}
          {!isAdmin && <NavLink to="/profile" className={link}>Profile</NavLink>}
        </nav>
        <div className="user">
          <span className="user-name">{user.name}</span>
          <span className={`badge ${isAdmin ? 'badge-purple' : 'badge-blue'}`}>{isAdmin ? 'Admin' : 'Student'}</span>
          <button className="btn btn-sm btn-ghost" onClick={() => { logout(); navigate('/login'); }}>Logout</button>
        </div>
      </header>
      <main className="container"><Outlet /></main>
      <footer className="footer">21CSE354T Full Stack Web Development · Task 4 · Demo data is synthetic</footer>
    </>
  );
}
