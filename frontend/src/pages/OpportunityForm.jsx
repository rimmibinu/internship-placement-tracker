import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api.js';
import { useToast } from '../components/Toast.jsx';
import { DEPARTMENTS, Field, Loading, useFetch } from '../components/ui.jsx';

const today = () => new Date().toISOString().slice(0, 10);
const EMPTY = { title: '', type: 'INTERNSHIP', companyId: '', description: '', location: '', salary: '', minCgpa: '6.0', maxBacklogs: '0', depts: ['ALL'], openings: '1', deadline: '' };

function validate(f) {
  const e = {};
  if (!f.title.trim()) e.title = 'Title is required';
  if (!f.companyId) e.companyId = 'Select a company';
  if (f.minCgpa === '' || Number(f.minCgpa) < 0 || Number(f.minCgpa) > 10) e.minCgpa = 'CGPA must be between 0 and 10';
  if (f.maxBacklogs === '' || Number(f.maxBacklogs) < 0 || !Number.isInteger(Number(f.maxBacklogs))) e.maxBacklogs = 'Enter 0 or a positive whole number';
  if (f.salary !== '' && Number(f.salary) < 0) e.salary = 'Salary cannot be negative';
  if (!f.openings || Number(f.openings) < 1 || !Number.isInteger(Number(f.openings))) e.openings = 'At least one opening is required';
  if (!f.deadline) e.deadline = 'Deadline is required';
  else if (f.deadline < today()) e.deadline = 'Deadline cannot be in the past';
  if (f.depts.length === 0) e.depts = 'Select at least one department (or All)';
  return e;
}

export default function OpportunityForm() {
  const { id } = useParams();
  const editing = Boolean(id);
  const navigate = useNavigate();
  const toast = useToast();
  const [f, setF] = useState(EMPTY);
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const { data: companies } = useFetch(() => api('/companies'), []);
  const existing = useFetch(() => (editing ? api(`/opportunities/${id}`) : Promise.resolve(null)), [id]);

  useEffect(() => {
    const o = existing.data;
    if (o) {
      setF({
        title: o.title, type: o.type, companyId: String(o.companyId), description: o.description || '', location: o.location || '',
        salary: o.salary ?? '', minCgpa: String(o.minCgpa), maxBacklogs: String(o.maxBacklogs),
        depts: o.allowedDepartments === 'ALL' ? ['ALL'] : o.allowedDepartments.split(','), openings: String(o.openings), deadline: o.deadline,
      });
    }
  }, [existing.data]);

  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });
  function toggleDept(d) {
    let depts;
    if (d === 'ALL') depts = f.depts.includes('ALL') ? [] : ['ALL'];
    else {
      const without = f.depts.filter((x) => x !== 'ALL');
      depts = without.includes(d) ? without.filter((x) => x !== d) : [...without, d];
    }
    setF({ ...f, depts });
  }

  async function submit(e) {
    e.preventDefault();
    const v = validate(f);
    setErrors(v);
    if (Object.keys(v).length) return;
    const body = {
      title: f.title.trim(), type: f.type, companyId: Number(f.companyId), description: f.description, location: f.location,
      salary: f.salary === '' ? null : Number(f.salary), minCgpa: Number(f.minCgpa), maxBacklogs: Number(f.maxBacklogs),
      allowedDepartments: f.depts.join(','), openings: Number(f.openings), deadline: f.deadline,
    };
    setBusy(true);
    try {
      const saved = await api(editing ? `/opportunities/${id}` : '/opportunities', { method: editing ? 'PUT' : 'POST', body });
      toast(editing ? 'Opportunity updated' : 'Opportunity created');
      navigate(`/opportunities/${saved.id}`);
    } catch (err) {
      setErrors(err.errors || {});
      toast(err.message, 'error');
      setBusy(false);
    }
  }

  if (editing && existing.loading) return <Loading />;
  return (
    <>
      <Link to="/opportunities" className="muted">← Back</Link>
      <h1>{editing ? 'Edit opportunity' : 'New opportunity'}</h1>
      <form className="card form" onSubmit={submit} noValidate>
        <div className="form-grid">
          <Field label="Title *" error={errors.title}><input value={f.title} onChange={set('title')} maxLength={150} /></Field>
          <Field label="Type *" error={errors.type}>
            <select value={f.type} onChange={set('type')}><option value="INTERNSHIP">Internship</option><option value="PLACEMENT">Placement</option></select>
          </Field>
          <Field label="Company *" error={errors.companyId}>
            <select value={f.companyId} onChange={set('companyId')}>
              <option value="">Select…</option>
              {(companies || []).map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </Field>
          <Field label="Location" error={errors.location}><input value={f.location} onChange={set('location')} /></Field>
          <Field label={f.type === 'INTERNSHIP' ? 'Stipend (₹ / month)' : 'CTC (LPA)'} error={errors.salary}>
            <input type="number" min="0" step="any" value={f.salary} onChange={set('salary')} />
          </Field>
          <Field label="Openings *" error={errors.openings}><input type="number" min="1" value={f.openings} onChange={set('openings')} /></Field>
          <Field label="Minimum CGPA *" error={errors.minCgpa}><input type="number" min="0" max="10" step="0.1" value={f.minCgpa} onChange={set('minCgpa')} /></Field>
          <Field label="Max active backlogs *" error={errors.maxBacklogs}><input type="number" min="0" value={f.maxBacklogs} onChange={set('maxBacklogs')} /></Field>
          <Field label="Application deadline *" error={errors.deadline}><input type="date" min={today()} value={f.deadline} onChange={set('deadline')} /></Field>
        </div>
        <Field label="Eligible departments *" error={errors.depts || errors.allowedDepartments}>
          <div className="checks">
            <label className="check"><input type="checkbox" checked={f.depts.includes('ALL')} onChange={() => toggleDept('ALL')} /> All</label>
            {DEPARTMENTS.map((d) => (
              <label className="check" key={d}><input type="checkbox" disabled={f.depts.includes('ALL')} checked={f.depts.includes(d)} onChange={() => toggleDept(d)} /> {d}</label>
            ))}
          </div>
        </Field>
        <Field label="Description" error={errors.description}><textarea rows={4} maxLength={2000} value={f.description} onChange={set('description')} /></Field>
        <div className="actions">
          <button className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : editing ? 'Save changes' : 'Create opportunity'}</button>
          <Link className="btn" to="/opportunities">Cancel</Link>
        </div>
      </form>
    </>
  );
}
