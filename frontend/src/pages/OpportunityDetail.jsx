import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { useToast } from '../components/Toast.jsx';
import { Empty, Loading, StatusBadge, fmtDate, fmtDateTime, fmtPay, useFetch } from '../components/ui.jsx';

function Applicants({ opportunityId, onChange }) {
  const toast = useToast();
  const { data, error, loading, reload } = useFetch(() => api('/applications', { params: { opportunityId } }), [opportunityId]);
  const [remarks, setRemarks] = useState({});

  async function move(a, status) {
    try {
      await api(`/applications/${a.id}/status`, { method: 'PUT', body: { status, remarks: remarks[a.id] || a.remarks || '' } });
      toast(`${a.studentName} → ${status}`);
      reload();
      onChange();
    } catch (e) { toast(e.message, 'error'); }
  }

  if (loading) return <Loading />;
  if (error) return <div className="alert alert-error">{error}</div>;
  if (data.length === 0) return <Empty text="No applicants yet." />;
  return (
    <div className="table-wrap"><table className="table">
      <thead><tr><th>Student</th><th>Dept</th><th className="num">CGPA</th><th>Status</th><th>Remarks</th><th>Move to</th></tr></thead>
      <tbody>
        {data.map((a) => (
          <tr key={a.id}>
            <td>{a.studentName}<div className="hint">{a.rollNo}</div></td>
            <td>{a.department}</td><td className="num">{a.cgpa}</td>
            <td><StatusBadge status={a.status} /></td>
            <td>
              {a.allowedNext.length > 0
                ? <input className="input-sm" placeholder="Optional note" value={remarks[a.id] ?? a.remarks ?? ''} maxLength={300}
                    onChange={(e) => setRemarks({ ...remarks, [a.id]: e.target.value })} />
                : (a.remarks || '—')}
            </td>
            <td className="actions">
              {a.allowedNext.length === 0 ? <span className="muted">Final</span>
                : a.allowedNext.map((s) => <button key={s} className={`btn btn-sm ${s === 'REJECTED' ? 'btn-danger' : ''}`} onClick={() => move(a, s)}>{s}</button>)}
            </td>
          </tr>
        ))}
      </tbody>
    </table></div>
  );
}

