import { useEffect, useState } from 'react';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { Field } from '../components/ui.jsx';

export default function Login() {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [demo, setDemo] = useState([]);

  useEffect(() => {
    api('/auth/demo-users').then(setDemo).catch(() => {});
  }, []);

  async function submit(e) {
    e.preventDefault();
    if (!/^\S+@\S+\.\S+$/.test(email)) { setError('Enter a valid email address'); return; }
    setBusy(true);
    try { await login(email.trim()); } catch (err) { setError(err.message); setBusy(false); }
  }

  return (
    <div className="login-wrap">
      <form className="card login-card" onSubmit={submit} noValidate>
        <h1>🎓 Placement Tracker</h1>
        <p className="muted">Internship &amp; placement opportunities, applications and outcomes in one place.</p>
        <Field label="Campus email" error={error}>
          <input type="email" value={email} onChange={(e) => { setEmail(e.target.value); setError(''); }} placeholder="you@campus.demo" autoFocus />
        </Field>
        <button className="btn btn-primary btn-block" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</button>
        {demo.length > 0 && (
          <div className="demo">
            <div className="hint">Demo accounts (no password – click to fill):</div>
            {demo.map((u) => (
              <button type="button" key={u.email} className="chip" onClick={() => { setEmail(u.email); setError(''); }}>
                {u.role === 'ADMIN' ? '🛠 ' : '🎓 '}{u.name}
              </button>
            ))}
          </div>
        )}
      </form>
    </div>
  );
}
