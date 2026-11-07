import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { Empty, Loading, StatusBadge, fmtDate, fmtPay, useFetch } from '../components/ui.jsx';

export default function Opportunities() {
  const { isAdmin } = useAuth();
  const [q, setQ] = useState('');
  const [debounced, setDebounced] = useState('');
  const [type, setType] = useState('');
  const [companyId, setCompanyId] = useState('');
  const [openOnly, setOpenOnly] = useState(!isAdmin);

  useEffect(() => {
    const t = setTimeout(() => setDebounced(q), 300);
    return () => clearTimeout(t);
  }, [q]);

  const { data: companies } = useFetch(() => api('/companies'), []);
  const { data, error, loading } = useFetch(
    () => api('/opportunities', { params: { q: debounced, type, companyId, openOnly } }),
    [debounced, type, companyId, openOnly]
  );

  return (
    <>
      <div className="page-head">
        <h1>Opportunities</h1>
        {isAdmin && <Link className="btn btn-primary" to="/opportunities/new">+ New opportunity</Link>}
      </div>
      <div className="card filters">
        <input placeholder="Search title, company or location…" value={q} onChange={(e) => setQ(e.target.value)} />
        <select value={type} onChange={(e) => setType(e.target.value)}>
          <option value="">All types</option><option value="INTERNSHIP">Internship</option><option value="PLACEMENT">Placement</option>
        </select>
        <select value={companyId} onChange={(e) => setCompanyId(e.target.value)}>
          <option value="">All companies</option>
          {(companies || []).map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <label className="check"><input type="checkbox" checked={openOnly} onChange={(e) => setOpenOnly(e.target.checked)} /> Open only</label>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      {loading ? <Loading /> : data.length === 0 ? <Empty text="No opportunities match your filters." /> : (
        <div className="grid cards">
          {data.map((o) => (
            <Link key={o.id} to={`/opportunities/${o.id}`} className="card opp-card">
              <div className="row-between"><StatusBadge status={o.type} /><StatusBadge status={o.expired ? 'EXPIRED' : o.status} /></div>
              <h3>{o.title}</h3>
              <div className="muted">{o.companyName} · {o.location || 'Location TBA'}</div>
              <div className="kv"><span>Pay</span><b>{fmtPay(o)}</b></div>
              <div className="kv"><span>Min CGPA</span><b>{o.minCgpa}</b></div>
              <div className="kv"><span>Deadline</span><b>{fmtDate(o.deadline)}</b></div>
              <div className="kv"><span>Openings</span><b>{o.offersMade}/{o.openings} filled</b></div>
            </Link>
          ))}
        </div>
      )}
    </>
  );
}
