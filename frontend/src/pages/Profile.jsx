import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { Loading, useFetch } from '../components/ui.jsx';

export default function Profile() {
  const { user } = useAuth();
  const { data: s, error, loading } = useFetch(() => api(`/students/${user.studentId}`), [user.studentId]);
  if (loading) return <Loading />;
  if (error) return <div className="alert alert-error">{error}</div>;
  return (
    <>
      <h1>My profile</h1>
      <section className="card narrow">
        <div className="kv"><span>Name</span><b>{s.name}</b></div>
        <div className="kv"><span>Email</span><b>{s.email}</b></div>
        <div className="kv"><span>Roll number</span><b>{s.rollNo}</b></div>
        <div className="kv"><span>Department</span><b>{s.department}</b></div>
        <div className="kv"><span>CGPA</span><b>{s.cgpa}</b></div>
        <div className="kv"><span>Active backlogs</span><b>{s.backlogs}</b></div>
        <div className="kv"><span>Graduation year</span><b>{s.graduationYear}</b></div>
        <div className="kv"><span>Skills</span><b>{s.skills || '—'}</b></div>
        <p className="hint">Academic details are maintained by the placement office. Contact them for corrections.</p>
      </section>
    </>
  );
}
