import { useEffect, useState } from 'react';

export function StatusBadge({ status }) {
  const colors = {
    APPLIED: 'blue', SHORTLISTED: 'amber', INTERVIEW: 'purple', OFFERED: 'green', REJECTED: 'red', WITHDRAWN: 'grey',
    OPEN: 'green', CLOSED: 'grey', EXPIRED: 'red', INTERNSHIP: 'blue', PLACEMENT: 'purple',
  };
  return <span className={`badge badge-${colors[status] || 'grey'}`}>{status}</span>;
}

export function Loading({ text = 'Loading…' }) { return <div className="empty">{text}</div>; }
export function Empty({ text }) { return <div className="empty">{text}</div>; }

export function Field({ label, error, children, hint }) {
  return (
    <label className="field">
      <span className="field-label">{label}</span>
      {children}
      {hint && !error && <span className="hint">{hint}</span>}
      {error && <span className="error">{error}</span>}
    </label>
  );
}

export function Stat({ label, value, sub }) {
  return (
    <div className="card stat">
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
      {sub && <div className="hint">{sub}</div>}
    </div>
  );
}

/** Small data-loading hook used by pages (useState + useEffect). */
export function useFetch(fn, deps) {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [tick, setTick] = useState(0);
  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    fn()
      .then((d) => { if (!cancelled) { setData(d); setError(''); } })
      .catch((e) => { if (!cancelled) setError(e.message); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, tick]);
  return { data, error, loading, reload: () => setTick((t) => t + 1) };
}

export const fmtDate = (d) => (d ? new Date(d).toLocaleDateString(undefined, { day: '2-digit', month: 'short', year: 'numeric' }) : '—');
export const fmtDateTime = (d) => (d ? new Date(d).toLocaleString(undefined, { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' }) : '—');
export const fmtPay = (o) => (o.salary == null ? '—' : o.type === 'INTERNSHIP' ? `₹${Number(o.salary).toLocaleString('en-IN')}/month` : `${o.salary} LPA`);
export const DEPARTMENTS = ['CSE', 'IT', 'ECE', 'EEE', 'MECH', 'CIVIL'];
