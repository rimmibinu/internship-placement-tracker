import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { useToast } from '../components/Toast.jsx';
import { Empty, Loading, StatusBadge, fmtDateTime, useFetch } from '../components/ui.jsx';

const STATUSES = ['APPLIED', 'SHORTLISTED', 'INTERVIEW', 'OFFERED', 'REJECTED', 'WITHDRAWN'];

export default function Applications() {
  const { isAdmin } = useAuth();
  const toast = useToast();
  const [status, setStatus] = useState('');
  const [q, setQ] = useState('');
  const { data, error, loading, reload } = useFetch(() => api('/applications', { params: { status } }), [status]);

  const rows = useMemo(() => {
    const t = q.trim().toLowerCase();
    if (!data) return [];
    return t ? data.filter((a) => `${a.studentName} ${a.rollNo} ${a.opportunityTitle} ${a.companyName}`.toLowerCase().includes(t)) : data;
  }, [data, q]);

  async function move(a, s) {
    try { await api(`/applications/${a.id}/status`, { method: 'PUT', body: { status: s } }); toast(`${a.studentName} → ${s}`); reload(); }
    catch (e) { toast(e.message, 'error'); }
  }
  async function withdraw(a) {
    if (!window.confirm('Withdraw this application?')) return;
    try { await api(`/applications/${a.id}/withdraw`, { method: 'PUT' }); toast('Application withdrawn'); reload(); }
    catch (e) { toast(e.message, 'error'); }
  }

  return (
    <>
      <h1>{isAdmin ? 'All applications' : 'My applications'}</h1>
      <div className="card filters">
        <input placeholder="Search…" value={q} onChange={(e) => setQ(e.target.value)} />
        <select value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">All statuses</option>{STATUSES.map((s) => <option key={s}>{s}</option>)}
        </select>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      {loading ? <Loading /> : rows.length === 0 ? <Empty text="No applications found." /> : (
        <div className="card table-wrap"><table className="table">
          <thead><tr>{isAdmin && <th>Student</th>}<th>Opportunity</th><th>Applied</th><th>Status</th><th>Remarks</th><th></th></tr></thead>
          <tbody>{rows.map((a) => (
            <tr key={a.id}>
              {isAdmin && <td>{a.studentName}<div className="hint">{a.rollNo} · {a.department}</div></td>}
              <td><Link to={`/opportunities/${a.opportunityId}`}>{a.opportunityTitle}</Link><div className="hint">{a.companyName} · {a.type}</div></td>
              <td>{fmtDateTime(a.appliedAt)}</td>
              <td><StatusBadge status={a.status} /></td>
              <td>{a.remarks || '—'}</td>
              <td className="actions">
                {isAdmin
                  ? a.allowedNext.map((s) => <button key={s} className={`btn btn-sm ${s === 'REJECTED' ? 'btn-danger' : ''}`} onClick={() => move(a, s)}>{s}</button>)
                  : ['APPLIED', 'SHORTLISTED', 'INTERVIEW'].includes(a.status) && <button className="btn btn-sm btn-danger" onClick={() => withdraw(a)}>Withdraw</button>}
              </td>
            </tr>
          ))}</tbody>
        </table></div>
      )}
    </>
  );
}