export default function OpportunityDetail() {
  const { id } = useParams();
  const { user, isAdmin } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [note, setNote] = useState('');
  const [busy, setBusy] = useState(false);

  const opp = useFetch(() => api(`/opportunities/${id}`), [id]);
  const elig = useFetch(() => (isAdmin ? Promise.resolve(null) : api(`/opportunities/${id}/eligibility`)), [id]);
  const mine = useFetch(() => (isAdmin ? Promise.resolve([]) : api('/applications', { params: { opportunityId: id } })), [id]);

  if (opp.loading) return <Loading />;
  if (opp.error) return <div className="alert alert-error">{opp.error}</div>;
  const o = opp.data;
  const myApp = mine.data && mine.data[0];
  const canApply = !isAdmin && o.status === 'OPEN' && !o.expired && !myApp;

  async function apply() {
    if (note.length > 500) { toast('Cover note must be at most 500 characters', 'error'); return; }
    setBusy(true);
    try {
      await api('/applications', { method: 'POST', body: { studentId: user.studentId, opportunityId: o.id, coverNote: note } });
      toast('Application submitted!');
      mine.reload(); opp.reload();
    } catch (e) { toast(e.message, 'error'); } finally { setBusy(false); }
  }

  async function withdraw() {
    if (!window.confirm('Withdraw your application?')) return;
    try { await api(`/applications/${myApp.id}/withdraw`, { method: 'PUT' }); toast('Application withdrawn'); mine.reload(); opp.reload(); }
    catch (e) { toast(e.message, 'error'); }
  }

  async function toggleStatus() {
    try { await api(`/opportunities/${o.id}/status`, { method: 'PUT', params: { value: o.status === 'OPEN' ? 'CLOSED' : 'OPEN' } }); toast('Status updated'); opp.reload(); }
    catch (e) { toast(e.message, 'error'); }
  }

  async function remove() {
    if (!window.confirm('Delete this opportunity?')) return;
    try { await api(`/opportunities/${o.id}`, { method: 'DELETE' }); toast('Opportunity deleted'); navigate('/opportunities'); }
    catch (e) { toast(e.message, 'error'); }
  }

  return (
    <>
      <Link to="/opportunities" className="muted">← Back to opportunities</Link>
      <div className="page-head">
        <div>
          <h1>{o.title}</h1>
          <div className="muted">{o.companyName} · {o.location || 'Location TBA'}</div>
        </div>
        <div className="actions">
          <StatusBadge status={o.type} /> <StatusBadge status={o.expired ? 'EXPIRED' : o.status} />
        </div>
      </div>

      <div className="grid two">
        <section className="card">
          <h2>Details</h2>
          <p>{o.description || 'No description provided.'}</p>
          <div className="kv"><span>Pay</span><b>{fmtPay(o)}</b></div>
          <div className="kv"><span>Deadline</span><b>{fmtDate(o.deadline)}</b></div>
          <div className="kv"><span>Openings</span><b>{o.offersMade}/{o.openings} filled</b></div>
          <div className="kv"><span>Applicants</span><b>{o.applicants}</b></div>
          <h3>Eligibility criteria</h3>
          <div className="kv"><span>Minimum CGPA</span><b>{o.minCgpa}</b></div>
          <div className="kv"><span>Max backlogs</span><b>{o.maxBacklogs}</b></div>
          <div className="kv"><span>Departments</span><b>{o.allowedDepartments}</b></div>
        </section>

        {!isAdmin ? (
          <section className="card">
            <h2>Your application</h2>
            {elig.data && (
              <div className={`alert ${elig.data.eligible ? 'alert-success' : 'alert-error'}`}>
                {elig.data.eligible ? '✅ You meet the eligibility criteria.' : (
                  <>❌ You are not eligible:<ul>{elig.data.reasons.map((r) => <li key={r}>{r}</li>)}</ul></>
                )}
                <div className="hint">Checked by: {elig.data.checkedBy}</div>
              </div>
            )}
            {elig.error && <div className="alert alert-error">{elig.error}</div>}
            {myApp ? (
              <>
                <div className="kv"><span>Status</span><StatusBadge status={myApp.status} /></div>
                <div className="kv"><span>Applied</span><b>{fmtDateTime(myApp.appliedAt)}</b></div>
                {myApp.remarks && <div className="kv"><span>Remarks</span><b>{myApp.remarks}</b></div>}
                {['APPLIED', 'SHORTLISTED', 'INTERVIEW'].includes(myApp.status) &&
                  <button className="btn btn-danger" onClick={withdraw}>Withdraw application</button>}
              </>
            ) : canApply ? (
              <>
                <label className="field"><span className="field-label">Cover note (optional)</span>
                  <textarea rows={4} maxLength={500} value={note} onChange={(e) => setNote(e.target.value)} placeholder="Why are you a good fit?" />
                  <span className="hint">{note.length}/500</span></label>
                <button className="btn btn-primary" disabled={busy || (elig.data && !elig.data.eligible)} onClick={apply}>
                  {busy ? 'Submitting…' : 'Apply now'}
                </button>
              </>
            ) : <div className="alert alert-warn">Applications are closed for this opportunity.</div>}
          </section>
        ) : (
          <section className="card">
            <h2>Manage</h2>
            <div className="actions">
              <Link className="btn" to={`/opportunities/${o.id}/edit`}>Edit</Link>
              <button className="btn" onClick={toggleStatus}>{o.status === 'OPEN' ? 'Close applications' : 'Re-open'}</button>
              <button className="btn btn-danger" onClick={remove}>Delete</button>
            </div>
            <p className="hint">Opportunities with applications cannot be deleted – close them instead.</p>
          </section>
        )}
      </div>

      {isAdmin && (
        <section className="card">
          <h2>Applicants</h2>
          <Applicants opportunityId={o.id} onChange={opp.reload} />
        </section>
      )}
    </>
  );
}
