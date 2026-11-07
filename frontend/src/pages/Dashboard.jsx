import { Link } from 'react-router-dom';
import { useAuth } from '../auth.jsx';
import { api } from '../api.js';
import { Empty, Loading, Stat, StatusBadge, fmtDate, useFetch } from '../components/ui.jsx';

const STATUS_COLORS = { APPLIED: '#3b82f6', SHORTLISTED: '#f59e0b', INTERVIEW: '#8b5cf6', OFFERED: '#16a34a', REJECTED: '#dc2626', WITHDRAWN: '#9ca3af' };

function Bars({ data }) {
  const max = Math.max(1, ...Object.values(data));
  return (
    <div className="bars">
      {Object.entries(data).map(([k, v]) => (
        <div className="bar-row" key={k}>
          <span className="bar-label">{k}</span>
          <div className="bar-track"><div className="bar-fill" style={{ width: `${(v / max) * 100}%`, background: STATUS_COLORS[k] || '#3b82f6' }} /></div>
          <span className="bar-val">{v}</span>
        </div>
      ))}
    </div>
  );
}

function AdminDashboard() {
  const { data: d, error, loading } = useFetch(() => api('/dashboard'), []);
  const { data: dept } = useFetch(() => api('/dashboard/department-report'), []);
  if (loading) return <Loading />;
  if (error) return <div className="alert alert-error">{error}</div>;
  return (
    <>
      <h1>Placement Dashboard</h1>
      <div className="grid stats">
        <Stat label="Students" value={d.totalStudents} />
        <Stat label="Companies" value={d.totalCompanies} />
        <Stat label="Open opportunities" value={d.openOpportunities} />
        <Stat label="Applications" value={d.totalApplications} />
        <Stat label="Offers made" value={d.offers} />
        <Stat label="Placement rate" value={`${d.placementRate}%`} sub={`${d.studentsPlaced} students placed (full-time)`} />
      </div>
      <div className="grid two">
        <section className="card">
          <h2>Applications by status</h2>
          <Bars data={d.applicationsByStatus} />
        </section>
        <section className="card">
          <h2>Applications by company</h2>
          {d.applicationsByCompany.length === 0 ? <Empty text="No applications yet" /> : (
            <table className="table"><tbody>
              {d.applicationsByCompany.map((c) => <tr key={c.name}><td>{c.name}</td><td className="num">{c.count}</td></tr>)}
            </tbody></table>
          )}
        </section>
      </div>
      <div className="grid two">
        <section className="card">
          <h2>Closing in the next 14 days</h2>
          {d.upcomingDeadlines.length === 0 ? <Empty text="No upcoming deadlines" /> : (
            <ul className="list">
              {d.upcomingDeadlines.map((o) => (
                <li key={o.id}>
                  <Link to={`/opportunities/${o.id}`}>{o.title}</Link> <span className="muted">· {o.companyName}</span>
                  <span className="right">{fmtDate(o.deadline)}</span>
                </li>
              ))}
            </ul>
          )}
        </section>
        <section className="card">
          <h2>Department report <span className="hint">(plain JDBC query)</span></h2>
          {!dept ? <Loading /> : (
            <div className="table-wrap"><table className="table">
              <thead><tr><th>Dept</th><th className="num">Students</th><th className="num">Applications</th><th className="num">Offers</th></tr></thead>
              <tbody>{dept.map((r) => (
                <tr key={r.department}><td>{r.department}</td><td className="num">{r.students}</td><td className="num">{r.applications}</td><td className="num">{r.offers}</td></tr>
              ))}</tbody>
            </table></div>
          )}
        </section>
      </div>
    </>
  );
}

function StudentDashboard() {
  const { user } = useAuth();
  const { data: apps, error, loading } = useFetch(() => api('/applications'), []);
  const { data: opps } = useFetch(() => api('/opportunities', { params: { openOnly: true } }), []);
  if (loading) return <Loading />;
  if (error) return <div className="alert alert-error">{error}</div>;
  const count = (s) => apps.filter((a) => a.status === s).length;
  const applied = new Set(apps.map((a) => a.opportunityId));
  const soon = (opps || []).filter((o) => !applied.has(o.id)).slice(0, 5);
  return (
    <>
      <h1>Welcome, {user.name.split(' ')[0]}</h1>
      <div className="grid stats">
        <Stat label="Applications" value={apps.length} />
        <Stat label="Shortlisted" value={count('SHORTLISTED')} />
        <Stat label="Interviews" value={count('INTERVIEW')} />
        <Stat label="Offers" value={count('OFFERED')} />
      </div>
      <div className="grid two">
        <section className="card">
          <h2>My latest applications</h2>
          {apps.length === 0 ? <Empty text="You have not applied yet." /> : (
            <ul className="list">
              {apps.slice(0, 5).map((a) => (
                <li key={a.id}><Link to={`/opportunities/${a.opportunityId}`}>{a.opportunityTitle}</Link>
                  <span className="muted"> · {a.companyName}</span><span className="right"><StatusBadge status={a.status} /></span></li>
              ))}
            </ul>
          )}
        </section>
        <section className="card">
          <h2>Closing soonest (not applied)</h2>
          {soon.length === 0 ? <Empty text="Nothing open right now" /> : (
            <ul className="list">
              {soon.map((o) => (
                <li key={o.id}><Link to={`/opportunities/${o.id}`}>{o.title}</Link>
                  <span className="muted"> · {o.companyName}</span><span className="right">{fmtDate(o.deadline)}</span></li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </>
  );
}

export default function Dashboard() {
  const { isAdmin } = useAuth();
  return isAdmin ? <AdminDashboard /> : <StudentDashboard />;
}
